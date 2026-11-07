import { useCallback, useEffect, useState } from 'react'
import { Button, Picker, Text, Textarea, View } from '@tarojs/components'
import Taro, { useDidShow, useRouter } from '@tarojs/taro'
import classnames from 'classnames'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { cancelVisit, createVisit, getVisits } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { Visit } from '../../types'
import { formatDate, formatDateTime, visitStatusText } from '../../utils/format'
import styles from './index.module.scss'

const MAX_PEOPLE = 5

function todayStr() {
  const now = new Date()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function visitTone(status: string) {
  if (status === '1') return 'primary' as const
  if (status === '2') return 'success' as const
  if (status === '0') return 'warning' as const
  return 'gray' as const
}

export default function VisitPage() {
  const router = useRouter()
  const paramElderId = Number(router.params.elderId || 0)
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)
  const [activeId, setActiveId] = useState<number | null>(null)
  const [visits, setVisits] = useState<Visit[]>([])
  const [visitDate, setVisitDate] = useState(todayStr())
  const [visitTime, setVisitTime] = useState('09:00')
  const [peopleCount, setPeopleCount] = useState(1)
  const [remark, setRemark] = useState('')
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const loadVisits = useCallback(async () => {
    setLoading(true)
    try {
      const data = await getVisits()
      setVisits(data)
    } catch (e) {
      console.error('[Visit] 加载预约记录失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setVisits([])
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
      }
      await loadVisits()
    } catch (e) {
      console.error('[Visit] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadVisits, paramElderId])

  useEffect(() => {
    init()
  }, [init])

  useDidShow(() => {
    if (hasAuth()) {
      loadVisits()
    }
  })

  const changePeople = (delta: number) => {
    setPeopleCount((count) => Math.min(MAX_PEOPLE, Math.max(1, count + delta)))
  }

  const handleSubmit = async () => {
    if (!activeId) {
      Taro.showToast({ title: '请选择探访老人', icon: 'none' })
      return
    }
    if (!visitDate) {
      Taro.showToast({ title: '请选择探访日期', icon: 'none' })
      return
    }
    if (!visitTime) {
      Taro.showToast({ title: '请选择探访时间', icon: 'none' })
      return
    }
    if (peopleCount < 1 || peopleCount > MAX_PEOPLE) {
      Taro.showToast({ title: `探访人数需在 1-${MAX_PEOPLE} 人之间`, icon: 'none' })
      return
    }
    setSubmitting(true)
    try {
      await createVisit({
        elderId: activeId,
        visitDate,
        visitTime,
        peopleCount,
        remark: remark.trim() || undefined
      })
      Taro.showToast({ title: '预约已提交，等待机构确认', icon: 'success' })
      setPeopleCount(1)
      setRemark('')
      await loadVisits()
    } catch (e) {
      console.error('[Visit] 提交预约失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleCancel = async (visit: Visit) => {
    const res = await Taro.showModal({
      title: '取消预约',
      content: `确定取消 ${formatDate(visit.visitDate)} ${visit.visitTime} 的探访预约吗？`,
      confirmText: '取消预约',
      confirmColor: '#f05a5a'
    })
    if (!res.confirm) return
    try {
      await cancelVisit(visit.visitId)
      Taro.showToast({ title: '预约已取消', icon: 'success' })
      await loadVisits()
    } catch (e) {
      console.error('[Visit] 取消预约失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    }
  }

  return (
    <View className={styles.pageRoot}>
      <View className={styles.header}>
        <Text className={styles.title}>探访预约</Text>
        <Text className={styles.subtitle}>提前预约到院探访，机构确认后即可前往</Text>
      </View>

      <View className={styles.formCard}>
        <Text className={styles.formTitle}>新建预约</Text>

        {elders.length > 1 && (
          <View className={styles.elderPicker}>
            <Text className={styles.label}>探访老人</Text>
            <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={setActiveId} />
          </View>
        )}
        {elders.length === 1 && (
          <View className={styles.field}>
            <Text className={styles.label}>探访老人</Text>
            <Text className={styles.fixedValue}>{elders[0].elderName}</Text>
          </View>
        )}
        {elders.length === 0 && (
          <View className={styles.field}>
            <Text className={styles.label}>探访老人</Text>
            <Text className={styles.placeholder}>暂无绑定老人</Text>
          </View>
        )}

        <View className={styles.field}>
          <Text className={styles.label}>探访日期</Text>
          <Picker mode='date' start={todayStr()} value={visitDate} onChange={(e) => setVisitDate(e.detail.value)}>
            <View className={styles.pickerBox}>
              <Text className={styles.pickerValue}>{visitDate}</Text>
              <Text className={styles.pickerArrow}>›</Text>
            </View>
          </Picker>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>到院时间</Text>
          <Picker mode='time' value={visitTime} onChange={(e) => setVisitTime(e.detail.value)}>
            <View className={styles.pickerBox}>
              <Text className={styles.pickerValue}>{visitTime}</Text>
              <Text className={styles.pickerArrow}>›</Text>
            </View>
          </Picker>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>来访人数</Text>
          <View className={styles.stepper}>
            <View
              className={classnames(styles.stepButton, peopleCount <= 1 && styles.stepDisabled)}
              onClick={() => changePeople(-1)}
            >
              <Text className={styles.stepText}>-</Text>
            </View>
            <Text className={styles.stepValue}>{peopleCount}</Text>
            <View
              className={classnames(styles.stepButton, peopleCount >= MAX_PEOPLE && styles.stepDisabled)}
              onClick={() => changePeople(1)}
            >
              <Text className={styles.stepText}>+</Text>
            </View>
            <Text className={styles.stepTip}>最多 {MAX_PEOPLE} 人（含本人）</Text>
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>备注（选填）</Text>
          <Textarea
            className={styles.textarea}
            maxlength={100}
            placeholder='如携带物品、特殊需求等，可在此说明'
            value={remark}
            onInput={(e) => setRemark(e.detail.value)}
          />
        </View>

        <Button className={styles.submit} loading={submitting} disabled={elders.length === 0} onClick={handleSubmit}>
          提交预约
        </Button>
        <Text className={styles.formTip}>提交后由院区工作人员确认，探访当天请配合门岗登记。</Text>
      </View>

      <View className={styles.sectionHeader}>
        <Text className={styles.sectionTitle}>我的预约</Text>
        <Text className={styles.sectionTip}>{visits.length} 条记录</Text>
      </View>

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : visits.length === 0 ? (
        <EmptyState text='暂无探访预约记录' />
      ) : (
        visits.map((visit) => (
          <View key={visit.visitId} className={styles.record}>
            <View className={styles.recordTop}>
              <View className={styles.dateBox}>
                <Text className={styles.dateText}>{formatDate(visit.visitDate)}</Text>
                <Text className={styles.timeText}>{visit.visitTime}</Text>
              </View>
              <View className={styles.recordInfo}>
                <Text className={styles.recordElder}>探访 {visit.elderName || '老人'}</Text>
                <Text className={styles.recordMeta}>
                  {visit.peopleCount} 人 · {visit.visitorName || '家属'}
                </Text>
              </View>
              <StatusTag text={visitStatusText(visit.status)} tone={visitTone(visit.status)} />
            </View>
            {!!visit.visitCode && visit.status !== '3' && (
              <View className={styles.codeBox}>
                <Text className={styles.codeLabel}>到院出示预约码</Text>
                <Text className={styles.codeValue}>{visit.visitCode}</Text>
                <Text className={styles.codeTip}>
                  {visit.checkinTime ? `已于 ${formatDateTime(visit.checkinTime)} 核销` : '请于探访当天向门岗出示'}
                </Text>
              </View>
            )}
            {!!visit.remark && <Text className={styles.recordRemark}>备注：{visit.remark}</Text>}
            {(visit.status === '0' || visit.status === '1') && (
              <View className={styles.recordBottom}>
                <Text className={styles.cancel} onClick={() => handleCancel(visit)}>
                  取消预约
                </Text>
              </View>
            )}
          </View>
        ))
      )}
    </View>
  )
}
