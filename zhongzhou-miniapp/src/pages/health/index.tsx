import { useEffect, useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getRecentHealth } from '../../services/family'
import { useAuthStore } from '../../store/auth'
import type { HealthRecord } from '../../types'
import styles from './index.module.scss'

interface Metric { key: keyof HealthRecord; label: string; unit: string }

const METRICS: Metric[] = [
  { key: 'bloodPressureHigh', label: '收缩压', unit: 'mmHg' },
  { key: 'bloodPressureLow', label: '舒张压', unit: 'mmHg' },
  { key: 'heartRate', label: '心率', unit: '次/分' },
  { key: 'bodyTemp', label: '体温', unit: '℃' },
  { key: 'bloodSugar', label: '血糖', unit: 'mmol/L' },
  { key: 'bloodOxygen', label: '血氧', unit: '%' },
  { key: 'weight', label: '体重', unit: 'kg' },
  { key: 'steps', label: '步数', unit: '步' }
]

export default function HealthPage() {
  const [list, setList] = useState<HealthRecord[]>([])
  const [loading, setLoading] = useState(true)
  const storeElders = useAuthStore((s) => s.elders)
  const paramElderId = Number(Taro.getCurrentInstance().router?.params?.elderId || 0)
  const elderId = paramElderId || storeElders[0]?.elderId || 0

  useEffect(() => {
    if (!elderId) {
      setLoading(false)
      return
    }
    getRecentHealth(elderId, 14).then((d) => { setList(d || []); setLoading(false) })
      .catch((e) => { setLoading(false); Taro.showToast({ title: e.message || '加载失败', icon: 'none' }) })
  }, [elderId])

  // 取每个指标的最近值
  const latestMap = new Map<string, number>()
  for (const rec of list) {
    for (const m of METRICS) {
      const v = rec[m.key] as number | undefined
      if (v !== undefined && v !== null && !latestMap.has(m.key)) {
        latestMap.set(m.key, v)
      }
    }
  }
  const isAbnormal = list[0]?.isAbnormal === '1'

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.headerTitle}>体征数据</Text>
        <Text className={styles.headerDesc}>最近14天测量记录</Text>
      </View>

      {loading && <Text className={styles.loading}>加载中…</Text>}
      {!loading && !elderId && <Text className={styles.empty}>请先登录并绑定老人后查看体征数据</Text>}
      {!loading && elderId > 0 && list.length === 0 && <Text className={styles.empty}>暂无体征记录</Text>}

      {list.length > 0 && (
        <View className={styles.grid}>
          {METRICS.map((m) => {
            const v = latestMap.get(m.key)
            const abnormal = isAbnormal
            const cls = v === undefined ? styles.cardValueEmpty : abnormal ? styles.cardValueAbnormal : styles.cardValueNormal
            const trendCls = abnormal ? styles.cardTrendAbnormal : styles.cardTrend
            return (
              <View key={m.key} className={styles.card}>
                <Text className={styles.cardLabel}>{m.label}</Text>
                <View>
                  <Text className={`${styles.cardValue} ${cls}`}>
                    {v === undefined ? '--' : typeof v === 'number' && m.unit !== 'mmHg' && m.unit !== '次/分' && m.unit !== '步' ? v.toFixed(1) : v}
                  </Text>
                  <Text className={styles.cardUnit}>{m.unit}</Text>
                </View>
                <Text className={trendCls}>{abnormal ? '⚠ 异常' : '✓ 正常'}</Text>
              </View>
            )
          })}
        </View>
      )}

      {list.length > 0 && (
        <View className={styles.meta}>
          最近测量 {list[0]?.recordDate}{list[0]?.recordTime ? ' ' + list[0].recordTime : ''}
        </View>
      )}
    </ScrollView>
  )
}
