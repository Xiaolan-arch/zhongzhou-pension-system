<template>
  <el-card shadow="never" class="panel">
    <template #header>
      <div class="panel-header">
        <span class="panel-title">经营趋势</span>
        <el-radio-group :model-value="months" size="small" @change="handleRange">
          <el-radio-button :label="6">近 6 月</el-radio-button>
          <el-radio-button :label="12">近 12 月</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <div ref="chartRef" class="chart" />
    <div class="panel-footnote">收入按实际缴费时间统计，服务仅统计已完成订单</div>
  </el-card>
</template>

<script setup name="TrendChart">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] },
  months: { type: Number, default: 6 }
})

const emit = defineEmits(['change'])
const chartRef = ref(null)
let chart = null

function buildOption() {
  const rows = props.data || []
  return {
    color: ['#2fa36b', '#f2a65a', '#4a8ef0', '#94a3b8'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12, color: '#6b7785' } },
    grid: { left: 6, right: 10, top: 24, bottom: 46, containLabel: true },
    xAxis: {
      type: 'category',
      data: rows.map((row) => row.label),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e8ecf0' } },
      axisLabel: { fontSize: 11, color: '#8a94a6' }
    },
    yAxis: [
      {
        type: 'value',
        name: '人次',
        nameTextStyle: { fontSize: 11, color: '#8a94a6' },
        splitLine: { lineStyle: { type: 'dashed', color: '#eef1f0' } },
        axisLabel: { fontSize: 10, color: '#8a94a6' }
      },
      {
        type: 'value',
        name: '收入',
        nameTextStyle: { fontSize: 11, color: '#8a94a6' },
        splitLine: { show: false },
        axisLabel: {
          fontSize: 10,
          color: '#8a94a6',
          formatter: (value) => (value >= 10000 ? `${Math.round(value / 10000)}万` : value)
        }
      }
    ],
    series: [
      {
        name: '入住',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data: rows.map((row) => row.checkin || 0)
      },
      {
        name: '退住',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data: rows.map((row) => row.checkout || 0)
      },
      {
        name: '服务完成',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        data: rows.map((row) => row.serviceDone || 0)
      },
      {
        name: '收入',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 14,
        itemStyle: { borderRadius: [4, 4, 0, 0], opacity: 0.75 },
        data: rows.map((row) => Number(row.income || 0))
      }
    ]
  }
}

function render() {
  if (!chart) return
  chart.setOption(buildOption(), true)
}

function handleRange(value) {
  emit('change', value)
}

function resize() {
  if (chart) chart.resize()
}

onMounted(() => {
  chart = echarts.init(chartRef.value)
  render()
  window.addEventListener('resize', resize)
})

watch(() => props.data, render, { deep: true })

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

.panel-footnote {
  margin-top: 4px;
  font-size: 12px;
  color: #a3acb9;
  text-align: right;
}
</style>
