<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="page-card__header">
        <div>
          <div class="page-card__title">PLC点位管理</div>
          <div class="page-card__subtitle">选择 PLC 后维护点位，并支持读/写调试。</div>
        </div>
        <div class="page-card__actions">
          <el-button type="primary" :disabled="!selectedPlcInfoId" @click="openCreate">新增点位</el-button>
          <el-button :disabled="!selectedPlcInfoId" @click="reload">刷新</el-button>
        </div>
      </div>
    </template>

    <div class="toolbar">
      <el-select v-model="selectedPlcInfoId" placeholder="请选择PLC" filterable style="width: 320px" @change="onPlcChange">
        <el-option v-for="p in plcInfos" :key="p.id" :label="`${p.ipAddr}:${p.portNo}`" :value="p.id" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索：点位/名称" style="width: 260px" clearable />
    </div>

    <el-table
      :data="filteredRows"
      v-loading="loading"
      size="small"
      style="width: 100%"
      row-key="id"
    >
      <el-table-column prop="plcNo" label="PLC点位" width="110" />
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="type" label="类型" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.type === 1 ? 'warning' : 'success'" effect="dark">
            {{ scope.row.type === 1 ? '写入' : '读取' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="调试" width="240">
        <template #default="scope">
          <el-input-number v-model="scope.row.__debugValue" :min="-32768" :max="32767" controls-position="right" size="small" />
          <el-button size="small" type="success" @click="debugRead(scope.row)">读</el-button>
          <el-button size="small" type="primary" @click="debugWrite(scope.row)">写</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" append-to-body>
      <el-form :model="form" label-width="90px" @submit.prevent>
        <el-form-item label="PLC点位">
          <el-input-number v-model="form.plcNo" :min="-999999" :max="999999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="例如：启动信号" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button :label="1">写入</el-radio-button>
            <el-radio-button :label="2">读取</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { plcInfoApi } from '../api/plcInfo'
import { plcAddrApi } from '../api/plcAddr'
import { plcDebugApi } from '../api/plcDebug'
import type { PlcAddr, PlcInfo } from '../types'

type PlcAddrRow = PlcAddr & { __debugValue?: number }

const plcInfos = ref<PlcInfo[]>([])
const selectedPlcInfoId = ref<number | null>(null)
const rows = ref<PlcAddrRow[]>([])
const loading = ref(false)
const keyword = ref('')

const filteredRows = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return rows.value
  return rows.value.filter(r => String(r.plcNo).includes(k) || (r.name || '').toLowerCase().includes(k))
})

async function loadPlcInfos() {
  plcInfos.value = await plcInfoApi.list()
}

async function reload() {
  if (!selectedPlcInfoId.value) return
  loading.value = true
  try {
    const list = await plcAddrApi.listByPlcInfoId(selectedPlcInfoId.value)
    rows.value = list.map(i => ({ ...i, __debugValue: 0 }))
  } finally {
    loading.value = false
  }
}

function onPlcChange() {
  reload()
}

onMounted(async () => {
  await loadPlcInfos()
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)

const form = reactive<PlcAddr>({
  id: undefined,
  plcNo: 0,
  name: '',
  type: 1,
  plcInfoId: 0
})

function openCreate() {
  dialogTitle.value = '新增点位'
  form.id = undefined
  form.plcNo = 0
  form.name = ''
  form.type = 1
  form.plcInfoId = selectedPlcInfoId.value!
  dialogVisible.value = true
}

function openEdit(row: PlcAddr) {
  dialogTitle.value = '编辑点位'
  form.id = row.id
  form.plcNo = row.plcNo
  form.name = row.name
  form.type = row.type
  form.plcInfoId = row.plcInfoId
  dialogVisible.value = true
}

async function submit() {
  saving.value = true
  try {
    if (!selectedPlcInfoId.value) {
      ElMessage.warning('请先选择PLC')
      return
    }

    if (!String(form.name || '').trim()) {
      ElMessage.warning('名称不能为空')
      return
    }

    const duplicated = rows.value.some(r => r.plcNo === form.plcNo && r.id !== form.id)
    if (duplicated) {
      ElMessage.warning('同一PLC下点位编号不能重复')
      return
    }

    const payload: PlcAddr = {
      id: form.id,
      plcNo: form.plcNo,
      name: form.name,
      type: form.type,
      plcInfoId: selectedPlcInfoId.value
    }

    if (payload.id) {
      await plcAddrApi.update(payload)
      ElMessage.success('更新成功')
    } else {
      await plcAddrApi.create(payload)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    await reload()
  } finally {
    saving.value = false
  }
}

async function remove(row: PlcAddr) {
  await ElMessageBox.confirm(`确定删除点位 ${row.plcNo} 吗？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await plcAddrApi.remove(row.id!)
  ElMessage.success('删除成功')
  await reload()
}

async function debugRead(row: PlcAddrRow) {
  const ret = await plcDebugApi.read(row.id!)
  row.__debugValue = ret.value
  ElMessage.success(`读取成功：${ret.value}`)
}

async function debugWrite(row: PlcAddrRow) {
  await plcDebugApi.write(row.id!, Number(row.__debugValue ?? 0))
  ElMessage.success('写入成功')
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

.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 12px;
}
</style>
