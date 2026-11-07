import { useCallback, useEffect, useState } from 'react'
import { Button, Text, Textarea, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { createFeedback, getFeedbacks } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { FeedbackRecord } from '../../types'
import { feedbackStatusText, feedbackTypeText, formatDateTime } from '../../utils/format'
import styles from './index.module.scss'

const TYPE_OPTIONS = ['投诉', '建议', '表扬']

function feedbackTone(type: string) {
  if (type === '0') return 'danger' as const
  if (type === '2') return 'success' as const
  return 'primary' as const
}

export default function FeedbackPage() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)

  const [activeId, setActiveId] = useState<number | null>(null)
  const [typeIndex, setTypeIndex] = useState(1)
  const [content, setContent] = useState('')
  const [feedbacks, setFeedbacks] = useState<FeedbackRecord[]>([])
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(false)

  const loadFeedbacks = useCallback(async () => {
    setLoading(true)
    try {
      const data = await getFeedbacks()
      setFeedbacks(data)
    } catch (e) {
      console.error('[Feedback] 加载反馈失败', e)
      setFeedbacks([])
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
      if (list[0]) {
        setActiveId(list[0].elderId)
      }
      await loadFeedbacks()
    } catch (e) {
      console.error('[Feedback] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadFeedbacks])

  useEffect(() => {
    init()
  }, [init])

  useDidShow(() => {
    if (hasAuth()) {
      loadFeedbacks()
    }
  })

  const handleSubmit = async () => {
    if (!content.trim()) {
      Taro.showToast({ title: '请填写反馈内容', icon: 'none' })
      return
    }
    setSubmitting(true)
    try {
      await createFeedback({
        elderId: activeId || undefined,
        feedbackType: String(typeIndex),
        content: content.trim()
      })
      Taro.showToast({ title: '提交成功，机构会尽快处理', icon: 'success' })
      setContent('')
      await loadFeedbacks()
    } catch (e) {
      console.error('[Feedback] 提交失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <View className={styles.pageRoot}>
      <View className={styles.header}>
        <Text className={styles.title}>投诉建议</Text>
        <Text className={styles.subtitle}>您的意见会直达院区管理人员，处理结果将在下方回复</Text>
      </View>

      <View className={styles.formCard}>
        <Text className={styles.formTitle}>提交反馈</Text>

        {elders.length > 1 && (
          <View className={styles.field}>
            <Text className={styles.label}>关联老人（选填）</Text>
            <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={setActiveId} />
          </View>
        )}

        <View className={styles.field}>
          <Text className={styles.label}>反馈类型</Text>
          <View className={styles.typeRow}>
            {TYPE_OPTIONS.map((label, index) => (
              <View
                key={label}
                className={typeIndex === index ? styles.typeItemActive : styles.typeItem}
                onClick={() => setTypeIndex(index)}
              >
                <Text className={styles.typeText}>{label}</Text>
              </View>
            ))}
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>反馈内容</Text>
          <Textarea
            className={styles.textarea}
            maxlength={1000}
            placeholder='请描述具体情况、发生时间或改进建议'
            value={content}
            onInput={(e) => setContent(e.detail.value)}
          />
        </View>

        <Button className={styles.submit} loading={submitting} onClick={handleSubmit}>
          提交反馈
        </Button>
      </View>

      <View className={styles.sectionHeader}>
        <Text className={styles.sectionTitle}>我的反馈</Text>
        <Text className={styles.sectionTip}>{feedbacks.length} 条</Text>
      </View>

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : feedbacks.length === 0 ? (
        <EmptyState text='暂无反馈记录' />
      ) : (
        feedbacks.map((item) => (
          <View key={item.feedbackId} className={styles.record}>
            <View className={styles.recordTop}>
              <View className={styles.typeTag}>
                <StatusTag text={feedbackTypeText(item.feedbackType)} tone={feedbackTone(item.feedbackType)} />
              </View>
              <Text className={styles.recordTime}>{formatDateTime(item.createTime)}</Text>
              <StatusTag
                text={feedbackStatusText(item.status)}
                tone={item.status === '2' ? 'success' : 'warning'}
              />
            </View>
            <Text className={styles.recordContent}>{item.content}</Text>
            {!!item.reply && (
              <View className={styles.replyBox}>
                <Text className={styles.replyLabel}>机构回复</Text>
                <Text className={styles.replyText}>{item.reply}</Text>
              </View>
            )}
          </View>
        ))
      )}
    </View>
  )
}
