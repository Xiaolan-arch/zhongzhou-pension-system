import { useCallback, useEffect, useState } from 'react'
import { Text, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import { getDeposits } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { DepositInfo } from '../../types'
import { depositTypeText, formatDateTime, formatMoney, payMethodText } from '../../utils/format'
import styles from './index.module.scss'

export default function DepositPage() {
  const router = useRouter()
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)
  const [activeId, setActiveId] = useState<number | null>(null)
  const [info, setInfo] = useState<DepositInfo | null>(null)
  const [loading, setLoading] = useState(false)

  const load = useCallback(async (elderId: number) => {
    setLoading(true)
    try {
      const data = await getDeposits(elderId)
      setInfo(data)
    } catch (e) {
      console.error('[Deposit] 加载押金失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setInfo(null)
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
      const target = Number(router.params.elderId || 0) || list[0]?.elderId
      if (target) {
        setActiveId(target)
        await load(target)
      }
    } catch (e) {
      console.error('[Deposit] 初始化失败', e)
    }
  }, [hydrate, refreshElders, load, router.params.elderId])

  useEffect(() => {
    init()
  }, [init])

  const switchElder = (elderId: number) => {
    setActiveId(elderId)
    load(elderId)
  }

  return (
    <View className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.title}>押金</Text>
        <Text className={styles.subtitle}>押金收退由机构财务统一管理</Text>
      </View>

      <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={switchElder} />

      {loading ? (
        <View className={styles.state}>
          <Text className={styles.stateText}>加载中...</Text>
        </View>
      ) : !info ? (
        <EmptyState text='暂无押金信息' />
      ) : (
        <View>
          <View className={styles.balanceCard}>
            <Text className={styles.balanceLabel}>当前押金余额</Text>
            <Text className={styles.balanceValue}>{formatMoney(info.balance)}</Text>
            <Text className={styles.balanceNote}>
              {info.elderName || '老人'} · 押金由机构统一保管，退住时按合同结算
            </Text>
          </View>

          <View className={styles.sectionHeader}>
            <Text className={styles.sectionTitle}>收退流水</Text>
            <Text className={styles.sectionTip}>共 {info.records.length} 笔</Text>
          </View>

          {info.records.length === 0 ? (
            <EmptyState text='暂无押金流水' />
          ) : (
            info.records.map((record) => {
              const isRefund = record.depositType === '2'
              return (
                <View key={record.depositId} className={styles.card}>
                  <View className={styles.cardTop}>
                    <Text className={styles.cardTitle}>{depositTypeText(record.depositType)}</Text>
                    <Text className={isRefund ? styles.amountOut : styles.amountIn}>
                      {isRefund ? '-' : '+'}
                      {formatMoney(record.amount)}
                    </Text>
                  </View>
                  <View className={styles.row}>
                    <Text className={styles.rowLabel}>时间</Text>
                    <Text className={styles.rowValue}>{formatDateTime(record.createTime)}</Text>
                  </View>
                  <View className={styles.row}>
                    <Text className={styles.rowLabel}>方式</Text>
                    <Text className={styles.rowValue}>{payMethodText(record.payMethod)}</Text>
                  </View>
                  <View className={styles.row}>
                    <Text className={styles.rowLabel}>经办人</Text>
                    <Text className={styles.rowValue}>{record.operatorName || '--'}</Text>
                  </View>
                  {!!record.remark && <Text className={styles.remark}>备注：{record.remark}</Text>}
                </View>
              )
            })
          )}

          <View className={styles.notice}>
            <Text className={styles.noticeText}>
              家属端仅展示押金余额与流水。如对金额有疑问，请联系护理站或财务核对。
            </Text>
          </View>
        </View>
      )}
    </View>
  )
}
