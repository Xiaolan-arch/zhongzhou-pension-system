import { useCallback, useEffect, useState } from 'react'
import { Text, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import { getBillDetail, getBills } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { Bill, BillDetail } from '../../types'
import { billStatusText, billTypeText, formatMoney } from '../../utils/format'
import styles from './index.module.scss'

export default function InvoicePage() {
  const router = useRouter()
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)
  const [activeId, setActiveId] = useState<number | null>(null)
  const [bills, setBills] = useState<Bill[]>([])
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [detail, setDetail] = useState<BillDetail | null>(null)

  const loadDetail = useCallback(async (billId: number) => {
    try {
      const data = await getBillDetail(billId)
      setDetail(data)
      setCurrentId(billId)
    } catch (e) {
      console.error('[Invoice] 加载票据失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setDetail(null)
    }
  }, [])

  const loadBills = useCallback(
    async (elderId: number) => {
      const list = await getBills(elderId)
      setBills(list || [])
      const paramId = Number(router.params.billId || 0)
      const target =
        (paramId && list.some((bill) => bill.billId === paramId) && paramId) ||
        list.find((bill) => bill.payStatus === '1')?.billId ||
        list[0]?.billId
      if (target) {
        await loadDetail(Number(target))
      } else {
        setDetail(null)
      }
    },
    [loadDetail, router.params.billId]
  )

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
        await loadBills(target)
      }
    } catch (e) {
      console.error('[Invoice] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadBills, router.params.elderId])

  useEffect(() => {
    init()
  }, [init])

  const switchElder = (elderId: number) => {
    setActiveId(elderId)
    loadBills(elderId)
  }

  const copyInvoice = () => {
    if (!detail) return
    const rows = detail.items.map((item) => `${item.feeName || '费用'} ${formatMoney(item.amount)}`).join('；')
    Taro.setClipboardData({
      data: `${detail.bill.elderName} ${detail.bill.billMonth} 账单 ${detail.bill.billNo}：${rows}，合计 ${formatMoney(detail.bill.amount)}`
    })
  }

  const bill = detail?.bill

  return (
    <View className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.title}>电子票据</Text>
        <Text className={styles.subtitle}>账单费用结算单预览</Text>
      </View>

      <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={switchElder} />

      {bills.length > 0 && (
        <View className={styles.monthRow}>
          {bills.map((item) => (
            <Text
              key={item.billId}
              className={`${styles.monthChip} ${currentId === item.billId ? styles.monthActive : ''}`}
              onClick={() => loadDetail(item.billId)}
            >
              {item.billMonth}
            </Text>
          ))}
        </View>
      )}

      {!bill ? (
        <EmptyState text='暂无可开具的账单' />
      ) : (
        <View>
          <View className={styles.invoice}>
            <View className={styles.invoiceHead}>
              <Text className={styles.invoiceTitle}>费用结算单</Text>
              <Text className={styles.invoiceSub}>中州养老院 · 电子票据</Text>
            </View>
            <View className={styles.invoiceRow}>
              <Text className={styles.rowLabel}>老人姓名</Text>
              <Text className={styles.rowValue}>{bill.elderName || '--'}</Text>
            </View>
            <View className={styles.invoiceRow}>
              <Text className={styles.rowLabel}>账单月份</Text>
              <Text className={styles.rowValue}>{bill.billMonth || '--'}</Text>
            </View>
            <View className={styles.invoiceRow}>
              <Text className={styles.rowLabel}>账单编号</Text>
              <Text className={styles.rowValue}>{bill.billNo || '--'}</Text>
            </View>
            <View className={styles.invoiceRow}>
              <Text className={styles.rowLabel}>账单类型</Text>
              <Text className={styles.rowValue}>{billTypeText(bill.billType)}</Text>
            </View>

            <View className={styles.itemHead}>
              <Text className={styles.itemName}>费用项目</Text>
              <Text className={styles.itemAmount}>金额</Text>
            </View>
            {detail?.items.map((item) => (
              <View key={item.itemId} className={styles.itemRow}>
                <Text className={styles.itemName}>{item.feeName || '其他费用'}</Text>
                <Text className={styles.itemAmount}>{formatMoney(item.amount)}</Text>
              </View>
            ))}

            <View className={styles.totalRow}>
              <Text className={styles.totalLabel}>合计</Text>
              <Text className={styles.totalValue}>{formatMoney(bill.amount)}</Text>
            </View>
            <View className={styles.invoiceRow}>
              <Text className={styles.rowLabel}>已收金额</Text>
              <Text className={styles.rowValue}>{formatMoney(bill.paidAmount)}</Text>
            </View>
            {Number(bill.penaltyAmount || 0) > 0 && (
              <View className={styles.invoiceRow}>
                <Text className={styles.rowLabel}>滞纳金</Text>
                <Text className={styles.rowValue}>{formatMoney(bill.penaltyAmount)}</Text>
              </View>
            )}
            <View className={styles.invoiceRow}>
              <Text className={styles.rowLabel}>收款状态</Text>
              <Text className={styles.rowValue}>{billStatusText(bill.payStatus)}</Text>
            </View>
          </View>

          <View className={styles.copyBtn} onClick={copyInvoice}>
            <Text className={styles.copyText}>复制账单信息</Text>
          </View>
          <Text className={styles.tip}>
            电子票据用于费用核对，如需盖章纸质票据，请联系机构财务开具。
          </Text>
        </View>
      )}
    </View>
  )
}
