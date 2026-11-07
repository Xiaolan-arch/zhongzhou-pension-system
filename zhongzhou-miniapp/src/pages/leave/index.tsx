import { useCallback, useEffect, useState } from 'react'
import { Button, Input, Picker, Text, Textarea, View } from '@tarojs/components'
import Taro, { useDidShow, useRouter } from '@tarojs/taro'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { cancelLeave, createLeave, getLeaves } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { LeaveRecord } from '../../types'
import { formatDateTime, leaveStatusText, leaveTypeText } from '../../utils/format'
import styles from './index.module.scss'

const TYPE_OPTIONS = ['请假', '外出就医', '回家居住']

function todayStr() {
  const now = new Date()
  const month = `${now.getMonth() + 1}`.padStart(2, '0')
  const day = `${now.getDate()}`.padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

function leaveTone(status: string) {
  if (status === '1') return 'success' as const
  if (status === '2') return 'danger' as const
  if (status === '0') return 'warning' as const
  return 'gray' as const
}

export default function LeavePage() {
  const router = useRouter()
  const paramElderId = Number(router.params.elderId || 0)
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)

  const [activeId, setActiveId] = useState<number | null>(null)
  const [leaves, setLeaves] = useState<LeaveRecord[]>([])
  const [typeIndex, setTypeIndex] = useState(0)
  const [startDate, setStartDate] = useState(todayStr())
  const [startTime, setStartTime] = useState('09:00')
  const [endDate, setEndDate] = useState(todayStr())
  const [endTime, setEndTime] = useState('18:00')
  const [reason, setReason] = useState('')
  const [contactPhone, setContactPhone] = useState('')
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const loadLeaves = useCallback(async (elderId: number) => {
    setLoading(true)
    try {
      const data = await getLeaves(elderId)
      setLeaves(data)
    } catch (e) {
      console.error('[Leave] 加载请假记录失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setLeaves([])
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
        await loadLeaves(current.elderId)
      }
    } catch (e) {
      console.error('[Leave] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadLeaves, paramElderId])

  useEffect(() => {
    init()
  }, [init])

  useDidShow(() => {
    if (activeId && hasAuth()) {
      loadLeaves(activeId)
    }
  })

  const handleElderChange = (id: number) => {
    setActiveId(id)
    loadLeaves(id)
  }

  const handleSubmit = async () => {
    if (!activeId) {
      Taro.showToast({ title: '请选择老人', icon: 'none' })
      return
    }
    const start = `${startDate} ${startTime}:00`
    const end = `${endDate} ${endTime}:00`
    if (end <= start) {
      Taro.showToast({ title: '预计返回时间需晚于外出时间', icon: 'none' })
      return
    }
    if (!reason.trim()) {
      Taro.showToast({ title: '请填写请假事由', icon: 'none' })
      return
    }
    setSubmitting(true)
    try {
      await createLeave({
        elderId: activeId,
        leaveType: String(typeIndex),
        startTime: start,
        endTime: end,
        reason: reason.trim(),
        contactPhone: contactPhone.trim() || undefined
      })
      Taro.showToast({ title: '申请已提交，等待审批', icon: 'success' })
      setReason('')
      await loadLeaves(activeId)
    } catch (e) {
      console.error('[Leave] 提交申请失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleCancel = async (item: LeaveRecord) => {
    const res = await Taro.showModal({
      title: '撤销申请',
      content: '确定撤销这条待审批的请假申请吗？',
      confirmText: '确定撤销',
      confirmColor: '#f05a5a'
    })
    if (!res.confirm) return
    try {
      await cancelLeave(item.leaveId)
      Taro.showToast({ title: '已撤销', icon: 'success' })
      if (activeId) await loadLeaves(activeId)
    } catch (e) {
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    }
  }

  return (
    <View className={styles.pageRoot}>
      <View className={styles.header}>
        <Text className={styles.title}>请假外出</Text>
        <Text className={styles.subtitle}>老人离院需家属申请并由机构审批，审批通过后床位自动标记外出</Text>
      </View>

      <View className={styles.formCard}>
        <Text className={styles.formTitle}>新建申请</Text>

        <View className={styles.field}>
          <Text className={styles.label}>老人</Text>
          {elders.length > 1 ? (
            <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={handleElderChange} />
          ) : (
            <Text className={styles.fixedValue}>{elders[0]?.elderName || '暂无绑定老人'}</Text>
          )}
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>请假类型</Text>
          <Picker
            mode='selector'
            range={TYPE_OPTIONS}
            value={typeIndex}
            onChange={(e) => setTypeIndex(Number(e.detail.value))}
          >
            <View className={styles.pickerBox}>
              <Text className={styles.pickerValue}>{TYPE_OPTIONS[typeIndex]}</Text>
              <Text className={styles.pickerArrow}>›</Text>
            </View>
          </Picker>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>外出时间</Text>
          <View className={styles.rowFields}>
            <Picker mode='date' value={startDate} onChange={(e) => setStartDate(e.detail.value)}>
              <View className={styles.pickerBox}>
                <Text className={styles.pickerValue}>{startDate}</Text>
              </View>
            </Picker>
            <Picker mode='time' value={startTime} onChange={(e) => setStartTime(e.detail.value)}>
              <View className={styles.pickerBox}>
                <Text className={styles.pickerValue}>{startTime}</Text>
              </View>
            </Picker>
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>预计返回</Text>
          <View className={styles.rowFields}>
            <Picker mode='date' value={endDate} onChange={(e) => setEndDate(e.detail.value)}>
              <View className={styles.pickerBox}>
                <Text className={styles.pickerValue}>{endDate}</Text>
              </View>
            </Picker>
            <Picker mode='time' value={endTime} onChange={(e) => setEndTime(e.detail.value)}>
              <View className={styles.pickerBox}>
                <Text className={styles.pickerValue}>{endTime}</Text>
              </View>
            </Picker>
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>请假事由</Text>
          <Textarea
            className={styles.textarea}
            maxlength={500}
            placeholder='请填写请假原因、去向等信息'
            value={reason}
            onInput={(e) => setReason(e.detail.value)}
          />
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>外出联系人电话（选填）</Text>
          <View className={styles.pickerBox}>
            <Input
              className={styles.input}
              type='number'
              maxlength={11}
              placeholder='便于机构紧急联系'
              value={contactPhone}
              onInput={(e) => setContactPhone(e.detail.value)}
            />
          </View>
        </View>

        <Button className={styles.submit} loading={submitting} disabled={elders.length === 0} onClick={handleSubmit}>
          提交申请
        </Button>
        <Text className={styles.formTip}>审批通过后老人床位将标记为「外出」，返院后由机构销假恢复。</Text>
      </View>

      <View className={styles.sectionHeader}>
        <Text className={styles.sectionTitle}>申请记录</Text>
        <Text className={styles.sectionTip}>{leaves.length} 条</Text>
      </View>

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : leaves.length === 0 ? (
        <EmptyState text='暂无请假外出记录' />
      ) : (
        leaves.map((item) => (
          <View key={item.leaveId} className={styles.record}>
            <View className={styles.recordTop}>
              <View className={styles.recordInfo}>
                <Text className={styles.recordElder}>{leaveTypeText(item.leaveType)}</Text>
                <Text className={styles.recordMeta}>
                  {formatDateTime(item.startTime)} 至 {formatDateTime(item.endTime)}
                </Text>
              </View>
              <StatusTag text={leaveStatusText(item.status)} tone={leaveTone(item.status)} />
            </View>
            {!!item.reason && <Text className={styles.recordRemark}>事由：{item.reason}</Text>}
            {!!item.auditOpinion && <Text className={styles.auditText}>审批意见：{item.auditOpinion}</Text>}
            {item.status === '0' && (
              <View className={styles.recordBottom}>
                <Text className={styles.cancel} onClick={() => handleCancel(item)}>
                  撤销申请
                </Text>
              </View>
            )}
          </View>
        ))
      )}
    </View>
  )
}
