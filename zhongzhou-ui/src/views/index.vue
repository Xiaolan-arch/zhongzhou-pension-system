<template>
  <div class="app-container dashboard" v-loading="loading" element-loading-text="工作台数据加载中">
    <div class="hero">
      <div class="hero-left">
        <div class="hero-greeting">{{ greeting }}，{{ nickname }}</div>
        <div class="hero-meta">
          <span class="tenant">{{ summary.tenantName || '全部机构' }}</span>
          <span class="dot">·</span>
          <span>{{ todayText }}</span>
          <span class="dot">·</span>
          <span>{{ isGroup ? '集团总览' : '本机构视图' }}</span>
        </div>
      </div>
      <div class="hero-right">
        <span class="ws-state" :class="{ online: wsConnected }">
          <i class="ws-dot"></i>{{ wsConnected ? '实时在线' : '连接中' }}
        </span>
        <span class="updated" v-if="lastUpdated">更新于 {{ lastUpdated }}</span>
        <el-button class="layout-btn" :icon="Operation" @click="openLayoutDialog">布局</el-button>
        <el-select v-model="refreshSeconds" size="small" class="refresh-select" @change="onRefreshChange">
          <el-option :value="0" label="手动刷新" />
          <el-option :value="30" label="30 秒刷新" />
          <el-option :value="60" label="1 分钟刷新" />
          <el-option :value="300" label="5 分钟刷新" />
        </el-select>
        <el-button circle :icon="Refresh" @click="loadAll" />
      </div>
    </div>

    <el-alert
      v-if="summary.highRiskReports > 0"
      class="risk-alert"
      type="warning"
      :closable="false"
      show-icon
      :title="`有 ${summary.highRiskReports} 份高风险健康报告待关注，建议及时查看老人照护情况`"
    />

    <el-alert
      v-if="summary.overdueCount > 0"
      class="risk-alert"
      type="error"
      :closable="false"
      show-icon
      :title="`有 ${summary.overdueCount} 笔逾期账单（超 30 天未付清），逾期金额 ${fmtAmount(summary.overdueAmount)}，请及时跟进收款`"
    />

    <div class="dash-flow">
      <section v-show="visibleOf('kpi')" :style="{ order: orderOf('kpi') }">
        <el-row :gutter="16" class="kpi-row">
          <el-col v-for="item in kpiCards" :key="item.key" :xs="12" :sm="8" :lg="4">
            <KpiCard v-bind="item" @click="go(item.path)" />
          </el-col>
        </el-row>
      </section>

      <section v-show="visibleOf('charts')" :style="{ order: orderOf('charts') }">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="16">
            <TrendChart :data="trend" :months="months" @change="changeMonths" />
          </el-col>
          <el-col :xs="24" :lg="8">
            <DistributionChart :care-level="distribution.careLevel" :bill-type="distribution.billType" />
          </el-col>
        </el-row>
      </section>

      <section v-show="visibleOf('care')" :style="{ order: orderOf('care') }">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="10">
            <FloorOccupancy :data="distribution.floorBed" />
          </el-col>
          <el-col :xs="24" :lg="14">
            <TodoPanel :todo="todo" @navigate="go" />
          </el-col>
        </el-row>
      </section>

      <section v-if="isGroup" v-show="visibleOf('tenant')" :style="{ order: orderOf('tenant') }">
        <el-row :gutter="16">
          <el-col :span="24">
            <TenantCompare :data="distribution.tenants" />
          </el-col>
        </el-row>
      </section>

      <section v-show="visibleOf('quick')" :style="{ order: orderOf('quick') }">
        <el-card shadow="never" class="panel quick-panel">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">快捷入口</span>
              <span class="panel-tip">仅展示当前账号有权限的模块</span>
            </div>
          </template>
          <div class="quick-grid">
            <div v-for="item in quickEntries" :key="item.path" class="quick-item" @click="go(item.path)">
              <div class="quick-icon" :class="`tone-${item.tone}`">
                <el-icon><component :is="iconOf(item.icon)" /></el-icon>
              </div>
              <div class="quick-body">
                <div class="quick-title">{{ item.title }}</div>
                <div class="quick-desc">{{ item.desc }}</div>
              </div>
            </div>
            <div v-if="!quickEntries.length" class="quick-empty">当前账号暂无可访问的业务模块</div>
          </div>
        </el-card>
      </section>
    </div>

    <el-dialog v-model="layoutDialog" title="个性化布局" width="420px" append-to-body>
      <div class="layout-tip">拖动调整区块顺序，开关控制是否显示</div>
      <draggable v-model="layoutDraft" item-key="key" handle=".drag-handle" :animation="180" class="layout-list">
        <template #item="{ element }">
          <div class="layout-item">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="layout-name">{{ element.title }}</span>
            <el-switch v-model="element.visible" />
          </div>
        </template>
      </draggable>
      <template #footer>
        <el-button @click="resetLayout">恢复默认</el-button>
        <el-button @click="layoutDialog = false">取消</el-button>
        <el-button type="primary" :loading="layoutSaving" @click="submitLayout">保存布局</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Operation, Rank, Refresh } from '@element-plus/icons-vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import useUserStore from '@/store/modules/user'
import { getToken } from '@/utils/auth'
import {
  getDashboardDistribution,
  getDashboardLayout,
  getDashboardSummary,
  getDashboardTodo,
  getDashboardTrend,
  saveDashboardLayout
} from '@/api/business/dashboard'
import KpiCard from '@/views/dashboard/KpiCard.vue'
import TrendChart from '@/views/dashboard/TrendChart.vue'
import DistributionChart from '@/views/dashboard/DistributionChart.vue'
import FloorOccupancy from '@/views/dashboard/FloorOccupancy.vue'
import TodoPanel from '@/views/dashboard/TodoPanel.vue'
import TenantCompare from '@/views/dashboard/TenantCompare.vue'

const REFRESH_KEY = 'dashboard_refresh_seconds'
const LAYOUT_KEY = 'dashboard_layout'
const BLOCK_DEFS = [
  { key: 'kpi', title: '核心指标' },
  { key: 'charts', title: '经营趋势与结构分布' },
  { key: 'care', title: '楼层床位与今日待办' },
  { key: 'tenant', title: '机构对比' },
  { key: 'quick', title: '快捷入口' }
]
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const months = ref(6)
const refreshSeconds = ref(Number(localStorage.getItem(REFRESH_KEY) || 60))
const lastUpdated = ref('')
const summary = ref({})
const trend = ref([])
const distribution = ref({ careLevel: [], floorBed: [], billType: [], serviceItem: [], tenants: [] })
const todo = ref({ pendingVisits: [], unfinishedServices: [], unpaidBills: [], keyElders: [] })
const layoutBlocks = ref(BLOCK_DEFS.map((item) => ({ ...item, visible: true })))
const layoutDialog = ref(false)
const layoutDraft = ref([])
const layoutSaving = ref(false)
const wsConnected = ref(false)
let timer = null
let socket = null
let wsRetryTimer = null
let wsStopped = false

const nickname = computed(() => userStore.nickname || '管理员')
const isGroup = computed(() => summary.value.scope === 'all')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() => {
  const now = new Date()
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][now.getDay()]
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${week}`
})

const kpiCards = computed(() => {
  const data = summary.value
  return [
    {
      key: 'residence',
      label: '在住老人',
      value: data.inResidence ?? 0,
      unit: '人',
      desc: `档案 ${data.elderTotal ?? 0} 人 · 待入住 ${data.pendingElders ?? 0} 人`,
      icon: 'User',
      tone: 'green',
      path: '/residence/overview'
    },
    {
      key: 'bed',
      label: '床位使用率',
      value: data.bedOccupancy ?? 0,
      unit: '%',
      desc: `已住 ${data.bedOccupied ?? 0} / 总 ${data.bedTotal ?? 0} · 空余 ${data.bedFree ?? 0}`,
      icon: 'House',
      tone: 'blue',
      path: '/residence/overview'
    },
    {
      key: 'income',
      label: '本月已收费用',
      value: fmtAmount(data.monthReceived),
      unit: '',
      desc: `应收 ${fmtAmount(data.monthReceivable)} · 完成率 ${data.monthIncomeRate ?? 0}%`,
      icon: 'Money',
      tone: 'orange',
      path: '/bill'
    },
    {
      key: 'unpaid',
      label: '待缴总额',
      value: fmtAmount(data.unpaidTotal),
      unit: '',
      desc: `涉及 ${data.unpaidCount ?? 0} 张账单`,
      icon: 'Warning',
      tone: 'red',
      path: '/bill'
    },
    {
      key: 'overdue',
      label: '逾期账单',
      value: data.overdueCount ?? 0,
      unit: '笔',
      desc: `逾期金额 ${fmtAmount(data.overdueAmount)} · 超 30 天未付清`,
      icon: 'CircleClose',
      tone: 'red',
      path: '/bill'
    },
    {
      key: 'visit',
      label: '今日探访',
      value: data.visitToday ?? 0,
      unit: '人次',
      desc: `待确认 ${data.visitPendingToday ?? 0} 条`,
      icon: 'Calendar',
      tone: 'purple',
      path: '/visit'
    },
    {
      key: 'service',
      label: '待执行服务',
      value: data.servicePending ?? 0,
      unit: '单',
      desc: `未完成合计 ${data.serviceUnfinished ?? 0} 单`,
      icon: 'Spa',
      tone: 'teal',
      path: '/service/order'
    }
  ]
})

const quickEntries = computed(() => {
  const entries = [
    { title: '来访预约', desc: '登记与确认来访', path: '/visit', perm: 'business:visit:list', icon: 'UserFilled', tone: 'green' },
    { title: '在住管理', desc: '楼层房间床位', path: '/residence/overview', perm: 'business:residence:list', icon: 'House', tone: 'blue' },
    { title: '服务订单', desc: '护理任务派工', path: '/service/order', perm: 'business:service:order:list', icon: 'Document', tone: 'teal' },
    { title: '财务账单', desc: '收款与结算', path: '/bill', perm: 'business:bill:list', icon: 'Money', tone: 'orange' },
    { title: '健康报告', desc: 'AI 照护评估', path: '/ai-report', perm: 'business:report:list', icon: 'DataAnalysis', tone: 'purple' },
    { title: '机构管理', desc: '租户与机构配置', path: '/system/tenant', perm: 'system:tenant:list', icon: 'OfficeBuilding', tone: 'red' }
  ]
  const permissions = userStore.permissions || []
  const all = permissions.includes('*:*:*')
  return entries.filter((entry) => all || permissions.includes(entry.perm))
})

function iconOf(name) {
  return ElementPlusIcons[name] || ElementPlusIcons.DataLine
}

function fmtAmount(value) {
  const amount = Number(value || 0)
  return `¥${Math.round(amount).toLocaleString('zh-CN')}`
}

function go(path) {
  if (!path) return
  router.push(path).catch(() => {
    ElMessage.warning('该模块暂未开放访问权限')
  })
}

async function loadTrend() {
  const res = await getDashboardTrend(months.value)
  trend.value = res.data || []
}

async function loadAll() {
  loading.value = true
  try {
    const [summaryRes, distributionRes, todoRes] = await Promise.all([
      getDashboardSummary(),
      getDashboardDistribution(),
      getDashboardTodo()
    ])
    summary.value = summaryRes.data || {}
    distribution.value = { ...distribution.value, ...(distributionRes.data || {}) }
    todo.value = { ...todo.value, ...(todoRes.data || {}) }
    await loadTrend()
    lastUpdated.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch (error) {
    console.error('[Dashboard] 数据加载失败', error)
    ElMessage.error(error?.message || '工作台数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function loadRealtime() {
  try {
    const [summaryRes, todoRes] = await Promise.all([getDashboardSummary(), getDashboardTodo()])
    summary.value = summaryRes.data || {}
    todo.value = { ...todo.value, ...(todoRes.data || {}) }
    lastUpdated.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch (error) {
    console.warn('[Dashboard] 自动刷新失败', error)
  }
}

function changeMonths(value) {
  months.value = value
  loadTrend().catch((error) => {
    console.error('[Dashboard] 趋势加载失败', error)
    ElMessage.error('趋势数据加载失败')
  })
}

function applyTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  if (refreshSeconds.value > 0) {
    timer = setInterval(loadRealtime, refreshSeconds.value * 1000)
  }
}

function onRefreshChange(value) {
  localStorage.setItem(REFRESH_KEY, String(value))
  applyTimer()
}

function orderOf(key) {
  const index = layoutBlocks.value.findIndex((item) => item.key === key)
  return index < 0 ? BLOCK_DEFS.length : index
}

function visibleOf(key) {
  const block = layoutBlocks.value.find((item) => item.key === key)
  return block ? block.visible !== false : true
}

function normalizeLayout(raw) {
  let parsed = []
  try {
    parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch (error) {
    parsed = []
  }
  const result = []
  if (Array.isArray(parsed)) {
    parsed.forEach((item) => {
      if (!item || !item.key) return
      const def = BLOCK_DEFS.find((block) => block.key === item.key)
      if (def && !result.some((block) => block.key === def.key)) {
        result.push({ ...def, visible: item.visible !== false })
      }
    })
  }
  BLOCK_DEFS.forEach((def) => {
    if (!result.some((block) => block.key === def.key)) {
      result.push({ ...def, visible: true })
    }
  })
  return result
}

function applyLayout(raw) {
  layoutBlocks.value = normalizeLayout(raw)
  if (typeof raw === 'string' && raw) {
    localStorage.setItem(LAYOUT_KEY, raw)
  }
}

async function loadLayout() {
  try {
    const res = await getDashboardLayout()
    if (res.layout) {
      applyLayout(res.layout)
      return
    }
  } catch (error) {
    console.warn('[Dashboard] 服务端布局读取失败，使用本地缓存', error)
  }
  applyLayout(localStorage.getItem(LAYOUT_KEY))
}

function openLayoutDialog() {
  layoutDraft.value = layoutBlocks.value.map((item) => ({ ...item }))
  layoutDialog.value = true
}

function resetLayout() {
  layoutDraft.value = BLOCK_DEFS.map((item) => ({ ...item, visible: true }))
}

async function submitLayout() {
  layoutSaving.value = true
  const payload = JSON.stringify(layoutDraft.value.map(({ key, visible }) => ({ key, visible })))
  layoutBlocks.value = layoutDraft.value.map((item) => ({ ...item }))
  localStorage.setItem(LAYOUT_KEY, payload)
  try {
    await saveDashboardLayout(payload)
    ElMessage.success('布局已保存')
  } catch (error) {
    console.warn('[Dashboard] 布局同步失败，已暂存本地', error)
    ElMessage.warning('布局已暂存本地，服务端同步失败')
  } finally {
    layoutSaving.value = false
    layoutDialog.value = false
  }
}

function connectRealtime() {
  if (wsStopped) return
  const token = getToken()
  if (!token) {
    wsRetryTimer = setTimeout(connectRealtime, 5000)
    return
  }
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  try {
    socket = new WebSocket(`${protocol}://${window.location.host}/ws/dashboard?token=${encodeURIComponent(token)}`)
  } catch (error) {
    console.warn('[Dashboard] 实时连接创建失败', error)
    wsRetryTimer = setTimeout(connectRealtime, 5000)
    return
  }
  socket.onopen = () => {
    wsConnected.value = true
  }
  socket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data)
      if (message.type === 'dashboard-refresh') {
        loadRealtime()
      }
    } catch (error) {
      // 忽略非 JSON 消息
    }
  }
  socket.onclose = () => {
    wsConnected.value = false
    socket = null
    if (!wsStopped) {
      wsRetryTimer = setTimeout(connectRealtime, 5000)
    }
  }
  socket.onerror = () => {
    wsConnected.value = false
  }
}

function closeRealtime() {
  wsStopped = true
  clearTimeout(wsRetryTimer)
  if (socket) {
    socket.onclose = null
    socket.close()
    socket = null
  }
  wsConnected.value = false
}

onMounted(() => {
  loadAll()
  loadLayout()
  applyTimer()
  connectRealtime()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  closeRealtime()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding-top: 12px;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: linear-gradient(120deg, #2fa36b 0%, #5bc08d 100%);
  box-shadow: 0 10px 28px rgba(47, 163, 107, 0.22);
  color: #fff;
}

.hero-greeting {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.hero-meta {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.86);

  .dot {
    margin: 0 6px;
  }

  .tenant {
    font-weight: 600;
  }
}

.hero-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.updated {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.ws-state {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.14);

  .ws-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #f2b23c;
  }

  &.online {
    color: #fff;
    background: rgba(47, 163, 107, 0.35);

    .ws-dot {
      background: #4ade80;
    }
  }
}

.refresh-select {
  width: 112px;

  :deep(.el-select__wrapper) {
    background: rgba(255, 255, 255, 0.9);
  }
}

.layout-btn {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(255, 255, 255, 0.9);
  color: #2fa36b;

  &:hover {
    background: #fff;
    border-color: #fff;
    color: #2fa36b;
  }
}

.dash-flow {
  display: flex;
  flex-direction: column;
}

.layout-tip {
  margin-bottom: 10px;
  font-size: 12px;
  color: #8a94a6;
}

.layout-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.layout-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #eef1f0;
  border-radius: 8px;
  background: #f8faf9;
}

.drag-handle {
  cursor: move;
  color: #98a2b3;
}

.layout-name {
  flex: 1;
  font-size: 13px;
  color: #1f2d3d;
}

.risk-alert {
  margin-bottom: 16px;
  border-radius: 10px;
}

.kpi-row {
  margin-bottom: 4px;

  .el-col {
    margin-bottom: 16px;
  }
}

.panel {
  border-radius: 12px;
  border: 1px solid #eef1f0;
  margin-bottom: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.panel-tip {
  font-size: 12px;
  color: #8a94a6;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 12px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 10px;
  background: #f8faf9;
  border: 1px solid #eef1f0;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #fff;
    border-color: rgba(47, 163, 107, 0.4);
    box-shadow: 0 6px 18px rgba(31, 45, 61, 0.08);
    transform: translateY(-2px);
  }
}

.quick-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
}

.quick-body {
  min-width: 0;
}

.quick-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
}

.quick-desc {
  margin-top: 3px;
  font-size: 12px;
  color: #8a94a6;
}

.quick-empty {
  grid-column: 1 / -1;
  padding: 20px 0;
  text-align: center;
  font-size: 13px;
  color: #a3acb9;
}

.tone-green {
  background: #e8f6ee;
  color: #2fa36b;
}
.tone-blue {
  background: #eaf2fe;
  color: #4a8ef0;
}
.tone-orange {
  background: #fdf3e7;
  color: #f2a65a;
}
.tone-red {
  background: #fdeceb;
  color: #f05a5a;
}
.tone-purple {
  background: #f0edfb;
  color: #7a6ad8;
}
.tone-teal {
  background: #e6f5f5;
  color: #1f9c94;
}

@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-right {
    width: 100%;
    justify-content: flex-end;
  }

  .hero-greeting {
    font-size: 18px;
  }
}
</style>
