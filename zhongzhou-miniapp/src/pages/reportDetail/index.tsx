import { useEffect, useState } from 'react'
import { Button, Text, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import StatusTag from '../../components/StatusTag'
import { getReportDetail } from '../../services/family'
import type { HealthReport } from '../../types'
import { formatDate, formatDateTime, reportTypeText, riskLevelText } from '../../utils/format'
import { parseMarkdown } from '../../utils/markdown'
import styles from './index.module.scss'

function modelSourceText(value?: string) {
  if (value === 'qianfan') return '千帆大模型'
  if (value === 'mock') return '本地规则模版'
  return value || '--'
}

export default function ReportDetailPage() {
  const router = useRouter()
  const reportId = Number(router.params.reportId || 0)
  const [report, setReport] = useState<HealthReport | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const load = async () => {
    if (!reportId) {
      setError('缺少报告参数')
      setLoading(false)
      return
    }
    setLoading(true)
    setError('')
    try {
      const data = await getReportDetail(reportId)
      setReport(data)
      Taro.setNavigationBarTitle({ title: data.reportTitle || '报告详情' })
    } catch (e) {
      console.error('[ReportDetail] 加载报告失败', e)
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reportId])

  if (loading) {
    return (
      <View className={styles.state}>
        <Text className={styles.stateText}>报告加载中...</Text>
      </View>
    )
  }

  if (!report) {
    return (
      <View className={styles.state}>
        <Text className={styles.stateText}>{error || '报告不存在'}</Text>
        <Button className={styles.backButton} onClick={() => Taro.navigateBack()}>
          返回
        </Button>
      </View>
    )
  }

  const blocks = parseMarkdown(report.content || report.summary)
  const tone = report.riskLevel === '0' ? 'success' : report.riskLevel === '1' ? 'warning' : 'danger'

  return (
    <View className={styles.page}>
      <View className={styles.hero}>
        <View className={styles.heroTop}>
          <Text className={styles.heroTitle}>{report.reportTitle}</Text>
          <StatusTag text={riskLevelText(report.riskLevel)} tone={tone} />
        </View>
        <View className={styles.heroMain}>
          <View className={styles.scoreBox}>
            <Text className={styles.scoreValue}>{report.score}</Text>
            <Text className={styles.scoreUnit}>综合评分</Text>
          </View>
          <View className={styles.heroInfo}>
            <Text className={styles.elderName}>{report.elderName}</Text>
            <Text className={styles.heroMeta}>
              {reportTypeText(report.reportType)} · {formatDate(report.periodStart)} 至{' '}
              {formatDate(report.periodEnd)}
            </Text>
          </View>
        </View>
      </View>

      <View className={styles.content}>
        {!!report.summary && (
          <View className={styles.summaryCard}>
            <Text className={styles.summaryTitle}>评估摘要</Text>
            <Text className={styles.summaryText}>{report.summary}</Text>
          </View>
        )}

        <View className={styles.card}>
          <Text className={styles.cardTitle}>报告正文</Text>
          {blocks.length === 0 ? (
            <Text className={styles.emptyText}>报告暂无正文内容</Text>
          ) : (
            blocks.map((block, index) => {
              if (block.type === 'hr') {
                return <View key={`hr-${index}`} className={styles.divider} />
              }
              if (block.type === 'h2') {
                return (
                  <View key={`h2-${index}`} className={styles.heading}>
                    <View className={styles.headingBar} />
                    <Text className={styles.headingText}>{block.text}</Text>
                  </View>
                )
              }
              if (block.type === 'h3') {
                return (
                  <Text key={`h3-${index}`} className={styles.subHeading}>
                    {block.text}
                  </Text>
                )
              }
              if (block.type === 'li') {
                return (
                  <View key={`li-${index}`} className={styles.listItem}>
                    <View className={styles.dot} />
                    <Text className={styles.listText}>{block.text}</Text>
                  </View>
                )
              }
              if (block.type === 'quote') {
                return (
                  <View key={`quote-${index}`} className={styles.quote}>
                    <Text className={styles.quoteText}>{block.text}</Text>
                  </View>
                )
              }
              return (
                <Text key={`p-${index}`} className={styles.paragraph}>
                  {block.text}
                </Text>
              )
            })
          )}
        </View>

        <View className={styles.card}>
          <Text className={styles.cardTitle}>报告信息</Text>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>报告类型</Text>
            <Text className={styles.rowValue}>{reportTypeText(report.reportType)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>统计周期</Text>
            <Text className={styles.rowValue}>
              {formatDate(report.periodStart)} 至 {formatDate(report.periodEnd)}
            </Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>生成时间</Text>
            <Text className={styles.rowValue}>{formatDateTime(report.createTime)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>评估模型</Text>
            <Text className={styles.rowValue}>{modelSourceText(report.modelSource)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>风险等级</Text>
            <Text className={styles.rowValue}>{riskLevelText(report.riskLevel)}</Text>
          </View>
        </View>

        {report.modelSource === 'mock' && (
          <Text className={styles.tip}>
            演示环境：本报告由本地规则模版生成，未调用真实大模型服务。
          </Text>
        )}

        <Text className={styles.tip}>
          本报告由 AI 辅助生成，基于老人照护记录与健康档案整理，仅供家属了解照护情况，不作为医疗诊断依据。
        </Text>
      </View>
    </View>
  )
}
