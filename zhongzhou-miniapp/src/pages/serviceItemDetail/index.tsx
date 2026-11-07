import { useEffect, useMemo, useState } from 'react'
import { Image, Picker, ScrollView, Text, Textarea, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { createOrder, getPublicServiceItemDetail } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { PublicServiceItem } from '../../types'
import styles from './index.module.scss'

// ====== 本地服务项目图片（与 serviceItems 页一致）======
import imgBath from '../../assets/services/助浴@2x.png'
import imgMeal from '../../assets/services/助餐@2x.png'
import imgWashHair from '../../assets/services/洗头@2x.png'
import imgNail from '../../assets/services/修剪指甲@2x.png'
import imgLaundry from '../../assets/services/衣物清洁@2x.png'
import imgChat from '../../assets/services/谈心@2x.png'
import imgTurn from '../../assets/services/翻身拍背@2x.png'
import imgRehab from '../../assets/services/复建运动@2x.png'
const SERVICE_IMAGES: Record<string, string> = {
  '助浴': imgBath, '助餐': imgMeal, '洗头': imgWashHair,
  '修剪指甲': imgNail, '衣物清洁': imgLaundry, '谈心': imgChat,
  '翻身拍背': imgTurn, '复健运动': imgRehab,
}

function pad(n: number) { return n < 10 ? `0${n}` : `${n}` }
function today() {
  const d = new Date(); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export default function ServiceItemDetailPage() {
  const router = Taro.getCurrentInstance().router
  const itemId = Number(router?.params?.itemId || 0)

  const [item, setItem] = useState<PublicServiceItem | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const elders = useAuthStore((s) => s.elders)
  const [elderIndex, setElderIndex] = useState(0)
  const [serviceDate, setServiceDate] = useState(today())
  const [remark, setRemark] = useState('')

  useEffect(() => {
    if (!itemId) { setError('缺少服务项目参数'); setLoading(false); return }
    getPublicServiceItemDetail(itemId)
      .then((d) => { setItem(d); setLoading(false) })
      .catch((err) => { setError(err.message || '加载失败'); setLoading(false) })
  }, [itemId])

  const elderNames = useMemo(() => elders.map((e) => `${e.elderName}（${e.roomNo || '未安排'}）`), [elders])

  const canSubmit = item && hasAuth() && elders.length > 0

  const onSubmit = async () => {
    if (!hasAuth()) {
      Taro.showModal({
        title: '需要登录',
        content: '预约下单需要先登录家属账号',
        confirmText: '去登录',
        cancelText: '再想想',
        success: (res) => { if (res.confirm) Taro.navigateTo({ url: '/pages/login/index' }) }
      })
      return
    }
    if (!item) return
    if (elders.length === 0) {
      Taro.showToast({ title: '暂无绑定老人', icon: 'none' }); return
    }
    setSubmitting(true)
    try {
      await createOrder({
        elderId: elders[elderIndex].elderId,
        itemId: item.itemId,
        serviceTime: `${serviceDate} 09:00:00`,
        remark
      })
      Taro.showToast({ title: '预约成功', icon: 'success' })
      setTimeout(() => Taro.switchTab({ url: '/pages/service/index' }), 1200)
    } catch (e) {
      Taro.showToast({ title: (e as Error).message || '下单失败', icon: 'none' })
    } finally { setSubmitting(false) }
  }

  const onContact = () => {
    Taro.showModal({
      title: '联系护理站',
      content: '遇到服务问题或需要售后反馈，请致电机构，或在「服务记录-详情」里提交评价与备注。',
      confirmText: '知道了', showCancel: false
    })
  }

  if (loading) return <ScrollView scrollY className={styles.page}><Text className={styles.loading}>加载中…</Text></ScrollView>
  if (error) return <ScrollView scrollY className={styles.page}><Text className={styles.empty}>{error}</Text></ScrollView>
  if (!item) return <ScrollView scrollY className={styles.page}><Text className={styles.empty}>服务不存在</Text></ScrollView>

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.hero}>
        <Image
          className={styles.heroImage}
          src={SERVICE_IMAGES[item.itemName] || imgBath}
          mode='aspectFill'
        />
        <View className={styles.heroOverlay}>
          <Text className={styles.contactBtn} onClick={onContact}>联系护理站</Text>
          <Text className={styles.heroName}>{item.itemName}</Text>
          <View>
            <Text className={styles.heroPrice}>{item.price ? `¥${item.price.toFixed(0)}` : '待咨询'}</Text>
            {item.unit && <Text className={styles.heroUnit}>/ {item.unit}</Text>}
          </View>
        </View>
      </View>

      <View className={styles.card}>
        <Text className={styles.cardTitle}>服务介绍</Text>
        <Text className={styles.desc}>{item.description || '暂无描述'}</Text>
      </View>

      <View className={styles.card}>
        <Text className={styles.cardTitle}>预约下单</Text>

        <View className={styles.field}>
          <Text className={styles.fieldLabel}>服务对象<Text className={styles.required}>*</Text></Text>
          {elders.length === 0 ? (
            <View className={styles.picker} onClick={() => Taro.showToast({ title: '请先登录并绑定老人', icon: 'none' })}>
              （未登录 / 无绑定老人）
            </View>
          ) : (
            <Picker mode='selector' range={elderNames}
              onChange={(e) => setElderIndex(Number(e.detail.value))}>
              <View className={styles.picker}>{elderNames[elderIndex]}</View>
            </Picker>
          )}
        </View>

        <View className={styles.field}>
          <Text className={styles.fieldLabel}>预约日期<Text className={styles.required}>*</Text></Text>
          <Picker mode='date' value={serviceDate} start={today()}
            onChange={(e) => setServiceDate(e.detail.value)}>
            <View className={styles.picker}>{serviceDate}</View>
          </Picker>
        </View>

        <View className={styles.field}>
          <Text className={styles.fieldLabel}>备注说明（选填）</Text>
          <Textarea className={styles.textarea} placeholder='如老人有特殊照护需求、提醒事项等'
            value={remark} onInput={(e) => setRemark(e.detail.value)} maxlength={200} />
        </View>
      </View>

      <View className={styles.card}>
        <Text className={styles.cardTitle}>注意事项</Text>
        <View className={styles.infoRow}>
          <Text className={styles.infoLabel}>提前预约</Text>
          <Text className={styles.infoValue}>建议提前一天</Text>
        </View>
        <View className={styles.infoRow}>
          <Text className={styles.infoLabel}>取消订单</Text>
          <Text className={styles.infoValue}>开始前可取消</Text>
        </View>
        <View className={styles.infoRow}>
          <Text className={styles.infoLabel}>评价反馈</Text>
          <Text className={styles.infoValue}>服务后可提交评价</Text>
        </View>
      </View>

      <View className={styles.footer}>
        <View className={styles.footerPrice}>
          <Text className={styles.footerPriceLabel}>本次费用</Text>
          <Text className={styles.footerPriceValue}>{item.price ? `¥${item.price.toFixed(0)}` : '待咨询'}</Text>
        </View>
        <View
          className={`${styles.submitBtn} ${!canSubmit ? styles.submitBtnDisabled : ''}`}
          onClick={onSubmit}
        >
          {submitting ? '提交中…' : canSubmit ? '立即预约' : (hasAuth() ? '请先绑定老人' : '去登录下单')}
        </View>
      </View>
    </ScrollView>
  )
}
