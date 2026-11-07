import { useCallback, useEffect, useState } from 'react'
import { Text, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import classnames from 'classnames'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { getReports } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { HealthReport } from '../../types'
import { formatDate, formatDateTime, reportTypeText, riskLevelText } from '../../utils/format'
import styles from './index.module.scss'

function riskTone(risk: string) {
  if (risk === '0') return 'success' as const
  if (risk === '1') return 'warning' as const
  return 'danger' as const
}

export default function ReportPage() {
  const router = useRouter()
  const paramElderId = Number(router.params.elderId || 0)
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)
  const [activeId, setActiveId] = useState<number | null>(null)
  const [reports, setReports] = useState<HealthReport[]>([])
  const [loading, setLoading] = useState(false)

  const load = useCallback(async (elderId: number) => {
    setLoading(true)
    try {
      const data = await getReports(elderId)
      setReports(data)
    } catch (e) {
      console.error('[Report] 加载健康报告失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setReports([])
    } finally {
      setLoading(false)
    }
  }, [])

  const init = useCallback(async () => {
    hydrate()
    if (!hasAuth()) {
      Taro.redirectTo({ url: '/pages/login/index' })
      return
    }
    try {
      const list = await refreshElders()
      const current = list.find((item) => item.elderId === paramElderId) || list[0]
      if (current) {
        setActiveId(current.elderId)
        await load(current.elderId)
      }
    } catch (e) {
      console.error('[Report] 初始化失败', e)
    }
  }, [hydrate, refreshElders, load, paramElderId])

  useEffect(() => {
    init()
  }, [init])

  const switchElder = (elderId: number) => {
    setActiveId(elderId)
    load(elderId)
  }

  const latest = reports[0]

  return (
    <View className={styles.pageRoot}>
      <View className={styles.header}>
        <Text className={styles.title}>AI 健康报告</Text>
        <Text className={styles.subtitle}>基于照护记录生成的阶段评估，长期保存可随时查看</Text>
      </View>

      <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={switchElder} />

      {reports.length > 0 && (
        <View className={styles.summary}>
          <View className={styles.summaryItem}>
            <Text className={styles.summaryValue}>{reports.length}</Text>
            <Text className={styles.summaryLabel}>累计报告</Text>
          </View>
          <View className={styles.summaryDivider} />
          <View className={styles.summaryItem}>
            <Text className={styles.summaryValue}>{latest ? latest.score : '--'}</Text>
            <Text className={styles.summaryLabel}>最新评分</Text>
          </View>
          <View className={styles.summaryDivider} />
          <View className={styles.summaryItem}>
            <StatusTag
              text={latest ? riskLevelText(latest.riskLevel) : '暂无'}
              tone={latest ? riskTone(latest.riskLevel) : 'gray'}
            />
            <Text className={styles.summaryLabel}>综合状态</Text>
          </View>
        </View>
      )}

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : reports.length === 0 ? (
        <EmptyState text='暂无健康报告，报告生成后即可查看' />
      ) : (
        reports.map((report) => (
          <View
            key={report.reportId}
            className={styles.card}
            onClick={() => Taro.navigateTo({ url: `/pages/reportDetail/index?reportId=${report.reportId}` })}
          >
            <View
              className={classnames(
                styles.scoreBadge,
                report.riskLevel === '0' && styles.scoreGood,
                report.riskLevel === '1' && styles.scoreWatch,
                report.riskLevel === '2' && styles.scoreWarn
              )}
            >
              <Text className={styles.scoreValue}>{report.score}</Text>
              <Text className={styles.scoreUnit}>分</Text>
            </View>
            <View className={styles.cardInfo}>
              <Text className={styles.cardTitle}>{report.reportTitle}</Text>
              <Text className={styles.cardMeta}>
                {reportTypeText(report.reportType)} · {formatDate(report.periodStart)} 至 {formatDate(report.periodEnd)}
              </Text>
              <View className={styles.cardBottom}>
                <StatusTag text={riskLevelText(report.riskLevel)} tone={riskTone(report.riskLevel)} />
                <Text className={styles.cardTime}>{formatDateTime(report.createTime)}</Text>
              </View>
            </View>
            <Text className={styles.arrow}>›</Text>
          </View>
        ))
      )}

      <Text className={styles.tip}>报告由 AI 辅助生成，仅供家属了解照护情况，不作为医疗诊断依据。</Text>
    </View>
  )
}
