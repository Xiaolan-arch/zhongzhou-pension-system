<template>
  <el-card shadow="never" class="panel">
    <template #header>
      <div class="panel-header">
        <span class="panel-title">结构分布</span>
        <el-radio-group v-model="tab" size="small">
          <el-radio-button label="care">护理等级</el-radio-button>
          <el-radio-button label="bill">费用类型</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <div ref="chartRef" class="chart" />
  </el-card>
</template>

<script setup name="DistributionChart">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  careLevel: { type: Array, default: () => [] },
  billType: { type: Array, default: () => [] }
})

const chartRef = ref(null)
const tab = ref('care')
let chart = null

const COLORS = ['#2fa36b', '#5bc08d', '#f2a65a', '#4a8ef0', '#7a6ad8', '#f05a5a']

function formatMoney(value) {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function currentRows() {
  const rows = tab.value === 'care' ? props.careLevel : props.billType
  return (rows || []).filter((row) => Number(row.value) > 0)
}

function buildOption() {
  const rows = currentRows()
  const isBill = tab.value === 'bill'
  return {
    color: COLORS,
    tooltip: {
      trigger: 'item',
      formatter: (item) => {
        if (isBill) {
          const amount = rows[item.dataIndex]?.amount
          return `${item.name}<br/>${item.value} 张 · ${formatMoney(amount)}`
        }
        return `${item.name}<br/>${item.value} 人（${item.percent}%）`
      }
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { fontSize: 11, color: '#6b7785' }
    },
    series: [
      {
        type: 'pie',
        radius: ['46%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 4 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 13, fontWeight: 600 } },
        data: rows.map((row) => ({ name: row.name, value: Number(row.value) || 0 }))
      }
    ],
    graphic: rows.length
      ? []
      : [{ type: 'text', left: 'center', top: 'middle', style: { text: '暂无数据', fill: '#a3acb9', fontSize: 13 } }]
  }
}

function render() {
  if (!chart) return
  chart.setOption(buildOption(), true)
}

function resize() {
  if (chart) chart.resize()
}

onMounted(() => {
  chart = echarts.init(chartRef.value)
  render()
  window.addEventListener('resize', resize)
})

watch(tab, render)
watch(() => [props.careLevel, props.billType], render, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
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

.chart {
  width: 100%;
  height: 300px;
}
</style>
