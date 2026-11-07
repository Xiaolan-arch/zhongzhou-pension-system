import { useCallback, useRef, useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import classnames from 'classnames'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { getServices } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { ServiceOrder } from '../../types'
import { formatDateTime, formatMoney, orderStatusText } from '../../utils/format'
import styles from './index.module.scss'

const FILTERS = [
  { key: 'all', label: '全部' },
  { key: '0', label: '待执行' },
  { key: '2', label: '已完成' },
  { key: '3', label: '已取消' }
]

function toneOf(status: string) {
  if (status === '2') return 'success' as const
  if (status === '1') return 'primary' as const
  if (status === '0') return 'warning' as const
  return 'gray' as const
}

export default function ServicePage() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)
  const [activeId, setActiveId] = useState<number | null>(null)
  const [records, setRecords] = useState<ServiceOrder[]>([])
  const [filter, setFilter] = useState('all')
  const [loading, setLoading] = useState(false)
  const activeIdRef = useRef<number | null>(null)

  const load = useCallback(async (elderId: number) => {
    setLoading(true)
    try {
      const data = await getServices(elderId)
      setRecords(data)
    } catch (e) {
      console.error('[Service] 加载服务记录失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setRecords([])
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
      if (list.length === 0) {
        setRecords([])
        return
      }
      const current = list.find((item) => item.elderId === activeIdRef.current)?.elderId ?? list[0].elderId
      activeIdRef.current = current
      setActiveId(current)
      await load(current)
    } catch (e) {
      console.error('[Service] 初始化失败', e)
    }
  }, [hydrate, refreshElders, load])

  // 每次页面显示（包括从预约页切换 tab 返回）都重新拉取，避免看到缓存数据
  useDidShow(() => {
    init()
  })

  const switchElder = (elderId: number) => {
    activeIdRef.current = elderId
    setActiveId(elderId)
    load(elderId)
  }

  const shown = filter === 'all' ? records : records.filter((item) => item.status === filter)

  return (
    <View className={styles.pageRoot}>
      <View className={styles.header}>
        <Text className={styles.title}>护理服务记录</Text>
        <Text className={styles.subtitle}>每一次服务，都有记录可查</Text>
      </View>

      <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={switchElder} />

      <ScrollView className={styles.filters} scrollX enableFlex>
        <View className={styles.filterRow}>
          {FILTERS.map((item) => (
            <View
              key={item.key}
              className={classnames(styles.chip, filter === item.key && styles.activeChip)}
              onClick={() => setFilter(item.key)}
            >
              <Text className={classnames(styles.chipText, filter === item.key && styles.activeText)}>
                {item.label}
              </Text>
            </View>
          ))}
        </View>
      </ScrollView>

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : shown.length === 0 ? (
        <EmptyState text='暂无服务记录' />
      ) : (
        shown.map((item) => (
          <View key={item.orderId} className={styles.card}>
            <View className={styles.cardTop}>
              <Text className={styles.itemName}>{item.itemName}</Text>
              <StatusTag text={orderStatusText(item.status)} tone={toneOf(item.status)} />
            </View>
            <View className={styles.row}>
              <Text className={styles.rowLabel}>服务时间</Text>
              <Text className={styles.rowValue}>{formatDateTime(item.serviceTime)}</Text>
            </View>
            <View className={styles.row}>
              <Text className={styles.rowLabel}>护理员</Text>
              <Text className={styles.rowValue}>{item.nurseName || '--'}</Text>
            </View>
            <View className={styles.cardBottom}>
              <Text className={styles.orderNo}>单号 {item.orderNo}</Text>
              <Text className={styles.amount}>{formatMoney(item.amount)}</Text>
            </View>
            {!!item.remark && <Text className={styles.remark}>备注：{item.remark}</Text>}
            {!!item.taskFeedback && (
              <View className={styles.feedback}>
                <Text className={styles.feedbackLabel}>执行反馈</Text>
                <Text className={styles.feedbackText}>{item.taskFeedback}</Text>
              </View>
            )}
            <View
              className={styles.detailLink}
              onClick={() =>
                Taro.navigateTo({
                  url: `/pages/serviceDetail/index?orderId=${item.orderId}&elderId=${item.elderId}`
                })
              }
            >
              <Text className={styles.detailText}>查看服务详情 ›</Text>
            </View>
          </View>
        ))
      )}
    </View>
  )
}
