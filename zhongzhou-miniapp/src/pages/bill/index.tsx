import { useCallback, useEffect, useState } from 'react'
import { Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { getBills, getDeposits } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { Bill } from '../../types'
import { billStatusText, billTypeText, formatMoney } from '../../utils/format'
import styles from './index.module.scss'

function billTone(status: string) {
  if (status === '1') return 'success' as const
  if (status === '3') return 'warning' as const
  if (status === '0') return 'danger' as const
  return 'info' as const
}

export default function BillPage() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)
  const [activeId, setActiveId] = useState<number | null>(null)
  const [bills, setBills] = useState<Bill[]>([])
  const [depositBalance, setDepositBalance] = useState(0)
  const [loading, setLoading] = useState(false)

  const load = useCallback(async (elderId: number) => {
    setLoading(true)
    try {
      const [data, deposit] = await Promise.all([getBills(elderId), getDeposits(elderId)])
      setBills(data)
      setDepositBalance(Number(deposit.balance || 0))
    } catch (e) {
      console.error('[Bill] 加载账单失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setBills([])
      setDepositBalance(0)
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
        await load(list[0].elderId)
      }
    } catch (e) {
      console.error('[Bill] 初始化失败', e)
    }
  }, [hydrate, refreshElders, load])

  useEffect(() => {
    init()
  }, [init])

  const switchElder = (elderId: number) => {
    setActiveId(elderId)
    load(elderId)
  }

  const unpaidOf = (bill: Bill) =>
    Math.max(0, Number(bill.amount || 0) - Number(bill.prepaid || 0) - Number(bill.paidAmount || 0))
  const penaltyOf = (bill: Bill) => Number(bill.penaltyAmount || 0)
  const unpaidBills = bills.filter((bill) => bill.payStatus !== '1' && bill.payStatus !== '2')
  const unpaidTotal = unpaidBills.reduce((sum, bill) => sum + unpaidOf(bill) + penaltyOf(bill), 0)
  const penaltyTotal = unpaidBills.reduce((sum, bill) => sum + penaltyOf(bill), 0)
  const paidTotal = bills.reduce((sum, bill) => sum + Number(bill.paidAmount || 0), 0)

  return (
    <View className={styles.pageRoot}>
      <View className={styles.summary}>
        <Text className={styles.summaryLabel}>待缴费用合计</Text>
        <Text className={styles.summaryValue}>{formatMoney(unpaidTotal)}</Text>
        <View className={styles.summarySplit}>
          <Text className={styles.summarySub}>累计已缴 {formatMoney(paidTotal)}</Text>
          <Text className={styles.summarySub}>共 {bills.length} 期账单</Text>
        </View>
        {penaltyTotal > 0 && (
          <Text className={styles.summaryPenalty}>含滞纳金 {formatMoney(penaltyTotal)}</Text>
        )}
      </View>

      <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={switchElder} />

      {!!activeId && (
        <View className={styles.quickRow}>
          <View
            className={styles.quickBox}
            onClick={() =>
              Taro.navigateTo({ url: `/pages/deposit/index?elderId=${activeId}` })
            }
          >
            <Text className={styles.quickLabel}>押金余额</Text>
            <Text className={styles.quickValue}>{formatMoney(depositBalance)}</Text>
            <Text className={styles.quickDesc}>查看收退流水 ›</Text>
          </View>
          <View
            className={styles.quickBox}
            onClick={() =>
              Taro.navigateTo({ url: `/pages/invoice/index?elderId=${activeId}` })
            }
          >
            <Text className={styles.quickLabel}>电子票据</Text>
            <Text className={styles.quickValue}>账单票据</Text>
            <Text className={styles.quickDesc}>预览费用结算单 ›</Text>
          </View>
        </View>
      )}

      <View className={styles.sectionHeader}>
        <Text className={styles.sectionTitle}>账单明细</Text>
        <Text className={styles.sectionTip}>费用由机构统一结算</Text>
      </View>

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : bills.length === 0 ? (
        <EmptyState text='暂无账单记录' />
      ) : (
        bills.map((bill) => (
          <View
            key={bill.billId}
            className={styles.card}
            onClick={() => Taro.navigateTo({ url: `/pages/billDetail/index?billId=${bill.billId}` })}
          >
            <View className={styles.cardTop}>
              <View className={styles.monthBox}>
                <Text className={styles.month}>{bill.billMonth || '--'}</Text>
                <Text className={styles.type}>{billTypeText(bill.billType)}</Text>
              </View>
              <View className={styles.statusBox}>
                {Number(bill.overdueDays || 0) > 0 && bill.payStatus !== '1' && bill.payStatus !== '2' && (
                  <Text className={styles.overdueTag}>逾期 {bill.overdueDays} 天</Text>
                )}
                <StatusTag text={billStatusText(bill.payStatus)} tone={billTone(bill.payStatus)} />
              </View>
            </View>
            <View className={styles.amountRow}>
              <Text className={styles.amountLabel}>账单金额</Text>
              <Text className={styles.amountValue}>{formatMoney(bill.amount)}</Text>
            </View>
            <View className={styles.metaRow}>
              <Text className={styles.meta}>已付 {formatMoney(bill.paidAmount)}</Text>
              {bill.payStatus !== '1' && bill.payStatus !== '2' && unpaidOf(bill) > 0 && (
                <Text className={styles.metaWarn}>待付 {formatMoney(unpaidOf(bill))}</Text>
              )}
            </View>
            {penaltyOf(bill) > 0 && (
              <View className={styles.penaltyRow}>
                <Text className={styles.penaltyText}>滞纳金 {formatMoney(penaltyOf(bill))}</Text>
                <Text className={styles.payableText}>
                  待收合计 {formatMoney(unpaidOf(bill) + penaltyOf(bill))}
                </Text>
              </View>
            )}
            <View className={styles.cardBottom}>
              <Text className={styles.billNo}>账单号 {bill.billNo}</Text>
              <Text className={styles.detailLink}>查看明细 ›</Text>
            </View>
          </View>
        ))
      )}
    </View>
  )
}
