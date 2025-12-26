<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="page-card__header">
        <div>
          <div class="page-card__title">PLC信息管理</div>
          <div class="page-card__subtitle">维护 PLC 设备连接信息（IP / 端口）。删除会级联清理该 PLC 下的点位与组合调试引用。</div>
        </div>
        <div class="page-card__actions">
          <el-button type="primary" @click="openCreate">新增PLC</el-button>
          <el-button type="success" @click="openImport">Excel导入</el-button>
          <el-button @click="reload">刷新</el-button>
        </div>
      </div>
    </template>

    <el-table :data="rows" v-loading="loading" size="small" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="ipAddr" label="IP地址" min-width="180" />
      <el-table-column prop="portNo" label="端口" width="120" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" append-to-body>
      <el-form :model="form" label-width="80px" @submit.prevent>
        <el-form-item label="IP地址">
          <el-input v-model="form.ipAddr" placeholder="例如：192.168.10.1" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input-number v-model="form.portNo" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="Excel导入" width="760px" append-to-body>
      <el-alert type="info" :closable="false" class="import-help">
        <template #title>表头（第一行）</template>
        <div class="import-help__content">
          <div class="import-help__code">ip | port | plc_no | name | type</div>
          <div class="import-help__desc">type 支持：写入/读取 或 1/2</div>
          <div class="import-help__desc">port 为空我做了默认 502</div>
        </div>
      </el-alert>

      <el-alert type="warning" :closable="false" class="import-help">
        <template #title>严格/非严格区别</template>
        <div class="import-help__content">
          <div class="import-help__desc">非严格模式：遇到错误行继续导入，最后汇总 errors</div>
          <div class="import-help__desc">严格模式：只要 errors 非空则事务回滚，并返回错误报告</div>
        </div>
      </el-alert>

      <el-form label-width="100px" @submit.prevent>
        <el-form-item label="选择文件">
          <el-upload
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            accept=".xlsx"
            :on-change="onFileChange"
          >
            <el-button>选择.xlsx</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="严格模式">
          <el-switch v-model="importStrict" />
        </el-form-item>
      </el-form>

      <el-card v-if="importReport" shadow="never" class="import-report">
        <div class="import-report__summary">
          <div>PLC 新增：{{ importReport.plcInserted }}，跳过：{{ importReport.plcSkipped }}</div>
          <div>点位 新增：{{ importReport.addrInserted }}，跳过：{{ importReport.addrSkipped }}</div>
          <div v-if="importReport.errors?.length">错误：{{ importReport.errors.length }}</div>
        </div>

        <el-table v-if="importReport.errors?.length" :data="importReport.errors" size="small" style="width: 100%">
          <el-table-column prop="row" label="行号" width="80" />
          <el-table-column prop="message" label="错误信息" min-width="240" />
        </el-table>
      </el-card>

      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">开始导入</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { plcInfoApi } from '../api/plcInfo'
import { plcImportApi } from '../api/plcImport'
import type { PlcInfo } from '../types'
import type { PlcImportReport } from '../types'

const rows = ref<PlcInfo[]>([])
const loading = ref(false)

async function reload() {
  loading.value = true
  try {
    rows.value = await plcInfoApi.list()
  } finally {
    loading.value = false
  }
}

onMounted(reload)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)

const form = reactive<PlcInfo>({
  id: undefined,
  ipAddr: '',
  portNo: 502
})

function openCreate() {
  dialogTitle.value = '新增PLC'
  form.id = undefined
  form.ipAddr = ''
  form.portNo = 502
  dialogVisible.value = true
}

function openEdit(row: PlcInfo) {
  dialogTitle.value = '编辑PLC'
  form.id = row.id
  form.ipAddr = row.ipAddr
  form.portNo = row.portNo
  dialogVisible.value = true
}

async function submit() {
  saving.value = true
  try {
    if (!form.ipAddr) {
      ElMessage.warning('请输入IP地址')
      return
    }
    if (!form.portNo) {
      ElMessage.warning('请输入端口')
      return
    }

    if (form.id) {
      await plcInfoApi.update({ id: form.id, ipAddr: form.ipAddr, portNo: form.portNo })
      ElMessage.success('更新成功')
    } else {
      await plcInfoApi.create({ ipAddr: form.ipAddr, portNo: form.portNo })
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    await reload()
  } finally {
    saving.value = false
  }
}

async function remove(row: PlcInfo) {
  await ElMessageBox.confirm(
    `确定删除 PLC：${row.ipAddr}:${row.portNo} 吗？\n\n注意：会级联删除该PLC下点位，并清理相关组合调试引用。`,
    '确认删除',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
  )

  await plcInfoApi.remove(row.id!)
  ElMessage.success('删除成功')
  await reload()
}

const importVisible = ref(false)
const importStrict = ref(false)
const importing = ref(false)
const importFile = ref<File | null>(null)
const importReport = ref<PlcImportReport | null>(null)

function openImport() {
  importVisible.value = true
  importStrict.value = false
  importFile.value = null
  importReport.value = null
}

function onFileChange(file: any) {
  const raw: File | undefined = file?.raw
  if (!raw) return
  importFile.value = raw
}

async function doImport() {
  if (!importFile.value) {
    ElMessage.warning('请选择.xlsx文件')
    return
  }
  importing.value = true
  try {
    const res = await plcImportApi.importExcel(importFile.value, importStrict.value)
    importReport.value = (res.data as PlcImportReport) ?? null
    if (res.code === 200) {
      ElMessage.success(res.msg || '导入完成')
      await reload()
    } else {
      ElMessage.error(res.msg || '导入失败')
    }
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.page-card {
  border: 1px solid rgba(255, 255, 255, 0.10);
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(10px);
}

.page-card__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.page-card__title {
  font-weight: 800;
  letter-spacing: 0.2px;
}

.page-card__subtitle {
  font-size: 12px;
  opacity: 0.65;
  margin-top: 6px;
}

.page-card__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.import-report {
  margin-top: 10px;
}

.import-report__summary {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 8px;
  font-size: 12px;
  opacity: 0.85;
}

.import-help {
  margin-bottom: 10px;
}

.import-help__content {
  display: grid;
  gap: 4px;
}

.import-help__code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.10);
  padding: 6px 10px;
  border-radius: 6px;
  width: fit-content;
}

.import-help__desc {
  font-size: 12px;
  opacity: 0.9;
}
</style>
