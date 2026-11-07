import { useState } from 'react'
import { Input, Picker, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { bindElder, matchElders } from '../../services/family'
import { useAuthStore } from '../../store/auth'
import type { ElderVO } from '../../types'
import styles from './index.module.scss'

const RELATIONS = ['配偶', '子女', '其他亲属', '朋友']
const STATUS_TEXT: Record<string, string> = { '0': '在住', '1': '已退住', '2': '待入住' }

/** 匹配结果描述：姓名 · 房号床位 · 状态 */
function describeElder(elder: ElderVO) {
  const bed = elder.roomNo
    ? `${elder.roomNo}房${elder.bedNo ? ` ${elder.bedNo}床` : ''}`
    : '未分配床位'
  return `${elder.elderName} · ${bed} · ${STATUS_TEXT[elder.status] || '状态未知'}`
}

/** 是否为用户取消操作（showModal / showActionSheet） */
function isCancel(err: unknown) {
  const text = String((err as any)?.errMsg || (err as Error)?.message || err)
  return text.indexOf('cancel') >= 0
}

export default function BindPage() {
  const [form, setForm] = useState({ elderName: '', phone: '', relation: '子女' })
  const [submitting, setSubmitting] = useState(false)
  const refreshElders = useAuthStore((s) => s.refreshElders)

  const submit = async () => {
    if (!form.elderName.trim()) { Taro.showToast({ title: '请填写老人姓名', icon: 'none' }); return }
    if (form.phone.trim() && !/^1\d{10}$/.test(form.phone.trim())) { Taro.showToast({ title: '请填写正确的手机号', icon: 'none' }); return }
    setSubmitting(true)
    try {
      const name = form.elderName.trim()
      // 绑定前先匹配档案，让家属确认关联对象，避免同名/新建误绑
      const matches = await matchElders(name)
      let elderId: number | undefined

      if (matches.length === 0) {
        const res = await Taro.showModal({
          title: '未找到老人档案',
          content: `机构内暂无「${name}」的档案，是否创建新档案并绑定？（新档案状态为待入住）`,
          confirmText: '创建并绑定',
          confirmColor: '#2fa36b'
        })
        if (!res.confirm) return
      } else if (matches.length === 1) {
        const res = await Taro.showModal({
          title: '确认绑定信息',
          content: `${describeElder(matches[0])}\n\n请确认是您的家属，绑定后可查看其服务与账单。`,
          confirmText: '确认绑定',
          confirmColor: '#2fa36b'
        })
        if (!res.confirm) return
        elderId = matches[0].elderId
      } else {
        const pick = await Taro.showActionSheet({
          // 微信 showActionSheet 最多支持 6 项
          itemList: matches.slice(0, 6).map(describeElder)
        })
        elderId = matches[pick.tapIndex].elderId
      }

      await bindElder(name, form.phone.trim(), form.relation, elderId)
      await refreshElders()
      Taro.showToast({ title: '绑定成功', icon: 'success' })
      setTimeout(() => Taro.redirectTo({ url: '/pages/bindList/index' }), 1200)
    } catch (err) {
      if (isCancel(err)) return
      Taro.showToast({ title: (err as Error).message || '绑定失败', icon: 'none' })
    } finally { setSubmitting(false) }
  }

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.card}>
        <Text className={styles.cardTitle}>录入老人信息</Text>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>老人姓名<Text className={styles.required}>*</Text></Text>
          <Input className={styles.input} value={form.elderName} maxlength={20}
            placeholder='请输入老人姓名'
            onInput={(e) => setForm((f) => ({ ...f, elderName: e.detail.value }))} />
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>联系电话</Text>
          <Input className={styles.input} type='number' value={form.phone} maxlength={11}
            placeholder='选填，用于接收服务通知'
            onInput={(e) => setForm((f) => ({ ...f, phone: e.detail.value }))} />
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>与老人关系</Text>
          <Picker mode='selector' range={RELATIONS}
            onChange={(e) => setForm((f) => ({ ...f, relation: RELATIONS[Number(e.detail.value)] }))}>
            <View className={styles.picker}>{form.relation}</View>
          </Picker>
        </View>
        <View className={styles.tip}>
          提交后会先展示匹配到的老人档案供您确认：已有档案直接关联（同名时需选择具体档案），没有档案则确认后自动创建新档案（待入住状态）。
        </View>
        <View className={styles.submitBtn} onClick={submit}>
          {submitting ? '绑定中…' : '立即绑定'}
        </View>
      </View>
    </ScrollView>
  )
}
