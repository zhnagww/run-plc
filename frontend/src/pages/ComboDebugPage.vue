<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="page-card__header">
        <div>
          <div class="page-card__title">组合调试</div>
          <div class="page-card__subtitle">管理组合步骤（点位、类型、目标值、超时）。</div>
        </div>
        <div class="page-card__actions">
          <el-button type="primary" @click="openCreateCombo">新建组合</el-button>
          <el-button :disabled="!selectedComboId" @click="reloadDetails">刷新详情</el-button>
          <el-button type="danger" :disabled="!selectedComboId" @click="removeCombo">删除组合</el-button>
        </div>
      </div>
    </template>

    <div class="toolbar">
      <el-select v-model="selectedComboId" placeholder="请选择组合调试" filterable style="width: 360px" @change="onComboChange">
        <el-option v-for="c in combos" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" :disabled="!selectedComboId" @click="openCreateDetail">新增步骤</el-button>
    </div>

    <el-card shadow="never" class="exec-card">
      <div class="exec-row">
        <div class="exec-left">
          <el-form inline @submit.prevent>
            <el-form-item label="循环次数">
              <el-input-number v-model="loopCount" :min="1" :max="9999" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :disabled="!selectedComboId || execRunning" :loading="execStarting" @click="startExec">
                开始执行
              </el-button>
              <el-button type="danger" :disabled="!execRunning" @click="stopExec">停止</el-button>
              <span class="exec-status">{{ execStatusText }}</span>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <el-input v-model="execLogText" type="textarea" :rows="10" readonly placeholder="执行日志" class="exec-log" />
    </el-card>

    <el-table :data="details" v-loading="loading" size="small" style="width: 100%" row-key="id">
      <el-table-column prop="sortNo" label="排序" width="80" />
      <el-table-column label="点位" min-width="200">
        <template #default="scope">
          <div class="mono">{{ addrLabel(scope.row.plcAddrId) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="scope">
          <el-tag :type="addrType(scope.row.plcAddrId) === 1 ? 'warning' : 'success'" effect="dark">
            {{ addrType(scope.row.plcAddrId) === 1 ? '写入' : '读取' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="plcValue" label="点位值" width="90" />
      <el-table-column prop="timeOutSecond" label="超时/秒" width="90" />
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="moveUp(scope.$index)">上移</el-button>
          <el-button size="small" @click="moveDown(scope.$index)">下移</el-button>
          <el-button size="small" @click="openEditDetail(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="removeDetail(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createComboVisible" title="新建组合调试" width="640px" append-to-body>
      <el-form label-width="90px" @submit.prevent>
        <el-form-item label="名称">
          <el-input v-model="createComboName" placeholder="例如：上料流程" />
        </el-form-item>
        <el-form-item label="选择点位">
          <el-select v-model="createComboPlcInfoId" placeholder="请选择PLC" filterable style="width: 320px" @change="loadAddrs">
            <el-option v-for="p in plcInfos" :key="p.id" :label="`${p.ipAddr}:${p.portNo}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="createComboAddrIds" multiple filterable placeholder="请选择点位（可多选）" style="width: 100%" :disabled="!createComboPlcInfoId">
            <el-option v-for="a in addrs" :key="a.id" :label="`${a.plcNo} - ${a.name}`" :value="a.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createComboVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="createCombo">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" :title="detailDialogTitle" width="640px" append-to-body>
      <el-form label-width="90px" @submit.prevent>
        <el-form-item v-if="!detailForm.id" label="PLC">
          <el-select v-model="detailPlcInfoId" placeholder="请选择PLC" filterable style="width: 320px" @change="loadAddrs">
            <el-option v-for="p in plcInfos" :key="p.id" :label="`${p.ipAddr}:${p.portNo}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!detailForm.id" label="点位">
          <el-select v-model="detailForm.plcAddrId" placeholder="请选择点位" filterable style="width: 100%" :disabled="!detailPlcInfoId">
            <el-option v-for="a in addrs" :key="a.id" :label="`${a.plcNo} - ${a.name}`" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="点位值">
          <el-input-number v-model="detailForm.plcValue" :min="-32768" :max="32767" style="width: 100%" />
        </el-form-item>
        <el-form-item label="超时/秒">
          <el-input-number v-model="detailForm.timeOutSecond" :min="1" :max="3600" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveDetail">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { plcRunApi } from '../api/plcRun'
import { plcRunDetailApi } from '../api/plcRunDetail'
import { plcInfoApi } from '../api/plcInfo'
import { plcAddrApi } from '../api/plcAddr'
import { plcRunExecApi } from '../api/plcRunExec'
import type { PlcAddr, PlcInfo, PlcRun, PlcRunDetail } from '../types'

const combos = ref<PlcRun[]>([])
const selectedComboId = ref<number | null>(null)

const plcInfos = ref<PlcInfo[]>([])
const addrs = ref<PlcAddr[]>([])

const details = ref<PlcRunDetail[]>([])
const loading = ref(false)
const saving = ref(false)

// 地址缓存：id -> label
const addrMap = ref<Record<number, string>>({})
const addrTypeMap = ref<Record<number, number>>({})

function addrLabel(id: number) {
  return addrMap.value[id] || `#${id}`
}

function addrType(id: number) {
  return addrTypeMap.value[id] ?? 2
}

async function ensureAddrMapForDetails() {
  const ids = details.value.map(d => d.plcAddrId).filter(Boolean) as number[]
  const missing = ids.filter(id => addrMap.value[id] == null)
  if (!missing.length) return
  if (!plcInfos.value.length) {
    plcInfos.value = await plcInfoApi.list()
  }
  const tasks = plcInfos.value
    .filter(p => p.id != null)
    .map(p => plcAddrApi.listByPlcInfoId(p.id as number))
  const groups = await Promise.all(tasks)
  for (const group of groups) {
    for (const a of group) {
      if (a.id != null) {
        addrMap.value[a.id] = `${a.plcNo} - ${a.name}`
        addrTypeMap.value[a.id] = a.type
      }
    }
  }
}

async function loadBase() {
  plcInfos.value = await plcInfoApi.list()
  combos.value = await plcRunApi.list()
}

async function loadAddrs() {
  if (!detailPlcInfoId.value && !createComboPlcInfoId.value) {
    addrs.value = []
    return
  }
  const plcInfoId = (detailPlcInfoId.value ?? createComboPlcInfoId.value) as number
  addrs.value = await plcAddrApi.listByPlcInfoId(plcInfoId)
  for (const a of addrs.value) {
    if (a.id != null) {
      addrMap.value[a.id] = `${a.plcNo} - ${a.name}`
      addrTypeMap.value[a.id] = a.type
    }
  }
}

async function reloadDetails() {
  if (!selectedComboId.value) return
  loading.value = true
  try {
    details.value = await plcRunDetailApi.listByPlcRunId(selectedComboId.value)
    await ensureAddrMapForDetails()
  } finally {
    loading.value = false
  }
}

async function onComboChange() {
  await reloadDetails()
}

onMounted(async () => {
  await loadBase()
})

const loopCount = ref(1)
const execStarting = ref(false)
const execRunning = ref(false)
const execRunId = ref<string | null>(null)
const execStatusText = ref('')
const execLogText = ref('')
let execSource: EventSource | null = null

function appendLog(line: string) {
  const s = String(line ?? '')
  if (!s) return
  execLogText.value = execLogText.value ? `${execLogText.value}\n${s}` : s
}

function closeExecSource() {
  if (execSource) {
    execSource.close()
    execSource = null
  }
}

onBeforeUnmount(() => {
  closeExecSource()
})

async function startExec() {
  if (!selectedComboId.value) return
  if (execRunning.value || execStarting.value) return
  if (!loopCount.value || loopCount.value < 1) {
    ElMessage.warning('循环次数必须大于0')
    return
  }

  execStarting.value = true
  execStatusText.value = '启动中...'
  execLogText.value = ''
  execRunId.value = null

  try {
    closeExecSource()
    const url = `/api/plc-run-exec/execute?plcRunId=${selectedComboId.value}&loopCount=${loopCount.value}`
    execSource = new EventSource(url)
    execRunning.value = true

    execSource.addEventListener('run', (e: MessageEvent) => {
      try {
        const data = JSON.parse(e.data)
        execRunId.value = data.runId
        execStatusText.value = `运行中（runId=${data.runId}）`
      } catch {
        execStatusText.value = '运行中'
      }
    })

    execSource.addEventListener('log', (e: MessageEvent) => {
      appendLog(e.data)
    })

    execSource.addEventListener('status', (e: MessageEvent) => {
      try {
        const data = JSON.parse(e.data)
        execStatusText.value = data.message || (data.success ? '成功' : '失败')
        appendLog(`状态：${execStatusText.value}`)
      } catch {
        execStatusText.value = '已结束'
      } finally {
        execRunning.value = false
        execStarting.value = false
        closeExecSource()
      }
    })

    execSource.onerror = () => {
      execStatusText.value = '连接中断'
      execRunning.value = false
      execStarting.value = false
      closeExecSource()
    }
  } catch (e) {
    execRunning.value = false
    execStarting.value = false
    closeExecSource()
    throw e
  } finally {
    execStarting.value = false
  }
}

async function stopExec() {
  if (!execRunning.value) return
  if (!execRunId.value) {
    ElMessage.warning('runId未获取到，稍后再试')
    return
  }
  await plcRunExecApi.stop(execRunId.value)
  appendLog('已发送停止指令')
}

// 新建组合
const createComboVisible = ref(false)
const createComboName = ref('')
const createComboPlcInfoId = ref<number | null>(null)
const createComboAddrIds = ref<number[]>([])

function openCreateCombo() {
  createComboName.value = ''
  createComboPlcInfoId.value = null
  createComboAddrIds.value = []
  addrs.value = []
  createComboVisible.value = true
}

async function createCombo() {
  saving.value = true
  try {
    if (!createComboName.value.trim()) {
      ElMessage.warning('请输入组合名称')
      return
    }
    const name = createComboName.value.trim()
    if (combos.value.some(c => (c.name ?? '').trim() === name)) {
      ElMessage.warning('组合名称不能重复')
      return
    }
    if (!createComboAddrIds.value.length) {
      ElMessage.warning('请选择至少一个点位')
      return
    }

    const plcAddrIds = createComboAddrIds.value.join(',')
    await plcRunApi.create({ name, plcAddrIds })
    ElMessage.success('创建成功')
    createComboVisible.value = false

    combos.value = await plcRunApi.list()
  } finally {
    saving.value = false
  }
}

async function removeCombo() {
  if (!selectedComboId.value) return
  const combo = combos.value.find(c => c.id === selectedComboId.value)
  await ElMessageBox.confirm(`确定删除组合：${combo?.name ?? selectedComboId.value} 吗？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })

  await plcRunApi.remove(selectedComboId.value)
  ElMessage.success('删除成功')
  selectedComboId.value = null
  details.value = []
  combos.value = await plcRunApi.list()
}

// 详情编辑
const detailDialogVisible = ref(false)
const detailDialogTitle = ref('')
const detailPlcInfoId = ref<number | null>(null)

const detailForm = reactive<PlcRunDetail>({
  id: undefined,
  plcRunId: 0,
  plcAddrId: 0,
  sortNo: 1,
  plcValue: 0,
  timeOutSecond: 60
})

function openCreateDetail() {
  if (!selectedComboId.value) return
  detailDialogTitle.value = '新增步骤'
  detailForm.id = undefined
  detailForm.plcRunId = selectedComboId.value
  detailForm.plcAddrId = 0
  detailForm.sortNo = (details.value.length || 0) + 1
  detailForm.plcValue = 0
  detailForm.timeOutSecond = 60
  detailPlcInfoId.value = null
  addrs.value = []
  detailDialogVisible.value = true
}

function openEditDetail(row: PlcRunDetail) {
  detailDialogTitle.value = '编辑步骤'
  detailForm.id = row.id
  detailForm.plcRunId = row.plcRunId
  detailForm.plcAddrId = row.plcAddrId
  detailForm.sortNo = row.sortNo
  detailForm.plcValue = row.plcValue
  detailForm.timeOutSecond = row.timeOutSecond
  detailPlcInfoId.value = null
  addrs.value = []
  detailDialogVisible.value = true
}

async function saveDetail() {
  saving.value = true
  try {
    if (!detailForm.plcAddrId) {
      ElMessage.warning('请选择点位')
      return
    }

    if (detailForm.id) {
      await plcRunDetailApi.update({ ...detailForm } as PlcRunDetail)
      ElMessage.success('更新成功')
    } else {
      await plcRunDetailApi.create({ ...detailForm } as PlcRunDetail)
      ElMessage.success('创建成功')
    }

    detailDialogVisible.value = false
    await reloadDetails()
  } finally {
    saving.value = false
  }
}

async function removeDetail(row: PlcRunDetail) {
  await ElMessageBox.confirm('确定删除该步骤吗？', '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await plcRunDetailApi.remove(row.id!)
  ElMessage.success('删除成功')
  await reloadDetails()
}

// 排序：上移/下移 => 交换 sortNo，然后逐条调用 update-sort-no
async function persistSort() {
  if (!selectedComboId.value) return
  const sorted = [...details.value].sort((a, b) => a.sortNo - b.sortNo)
  for (let i = 0; i < sorted.length; i++) {
    const id = sorted[i].id
    if (!id) continue
    const sortNo = i + 1
    if (sorted[i].sortNo !== sortNo) {
      sorted[i].sortNo = sortNo
      await plcRunDetailApi.updateSortNo(id, sortNo)
    }
  }
  details.value = sorted
}

async function moveUp(index: number) {
  if (index <= 0) return
  const a = details.value[index]
  const b = details.value[index - 1]
  const t = a.sortNo
  a.sortNo = b.sortNo
  b.sortNo = t
  await persistSort()
}

async function moveDown(index: number) {
  if (index >= details.value.length - 1) return
  const a = details.value[index]
  const b = details.value[index + 1]
  const t = a.sortNo
  a.sortNo = b.sortNo
  b.sortNo = t
  await persistSort()
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

.exec-card {
  margin-bottom: 12px;
  border: 1px solid rgba(255, 255, 255, 0.10);
  background: rgba(255, 255, 255, 0.03);
}

.exec-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.exec-left {
  flex: 1;
}

.exec-status {
  margin-left: 10px;
  font-size: 12px;
  opacity: 0.8;
}

.exec-log :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}
</style>
