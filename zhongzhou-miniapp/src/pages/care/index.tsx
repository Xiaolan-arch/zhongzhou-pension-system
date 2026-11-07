import { useCallback, useEffect, useState } from 'react'
import { Text, View } from '@tarojs/components'
import Taro, { useDidShow, useRouter } from '@tarojs/taro'
import classnames from 'classnames'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { getCareLogs, getMedications } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { CareLog, Medication } from '../../types'
import { careTypeText, formatDate } from '../../utils/format'
import styles from './index.module.scss'

type TabKey = 'medication' | 'care'

export default function CarePage() {
  const router = useRouter()
  const paramElderId = Number(router.params.elderId || 0)
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)

  const [activeId, setActiveId] = useState<number | null>(null)
  const [tab, setTab] = useState<TabKey>('medication')
  const [medications, setMedications] = useState<Medication[]>([])
  const [careLogs, setCareLogs] = useState<CareLog[]>([])
  const [loading, setLoading] = useState(false)

  const loadData = useCallback(async (elderId: number) => {
    setLoading(true)
    try {
      const [meds, logs] = await Promise.all([getMedications(elderId), getCareLogs(elderId)])
      setMedications(meds)
      setCareLogs(logs)
    } catch (e) {
      console.error('[Care] 加载照护数据失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setMedications([])
      setCareLogs([])
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
        await loadData(current.elderId)
      }
    } catch (e) {
      console.error('[Care] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadData, paramElderId])

  useEffect(() => {
    init()
  }, [init])

  useDidShow(() => {
    if (activeId && hasAuth()) {
      loadData(activeId)
    }
  })

  const handleElderChange = (id: number) => {
    setActiveId(id)
    loadData(id)
  }

  const ongoing = medications.filter((item) => item.status === '0').length

  return (
    <View className={styles.pageRoot}>
      <View className={styles.header}>
        <Text className={styles.title}>照护记录</Text>
        <Text className={styles.subtitle}>查看老人的用药提醒与护理员每日记录</Text>
      </View>

      {elders.length > 1 && (
        <View className={styles.elderBar}>
          <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={handleElderChange} />
        </View>
      )}

      <View className={styles.tabs}>
        <View
          className={classnames(styles.tab, tab === 'medication' && styles.tabActive)}
          onClick={() => setTab('medication')}
        >
          <Text className={styles.tabText}>用药提醒</Text>
          {ongoing > 0 && <Text className={styles.badge}>{ongoing}</Text>}
        </View>
        <View
          className={classnames(styles.tab, tab === 'care' && styles.tabActive)}
          onClick={() => setTab('care')}
        >
          <Text className={styles.tabText}>护理日志</Text>
        </View>
      </View>

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : tab === 'medication' ? (
        medications.length === 0 ? (
          <EmptyState text='暂无用药提醒' />
        ) : (
          medications.map((item) => (
            <View key={item.medicationId} className={styles.card}>
              <View className={styles.cardTop}>
                <Text className={styles.cardTitle}>{item.medicineName}</Text>
                <StatusTag
                  text={item.status === '0' ? '进行中' : '已停用'}
                  tone={item.status === '0' ? 'success' : 'gray'}
                />
              </View>
              <View className={styles.metaRow}>
                <Text className={styles.metaLabel}>用量</Text>
                <Text className={styles.metaValue}>{item.dosage || '遵医嘱'}</Text>
              </View>
              <View className={styles.metaRow}>
                <Text className={styles.metaLabel}>频次</Text>
                <Text className={styles.metaValue}>{item.frequency || '-'}</Text>
              </View>
              <View className={styles.metaRow}>
                <Text className={styles.metaLabel}>时间</Text>
                <Text className={styles.metaValue}>{item.timePoints || '-'}</Text>
              </View>
              <View className={styles.metaRow}>
                <Text className={styles.metaLabel}>周期</Text>
                <Text className={styles.metaValue}>
                  {formatDate(item.startDate)} ~ {formatDate(item.endDate)}
                </Text>
              </View>
              {!!item.remark && <Text className={styles.remark}>说明：{item.remark}</Text>}
            </View>
          ))
        )
      ) : careLogs.length === 0 ? (
        <EmptyState text='暂无护理日志' />
      ) : (
        careLogs.map((item) => (
          <View key={item.logId} className={styles.card}>
            <View className={styles.cardTop}>
              <View className={styles.logTitleBox}>
                <Text className={styles.logDate}>{formatDate(item.logDate)}</Text>
                <StatusTag text={careTypeText(item.careType)} tone='primary' />
              </View>
              <Text className={styles.nurse}>{item.nurseName || '护理员'}</Text>
            </View>
            <Text className={styles.content}>{item.content || '暂无记录'}</Text>
          </View>
        ))
      )}
    </View>
  )
}
