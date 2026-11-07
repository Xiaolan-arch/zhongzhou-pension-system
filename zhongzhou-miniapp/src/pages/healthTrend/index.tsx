import { useEffect, useRef, useState } from 'react'
import { Canvas, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getRecentHealth } from '../../services/family'
import type { HealthRecord } from '../../types'
import styles from './index.module.scss'

interface Metric { key: keyof HealthRecord; label: string; unit: string }

const METRICS: Metric[] = [
  { key: 'bloodPressureHigh', label: '收缩压', unit: 'mmHg' },
  { key: 'bloodPressureLow', label: '舒张压', unit: 'mmHg' },
  { key: 'heartRate', label: '心率', unit: '次/分' },
  { key: 'bodyTemp', label: '体温', unit: '℃' },
  { key: 'bloodSugar', label: '血糖', unit: 'mmol/L' }
]

export default function HealthTrendPage() {
  const canvasRef = useRef<any>(null)
  const [list, setList] = useState<HealthRecord[]>([])
  const metricKey = Taro.getCurrentInstance().router?.params?.metric || 'bloodPressureHigh'
  const elderId = Number(Taro.getCurrentInstance().router?.params?.elderId || 101)

  useEffect(() => {
    const metric = METRICS.find((m) => m.key === metricKey) || METRICS[0]
    getRecentHealth(elderId, 14).then((d) => {
      setList(d || [])
      drawChart(d || [], metric)
    })
  }, [metricKey, elderId])

  const drawChart = (records: HealthRecord[], metric: Metric) => {
    // 取 metric 有值的记录，倒序（时间从旧到新）
    const points: { x: number; y: number; date: string }[] = []
    for (let i = records.length - 1; i >= 0; i--) {
      const v = records[i][metric.key] as number | undefined
      if (v !== undefined && v !== null) {
        points.push({ x: points.length, y: Number(v), date: records[i].recordDate || '' })
      }
    }
    if (points.length < 2) return // 不够画

    Taro.nextTick(() => {
      try {
        const query = Taro.createSelectorQuery()
        query.select('#trendCanvas').fields({ node: true, size: true }).exec((res) => {
          if (!res[0]) return
          const canvas = res[0].node
          const ctx = canvas.getContext('2d')
          const dpr = Taro.getSystemInfoSync().pixelRatio
          canvas.width = res[0].width * dpr
          canvas.height = res[0].height * dpr
          ctx.scale(dpr, dpr)

          const W = res[0].width
          const H = res[0].height
          const padL = 50, padR = 20, padT = 20, padB = 40
          const chartW = W - padL - padR
          const chartH = H - padT - padB

          const values = points.map((p) => p.y)
          const maxV = Math.max(...values)
          const minV = Math.min(...values)
          const range = maxV - minV || 1
          const xStep = chartW / Math.max(1, points.length - 1)

          // 网格
          ctx.strokeStyle = '#e3ebe4'
          ctx.lineWidth = 1
          for (let i = 0; i <= 4; i++) {
            const y = padT + (chartH / 4) * i
            ctx.beginPath(); ctx.moveTo(padL, y); ctx.lineTo(W - padR, y); ctx.stroke()
          }

          // Y 轴标签
          ctx.fillStyle = '#8a94a6'; ctx.font = '10px sans-serif'
          ctx.textAlign = 'right'
          for (let i = 0; i <= 4; i++) {
            const v = maxV - (range / 4) * i
            const y = padT + (chartH / 4) * i
            ctx.fillText(v.toFixed(0), padL - 6, y + 3)
          }

          // 折线
          ctx.strokeStyle = '#2fa36b'; ctx.lineWidth = 2
          ctx.beginPath()
          points.forEach((p, i) => {
            const x = padL + p.x * xStep
            const y = padT + chartH - ((p.y - minV) / range) * chartH
            if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y)
          })
          ctx.stroke()

          // 点
          ctx.fillStyle = '#2fa36b'
          points.forEach((p) => {
            const x = padL + p.x * xStep
            const y = padT + chartH - ((p.y - minV) / range) * chartH
            ctx.beginPath(); ctx.arc(x, y, 3, 0, Math.PI * 2); ctx.fill()
          })
        })
      } catch (e) {
        console.warn('[HealthTrend] draw err', e)
      }
    })
  }

  const metric = METRICS.find((m) => m.key === metricKey) || METRICS[0]

  const tableRows = list
    .slice()
    .reverse()
    .filter((r) => r[metric.key] !== undefined && r[metric.key] !== null)

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.card}>
        <Text className={styles.cardTitle}>{metric.label} 趋势 ({metric.unit})</Text>

        <View className={styles.canvasWrap}>
          <Canvas id='trendCanvas' ref={canvasRef} className={styles.canvas} type='2d' />
        </View>

        {tableRows.length > 0 ? (
          <View className={styles.table}>
            <View className={styles.tableHeader}>
              <Text className={styles.th}>日期</Text>
              <Text className={styles.th}>时间</Text>
              <Text className={styles.th}>{metric.label}</Text>
            </View>
            {tableRows.slice(0, 10).map((r) => (
              <View className={styles.tableRow} key={r.recordId}>
                <Text className={styles.td}>{r.recordDate}</Text>
                <Text className={styles.td}>{r.recordTime || '-'}</Text>
                <Text className={styles.td}>{Number(r[metric.key] as number).toFixed(metric.unit === '℃' || metric.unit === 'mmol/L' ? 1 : 0)}</Text>
              </View>
            ))}
          </View>
        ) : (
          <Text className={styles.empty}>暂无数据</Text>
        )}
      </View>
    </ScrollView>
  )
}
