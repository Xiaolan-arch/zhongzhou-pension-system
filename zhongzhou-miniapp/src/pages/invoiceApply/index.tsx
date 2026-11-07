import { useCallback, useEffect, useState } from 'react'
import { Button, Input, Picker, Text, View } from '@tarojs/components'
import Taro, { useDidShow, useRouter } from '@tarojs/taro'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import StatusTag from '../../components/StatusTag'
import { createInvoice, getBills, getInvoices } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { Bill, InvoiceApply } from '../../types'
import { formatDateTime, invoiceStatusText } from '../../utils/format'
import styles from './index.module.scss'

function invoiceTone(status: string) {
  if (status === '1') return 'success' as const
  if (status === '2') return 'danger' as const
  return 'warning' as const
}

export default function InvoiceApplyPage() {
  const router = useRouter()
  const paramElderId = Number(router.params.elderId || 0)
  const paramBillId = Number(router.params.billId || 0)

  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)

  const [activeId, setActiveId] = useState<number | null>(null)
  const [bills, setBills] = useState<Bill[]>([])
  const [invoices, setInvoices] = useState<InvoiceApply[]>([])
  const [billIndex, setBillIndex] = useState(0)
  const [title, setTitle] = useState('')
  const [taxNo, setTaxNo] = useState('')
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [loading, setLoading] = useState(false)

  const loadData = useCallback(async (elderId: number) => {
    setLoading(true)
    try {
      const [billList, invoiceList] = await Promise.all([getBills(elderId), getInvoices(elderId)])
      const available = (billList || []).filter((item) => item.payStatus === '1')
      setBills(available)
      setInvoices(invoiceList)
      let index = 0
      if (paramBillId) {
        const found = available.findIndex((item) => item.billId === paramBillId)
        if (found >= 0) index = found
      }
      setBillIndex(index)
      const elder = elders.find((item) => item.elderId === elderId)
      const target = available[index]
      if (target) {
        setTitle(`${elder?.elderName || target.elderName} 养老服务费`)
      }
    } catch (e) {
      console.error('[InvoiceApply] 加载数据失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
      setBills([])
      setInvoices([])
    } finally {
      setLoading(false)
    }
  }, [elders, paramBillId])

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
      console.error('[InvoiceApply] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadData, paramElderId])

  useEffect(() => {
    init()
    // eslint-disable-next-line react-hooks/exhaustive-deps
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

  const handleSubmit = async () => {
    const bill = bills[billIndex]
    if (!bill) {
      Taro.showToast({ title: '暂无可开票的已缴清账单', icon: 'none' })
      return
    }
    if (!title.trim()) {
      Taro.showToast({ title: '请填写发票抬头', icon: 'none' })
      return
    }
    setSubmitting(true)
    try {
      await createInvoice({
        billId: bill.billId,
        title: title.trim(),
        taxNo: taxNo.trim() || undefined,
        email: email.trim() || undefined
      })
      Taro.showToast({ title: '申请已提交', icon: 'success' })
      if (activeId) await loadData(activeId)
    } catch (e) {
      console.error('[InvoiceApply] 提交失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setSubmitting(false)
    }
  }

  const billLabels = bills.map((item) => `${item.billMonth} ￥${item.amount}`)

  return (
    <View className={styles.pageRoot}>
      <View className={styles.header}>
        <Text className={styles.title}>申请发票</Text>
        <Text className={styles.subtitle}>已缴清账单可申请开具电子发票，机构处理后可在本页查看进度</Text>
      </View>

      {elders.length > 1 && (
        <View className={styles.elderBar}>
          <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={handleElderChange} />
        </View>
      )}

      <View className={styles.formCard}>
        <Text className={styles.formTitle}>开票信息</Text>

        <View className={styles.field}>
          <Text className={styles.label}>选择账单</Text>
          {bills.length === 0 ? (
            <Text className={styles.emptyTip}>暂无可开票的已缴清账单</Text>
          ) : (
            <Picker
              mode='selector'
              range={billLabels}
              value={billIndex}
              onChange={(e) => setBillIndex(Number(e.detail.value))}
            >
              <View className={styles.pickerBox}>
                <Text className={styles.pickerValue}>{billLabels[billIndex] || '请选择账单'}</Text>
                <Text className={styles.pickerArrow}>›</Text>
              </View>
            </Picker>
          )}
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>发票抬头</Text>
          <View className={styles.inputBox}>
            <Input
              className={styles.input}
              maxlength={100}
              placeholder='个人姓名或单位名称'
              value={title}
              onInput={(e) => setTitle(e.detail.value)}
            />
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>纳税人识别号（单位抬头填写）</Text>
          <View className={styles.inputBox}>
            <Input
              className={styles.input}
              maxlength={50}
              placeholder='选填'
              value={taxNo}
              onInput={(e) => setTaxNo(e.detail.value)}
            />
          </View>
        </View>

        <View className={styles.field}>
          <Text className={styles.label}>接收邮箱</Text>
          <View className={styles.inputBox}>
            <Input
              className={styles.input}
              maxlength={100}
              placeholder='选填，用于接收电子发票'
              value={email}
              onInput={(e) => setEmail(e.detail.value)}
            />
          </View>
        </View>

        <Button className={styles.submit} loading={submitting} disabled={bills.length === 0} onClick={handleSubmit}>
          提交申请
        </Button>
      </View>

      <View className={styles.sectionHeader}>
        <Text className={styles.sectionTitle}>开票记录</Text>
        <Text className={styles.sectionTip}>{invoices.length} 条</Text>
      </View>

      {loading ? (
        <View className={styles.loading}>
          <Text className={styles.loadingText}>加载中...</Text>
        </View>
      ) : invoices.length === 0 ? (
        <EmptyState text='暂无开票记录' />
      ) : (
        invoices.map((item) => (
          <View key={item.invoiceId} className={styles.record}>
            <View className={styles.recordTop}>
              <Text className={styles.recordTitle}>{item.title}</Text>
              <StatusTag text={invoiceStatusText(item.status)} tone={invoiceTone(item.status)} />
            </View>
            <View className={styles.metaRow}>
              <Text className={styles.metaLabel}>账单</Text>
              <Text className={styles.metaValue}>{item.billNo || '-'}</Text>
            </View>
            <View className={styles.metaRow}>
              <Text className={styles.metaLabel}>金额</Text>
              <Text className={styles.metaValue}>￥{item.amount ?? 0}</Text>
            </View>
            {!!item.invoiceNo && (
              <View className={styles.metaRow}>
                <Text className={styles.metaLabel}>发票号</Text>
                <Text className={styles.metaValue}>{item.invoiceNo}</Text>
              </View>
            )}
            <Text className={styles.recordTime}>申请时间：{formatDateTime(item.createTime)}</Text>
            {!!item.remark && <Text className={styles.recordRemark}>备注：{item.remark}</Text>}
          </View>
        ))
      )}
    </View>
  )
}
