<template>
  <el-card shadow="never" class="panel">
    <template #header>
      <div class="panel-header">
        <span class="panel-title">今日待办</span>
        <el-tag size="small" effect="plain" type="warning">{{ totalCount }} 项待处理</el-tag>
      </div>
    </template>

    <el-tabs v-model="tab" class="todo-tabs">
      <el-tab-pane :label="`探访 ${listOf('pendingVisits').length}`" name="visits">
        <div
          v-for="item in listOf('pendingVisits')"
          :key="item.visitId"
          class="todo-item"
          @click="$emit('navigate', '/visit')"
        >
          <div class="todo-main">
            <div class="todo-title">{{ item.elderName }} · {{ item.visitorName }}</div>
            <div class="todo-sub">{{ item.visitDate }} {{ item.visitTime }} · {{ item.peopleCount }} 人</div>
          </div>
          <el-tag size="small" :type="visitTone(item.status)">{{ visitText(item.status) }}</el-tag>
        </div>
        <el-empty v-if="!listOf('pendingVisits').length" description="暂无待探访预约" :image-size="56" />
      </el-tab-pane>

      <el-tab-pane :label="`服务 ${listOf('unfinishedServices').length}`" name="services">
        <div
          v-for="item in listOf('unfinishedServices')"
          :key="item.orderId"
          class="todo-item"
          @click="$emit('navigate', '/service/order')"
        >
          <div class="todo-main">
            <div class="todo-title">{{ item.elderName }} · {{ item.itemName }}</div>
            <div class="todo-sub">{{ item.serviceTime }} · {{ item.nurseName || '待分配护理员' }}</div>
          </div>
          <el-tag size="small" :type="orderTone(item.status)">{{ orderText(item.status) }}</el-tag>
        </div>
        <el-empty v-if="!listOf('unfinishedServices').length" description="暂无待执行服务" :image-size="56" />
      </el-tab-pane>

      <el-tab-pane :label="`欠费 ${listOf('unpaidBills').length}`" name="bills">
        <div
          v-for="item in listOf('unpaidBills')"
          :key="item.billId"
          class="todo-item"
          @click="$emit('navigate', '/bill')"
        >
          <div class="todo-main">
            <div class="todo-title">{{ item.elderName }} · {{ item.billMonth }}</div>
            <div class="todo-sub">{{ billTypeText(item.billType) }} · 待缴 {{ formatMoney(item.unpaidAmount) }}</div>
          </div>
          <el-tag size="small" type="danger">待缴</el-tag>
        </div>
        <el-empty v-if="!listOf('unpaidBills').length" description="暂无欠费账单" :image-size="56" />
      </el-tab-pane>

      <el-tab-pane :label="`关注 ${listOf('keyElders').length}`" name="elders">
        <div
          v-for="item in listOf('keyElders')"
          :key="item.elderId"
          class="todo-item"
          @click="$emit('navigate', '/residence/overview')"
        >
          <div class="todo-main">
            <div class="todo-title">{{ item.elderName }} · {{ item.age }} 岁</div>
            <div class="todo-sub">入住 {{ item.checkinDate || '--' }} · 需重点照护</div>
          </div>
          <el-tag size="small" type="warning">{{ careLevelText(item.careLevel) }}</el-tag>
        </div>
        <el-empty v-if="!listOf('keyElders').length" description="暂无重点照护老人" :image-size="56" />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup name="TodoPanel">
import { computed, ref } from 'vue'

const props = defineProps({
  todo: { type: Object, default: () => ({}) }
})

defineEmits(['navigate'])

const tab = ref('visits')

const totalCount = computed(() => {
  const todo = props.todo || {}
  return ['pendingVisits', 'unfinishedServices', 'unpaidBills', 'keyElders'].reduce(
    (sum, key) => sum + (Array.isArray(todo[key]) ? todo[key].length : 0),
    0
  )
})

function listOf(key) {
  const value = (props.todo || {})[key]
  return Array.isArray(value) ? value : []
}

const VISIT_STATUS = {
  '0': { text: '待确认', tone: 'warning' },
  '1': { text: '已确认', tone: '' },
  '2': { text: '已完成', tone: 'success' },
  '3': { text: '已取消', tone: 'info' }
}

const ORDER_STATUS = {
  '0': { text: '待执行', tone: 'warning' },
  '1': { text: '执行中', tone: '' },
  '2': { text: '已完成', tone: 'success' },
  '3': { text: '已取消', tone: 'info' }
}

function visitText(status) {
  return VISIT_STATUS[String(status)]?.text || '未知'
}

function visitTone(status) {
  return VISIT_STATUS[String(status)]?.tone || 'info'
}

function orderText(status) {
  return ORDER_STATUS[String(status)]?.text || '未知'
}

function orderTone(status) {
  return ORDER_STATUS[String(status)]?.tone || 'info'
}

function careLevelText(level) {
  return { '1': '完全自理', '2': '半自理', '3': '重度失能', '4': '特护' }[String(level)] || '未评估'
}

function billTypeText(type) {
  return { '0': '首期费用', '1': '月度费用', '2': '退住结算' }[String(type)] || '其他费用'
}

function formatMoney(value) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
</script>

<style scoped lang="scss">
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

.todo-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 4px;
  }
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: #f6faf8;
  }
}

.todo-main {
  min-width: 0;
}

.todo-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #8a94a6;
}

:deep(.el-empty) {
  padding: 18px 0;

  .el-empty__description p {
    font-size: 12px;
    color: #a3acb9;
  }
}
</style>
