import { useCallback, useEffect, useState } from 'react'
import { Button, Text, Textarea, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import EmptyState from '../../components/EmptyState'
import { cancelFamilyOrder, getServiceEvaluation, getServices, submitServiceEvaluation } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { ServiceEvaluation, ServiceOrder } from '../../types'
import { formatDateTime, formatMoney, orderStatusText } from '../../utils/format'
import styles from './index.module.scss'

const RATING_OPTIONS = [
  { value: 4, label: '非常满意' },
  { value: 3, label: '满意' },
  { value: 2, label: '一般' },
  { value: 1, label: '需改进' }
]

const MAX_CONTENT = 500

function ratingText(value: number): string {
  const target = RATING_OPTIONS.find((item) => item.value === value)
  return target ? target.label : '未评价'
}

export default function ServiceDetailPage() {
  const router = useRouter()
  const hydrate = useAuthStore((s) => s.hydrate)
  const [order, setOrder] = useState<ServiceOrder | null>(null)
  const [evaluation, setEvaluation] = useState<ServiceEvaluation | null>(null)
  const [canEvaluate, setCanEvaluate] = useState(false)
  const [evalReason, setEvalReason] = useState('')
  const [rating, setRating] = useState(0)
  const [content, setContent] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(true)

  const elderId = Number(router.params.elderId || 0)
  const orderId = Number(router.params.orderId || 0)

  const init = useCallback(async () => {
    hydrate()
    if (!hasAuth()) {
      Taro.redirectTo({ url: '/pages/login/index' })
      return
    }
    try {
      const [list, evalInfo] = await Promise.all([getServices(elderId), getServiceEvaluation(orderId)])
      const target = (list || []).find((item) => item.orderId === orderId)
      setOrder(target || null)
      setEvaluation(evalInfo.evaluation || null)
      setCanEvaluate(!!evalInfo.canEvaluate)
      setEvalReason(evalInfo.reason || '')
    } catch (e) {
      console.error('[ServiceDetail] 加载失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setLoading(false)
    }
  }, [hydrate, elderId, orderId])

  useEffect(() => {
    init()
  }, [init])

  const handleSubmit = async () => {
    if (!rating) {
      Taro.showToast({ title: '请选择服务满意度', icon: 'none' })
      return
    }
    const res = await Taro.showModal({
      title: '提交评价',
      content: '评价提交后不可修改，确认提交吗？',
      confirmText: '确认提交'
    })
    if (!res.confirm) return
    setSubmitting(true)
    try {
      const data = await submitServiceEvaluation(orderId, {
        rating,
        content: content.trim() ? content.trim() : undefined
      })
      setEvaluation(data)
      setCanEvaluate(false)
      setEvalReason('该服务已提交评价')
      Taro.showToast({ title: '评价已提交', icon: 'success' })
    } catch (e) {
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <View className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.title}>服务详情</Text>
        <Text className={styles.subtitle}>服务记录、护理反馈与家属评价</Text>
      </View>

      {loading ? (
        <View className={styles.state}>
          <Text className={styles.stateText}>加载中...</Text>
        </View>
      ) : !order ? (
        <EmptyState text='服务记录不存在' />
      ) : (
        <View>
          <View className={styles.card}>
            <View className={styles.cardTop}>
              <Text className={styles.itemName}>{order.itemName}</Text>
              <Text className={styles.status}>{orderStatusText(order.status)}</Text>
            </View>
            <View className={styles.row}>
              <Text className={styles.rowLabel}>老人</Text>
              <Text className={styles.rowValue}>{order.elderName || '--'}</Text>
            </View>
            <View className={styles.row}>
              <Text className={styles.rowLabel}>服务时间</Text>
              <Text className={styles.rowValue}>{formatDateTime(order.serviceTime)}</Text>
            </View>
            <View className={styles.row}>
              <Text className={styles.rowLabel}>护理员</Text>
              <Text className={styles.rowValue}>{order.nurseName || '--'}</Text>
            </View>
            <View className={styles.row}>
              <Text className={styles.rowLabel}>服务单号</Text>
              <Text className={styles.rowValue}>{order.orderNo}</Text>
            </View>
            <View className={styles.row}>
              <Text className={styles.rowLabel}>服务金额</Text>
              <Text className={styles.amount}>{formatMoney(order.amount)}</Text>
            </View>
          </View>

          {/* 售后与反馈 */}
          <View className={styles.sectionHeader}>
            <Text className={styles.sectionTitle}>售后与反馈</Text>
          </View>
          <View className={styles.card}>
            {order.status === '0' && (
              <View className={styles.afterSaleRow}>
                <View style={{ flex: 1 }}>
                  <Text className={styles.afterSaleTitle}>取消订单</Text>
                  <Text className={styles.afterSaleDesc}>服务尚未开始，可免费取消</Text>
                </View>
                <Button
                  size='mini'
                  className={styles.afterSaleBtnDanger}
                  onClick={() => {
                    Taro.showModal({
                      title: '确认取消订单？',
                      content: '取消后服务将不会执行',
                      success: async (r) => {
                        if (!r.confirm) return
                        try {
                          await cancelFamilyOrder(order.orderId)
                          Taro.showToast({ title: '已取消', icon: 'success' })
                          setTimeout(() => Taro.navigateBack(), 800)
                        } catch (e) {
                          Taro.showToast({ title: (e as Error).message || '取消失败', icon: 'none' })
                        }
                      }
                    })
                  }}
                >
                  取消订单
                </Button>
              </View>
            )}
            <View className={styles.afterSaleRow}>
              <View style={{ flex: 1 }}>
                <Text className={styles.afterSaleTitle}>联系护理站</Text>
                <Text className={styles.afterSaleDesc}>服务问题、售后咨询、紧急事项</Text>
              </View>
              <Button
                size='mini'
                className={styles.afterSaleBtn}
                onClick={() => {
                  Taro.showModal({
                    title: '联系护理站',
                    content: '中州颐养护理站：400-888-6666（工作时间 8:00-20:00），紧急情况请直接拨打。',
                    confirmText: '知道了', showCancel: false
                  })
                }}
              >
                拨打电话
              </Button>
            </View>
          </View>

          <View className={styles.sectionHeader}>
            <Text className={styles.sectionTitle}>护理员执行反馈</Text>
          </View>
          <View className={styles.card}>
            {order.taskFeedback ? (
              <Text className={styles.feedback}>{order.taskFeedback}</Text>
            ) : (
              <Text className={styles.emptyFeedback}>护理员暂未填写本次服务反馈</Text>
            )}
            {!!order.remark && <Text className={styles.remark}>备注：{order.remark}</Text>}
          </View>

          <View className={styles.sectionHeader}>
            <Text className={styles.sectionTitle}>服务评价</Text>
            {evaluation && <Text className={styles.sectionTip}>已评价</Text>}
          </View>

          {evaluation ? (
            <View className={styles.card}>
              <View className={styles.evalTop}>
                <Text className={styles.evalRating}>{ratingText(evaluation.rating)}</Text>
                <Text className={styles.evalTime}>{formatDateTime(evaluation.createTime)}</Text>
              </View>
              <Text className={styles.evalContent}>
                {evaluation.content || '本次未填写评价内容'}
              </Text>
              <Text className={styles.evalTip}>评价提交后不可修改，如需调整请联系护理站。</Text>
            </View>
          ) : canEvaluate ? (
            <View className={styles.card}>
              <Text className={styles.formLabel}>您对本次服务是否满意？</Text>
              <View className={styles.ratingRow}>
                {RATING_OPTIONS.map((item) => (
                  <View
                    key={item.value}
                    className={`${styles.ratingItem} ${rating === item.value ? styles.ratingItemActive : ''}`}
                    onClick={() => setRating(item.value)}
                  >
                    <Text
                      className={`${styles.ratingText} ${rating === item.value ? styles.ratingTextActive : ''}`}
                    >
                      {item.label}
                    </Text>
                  </View>
                ))}
              </View>
              <Text className={styles.formLabel}>评价内容（选填）</Text>
              <Textarea
                className={styles.textarea}
                maxlength={MAX_CONTENT}
                placeholder='可写下对本次服务的建议或感受'
                value={content}
                onInput={(e) => setContent(e.detail.value)}
              />
              <Text className={styles.counter}>
                {content.length}/{MAX_CONTENT}
              </Text>
              <Button
                className={styles.submit}
                loading={submitting}
                disabled={submitting}
                onClick={handleSubmit}
              >
                提交评价
              </Button>
              <Text className={styles.formTip}>评价提交后不可修改，请确认后再提交。</Text>
            </View>
          ) : (
            <View className={styles.card}>
              <Text className={styles.emptyFeedback}>{evalReason || '当前服务暂不可评价'}</Text>
              <Text className={styles.remark}>服务完成后可对护理员的本次服务进行评价。</Text>
            </View>
          )}

          <View className={styles.notice}>
            <Text className={styles.noticeText}>
              评价仅用于服务质量改进，不会影响护理员的正常服务。如对服务有疑问，请联系护理站。
            </Text>
          </View>
        </View>
      )}
    </View>
  )
}
