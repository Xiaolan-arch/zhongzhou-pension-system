import { useEffect, useState } from 'react'
import { Button, Text, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import StatusTag from '../../components/StatusTag'
import { getBillDetail, payBill } from '../../services/family'
import type { Bill, BillItem, PaymentRecord } from '../../types'
import {
  billStatusText,
  billTypeText,
  formatDate,
  formatDateTime,
  formatMoney,
  payMethodText
} from '../../utils/format'
import styles from './index.module.scss'

export default function BillDetailPage() {
  const router = useRouter()
  const billId = Number(router.params.billId || 0)
  const [bill, setBill] = useState<Bill | null>(null)
  const [items, setItems] = useState<BillItem[]>([])
  const [records, setRecords] = useState<PaymentRecord[]>([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [paying, setPaying] = useState(false)

  useEffect(() => {
    const load = async () => {
      if (!billId) {
        setError('缺少账单参数')
        setLoading(false)
        return
      }
      setLoading(true)
      try {
        const data = await getBillDetail(billId)
        setBill(data.bill)
        setItems(data.items || [])
        setRecords(data.records || [])
      } catch (e) {
        console.error('[BillDetail] 加载账单失败', e)
        setError((e as Error).message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [billId])

  if (loading) {
    return (
      <View className={styles.state}>
        <Text className={styles.stateText}>账单加载中...</Text>
      </View>
    )
  }

  if (!bill) {
    return (
      <View className={styles.state}>
        <Text className={styles.stateText}>{error || '账单不存在'}</Text>
        <Button className={styles.backButton} onClick={() => Taro.navigateBack()}>
          返回
        </Button>
      </View>
    )
  }

  const tone = bill.payStatus === '1' ? 'success' : bill.payStatus === '3' ? 'warning' : bill.payStatus === '0' ? 'danger' : 'info'
  const unpaid = Math.max(
    0,
    Number(bill.amount || 0) - Number(bill.prepaid || 0) - Number(bill.paidAmount || 0)
  )
  const penalty = Number(bill.penaltyAmount || 0)
  const overdueDays = Number(bill.overdueDays || 0)
  const totalDue = unpaid + penalty
  const canPay = bill.payStatus !== '1' && bill.payStatus !== '2' && unpaid > 0
  const itemsTotal = items.reduce((sum, item) => sum + Number(item.amount || 0), 0)

  const handlePay = async () => {
    if (!canPay || paying) return
    const result = await Taro.showModal({
      title: '确认缴费',
      content: `演示环境模拟微信支付 ${formatMoney(unpaid)}，不会真实扣款，确认继续？`,
      confirmText: '确认支付',
      confirmColor: '#2f9b6a'
    })
    if (!result.confirm) return
    setPaying(true)
    try {
      const updated = await payBill(billId)
      setBill(updated)
      Taro.showToast({ title: '缴费成功', icon: 'success' })
    } catch (e) {
      console.error('[BillDetail] 缴费失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setPaying(false)
    }
  }

  return (
    <View className={styles.page}>
      <View className={styles.hero}>
        <View className={styles.heroTop}>
          <Text className={styles.heroLabel}>{bill.billMonth} · {billTypeText(bill.billType)}</Text>
          <StatusTag text={billStatusText(bill.payStatus)} tone={tone} />
        </View>
        <Text className={styles.heroAmount}>{formatMoney(bill.amount)}</Text>
        <View className={styles.heroSplit}>
          <Text className={styles.heroSub}>已付 {formatMoney(bill.paidAmount)}</Text>
          <Text className={styles.heroSub}>
            {unpaid > 0 ? `待付 ${formatMoney(unpaid)}` : '费用已结清'}
          </Text>
        </View>
        {penalty > 0 && (
          <Text className={styles.heroPenalty}>
            逾期 {overdueDays} 天 · 滞纳金 {formatMoney(penalty)} · 待收合计 {formatMoney(totalDue)}
          </Text>
        )}
      </View>

      <View className={styles.content}>
        <View className={styles.card}>
          <Text className={styles.cardTitle}>账单信息</Text>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>账单编号</Text>
            <Text className={styles.rowValue}>{bill.billNo}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>老人姓名</Text>
            <Text className={styles.rowValue}>{bill.elderName || '--'}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>账单类型</Text>
            <Text className={styles.rowValue}>{billTypeText(bill.billType)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>账期</Text>
            <Text className={styles.rowValue}>{bill.billMonth || '--'}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>费用周期</Text>
            <Text className={styles.rowValue}>
              {formatDate(bill.periodStart)} 至 {formatDate(bill.periodEnd)}
            </Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>账单金额</Text>
            <Text className={styles.rowValue}>{formatMoney(bill.amount)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>已付金额</Text>
            <Text className={styles.rowValue}>{formatMoney(bill.paidAmount)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>待付金额</Text>
            <Text className={styles.rowValue}>{formatMoney(unpaid > 0 ? unpaid : 0)}</Text>
          </View>
          {overdueDays > 0 && (
            <View className={styles.row}>
              <Text className={styles.rowLabel}>逾期天数</Text>
              <Text className={styles.rowValue}>{overdueDays} 天</Text>
            </View>
          )}
          {penalty > 0 && (
            <View className={styles.row}>
              <Text className={styles.rowLabel}>滞纳金</Text>
              <Text className={styles.rowValue}>{formatMoney(penalty)}</Text>
            </View>
          )}
          {penalty > 0 && (
            <View className={styles.row}>
              <Text className={styles.rowLabel}>待收合计</Text>
              <Text className={styles.rowValue}>{formatMoney(totalDue)}</Text>
            </View>
          )}
          {!!bill.payTime && (
            <View className={styles.row}>
              <Text className={styles.rowLabel}>缴费时间</Text>
              <Text className={styles.rowValue}>{formatDateTime(bill.payTime)}</Text>
            </View>
          )}
          {!!bill.payMethod && (
            <View className={styles.row}>
              <Text className={styles.rowLabel}>缴费方式</Text>
              <Text className={styles.rowValue}>{bill.payMethod}</Text>
            </View>
          )}
          {!!bill.remark && (
            <View className={styles.row}>
              <Text className={styles.rowLabel}>备注</Text>
              <Text className={styles.rowValue}>{bill.remark}</Text>
            </View>
          )}
        </View>

        <View className={styles.card}>
          <View className={styles.itemsHeader}>
            <Text className={styles.cardTitle}>费用明细</Text>
            <Text className={styles.itemCount}>{items.length} 项</Text>
          </View>
          {items.length === 0 ? (
            <Text className={styles.emptyText}>暂无费用明细</Text>
          ) : (
            items.map((item) => (
              <View key={item.itemId} className={styles.item}>
                <View className={styles.itemInfo}>
                  <Text className={styles.itemName}>{item.feeName}</Text>
                  {!!item.serviceContent && (
                    <Text className={styles.itemContent}>{item.serviceContent}</Text>
                  )}
                </View>
                <Text className={styles.itemAmount}>{formatMoney(item.amount)}</Text>
              </View>
            ))
          )}
          {items.length > 0 && (
            <View className={styles.totalRow}>
              <Text className={styles.totalLabel}>明细合计</Text>
              <Text className={styles.totalValue}>{formatMoney(itemsTotal || bill.amount)}</Text>
            </View>
          )}
        </View>

        <View className={styles.card}>
          <View className={styles.itemsHeader}>
            <Text className={styles.cardTitle}>收款记录</Text>
            <Text className={styles.itemCount}>{records.length} 笔</Text>
          </View>
          {records.length === 0 ? (
            <Text className={styles.emptyText}>暂无收款记录</Text>
          ) : (
            records.map((record) => (
              <View key={record.recordId} className={styles.record}>
                <View className={styles.recordInfo}>
                  <Text className={styles.recordAmount}>
                    {Number(record.payAmount || 0) >= 0 ? '+' : ''}
                    {formatMoney(record.payAmount)}
                  </Text>
                  <Text className={styles.recordMeta}>
                    {formatDateTime(record.createTime)} · {payMethodText(record.payMethod)}
                  </Text>
                </View>
                <Text className={styles.recordOperator}>{record.operatorName || '--'}</Text>
              </View>
            ))
          )}
        </View>

        {bill.payStatus === '1' && (
          <View className={styles.invoiceCard}>
            <View className={styles.invoiceInfo}>
              <Text className={styles.invoiceTitle}>电子发票</Text>
              <Text className={styles.invoiceDesc}>该账单已结清，可申请开具发票</Text>
            </View>
            <View
              className={styles.invoiceBtn}
              onClick={() =>
                Taro.navigateTo({
                  url: `/pages/invoiceApply/index?billId=${bill.billId}&elderId=${bill.elderId}`
                })
              }
            >
              <Text className={styles.invoiceBtnText}>申请开票</Text>
            </View>
          </View>
        )}

        {canPay && (
          <View className={styles.payCard}>
            <Button className={styles.payButton} loading={paying} disabled={paying} onClick={handlePay}>
              立即缴费 {formatMoney(unpaid)}
            </Button>
            <Text className={styles.payHint}>
              演示环境为模拟微信支付，不会真实扣款
              {penalty > 0 ? '；滞纳金需到院或联系护理站补缴' : ''}
            </Text>
          </View>
        )}

        <Text className={styles.tip}>
          费用由机构统一结算，如对账单有疑问请及时与护理站核对。
        </Text>
      </View>
    </View>
  )
}
