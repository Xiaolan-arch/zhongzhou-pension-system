import { useState } from 'react'
import { Input, Picker, ScrollView, Text, Textarea, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { createCheckin } from '../../services/family'
import styles from './index.module.scss'

const GENDERS = [
  { label: '男', value: '0' },
  { label: '女', value: '1' }
]

export default function CheckinApplyPage() {
  const [form, setForm] = useState({
    elderName: '',
    gender: '0',
    age: '',
    phone: '',
    address: '',
    selfCare: '',
    mentalState: '',
    disease: '',
    familyRelation: ''
  })
  const [submitting, setSubmitting] = useState(false)

  const update = (k: keyof typeof form, v: string) =>
    setForm((f) => ({ ...f, [k]: v }))

  const submit = async () => {
    const missing: string[] = []
    if (!form.elderName.trim()) missing.push('老人姓名')
    if (!form.age || Number(form.age) < 40 || Number(form.age) > 120) missing.push('请填写 40-120 的有效年龄')
    if (!/^1\d{10}$/.test(form.phone.trim())) missing.push('有效的联系电话')
    if (missing.length) {
      Taro.showToast({ title: missing[0], icon: 'none' }); return
    }
    setSubmitting(true)
    try {
      await createCheckin({
        elderName: form.elderName.trim(),
        gender: form.gender,
        age: Number(form.age),
        phone: form.phone.trim(),
        address: form.address.trim() || undefined,
        selfCare: form.selfCare.trim() || undefined,
        mentalState: form.mentalState.trim() || undefined,
        disease: form.disease.trim() || undefined,
        familyRelation: form.familyRelation.trim() || undefined
      })
      Taro.showToast({ title: '申请已提交', icon: 'success' })
      setTimeout(() => Taro.redirectTo({ url: '/pages/checkinList/index' }), 1200)
    } catch (err) {
      Taro.showToast({ title: (err as Error).message || '提交失败', icon: 'none' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <ScrollView scrollY className={styles.page}>
      {/* 基本信息 */}
      <View className={styles.card}>
        <Text className={styles.cardTitle}>老人基本信息</Text>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>老人姓名<Text className={styles.required}>*</Text></Text>
          <Input className={styles.input} value={form.elderName} placeholder='请输入老人姓名'
            onInput={(e) => update('elderName', e.detail.value)} />
        </View>
        <View className={styles.row2}>
          <View className={styles.field}>
            <Text className={styles.fieldLabel}>性别<Text className={styles.required}>*</Text></Text>
            <Picker mode='selector' range={GENDERS.map((g) => g.label)}
              onChange={(e) => update('gender', GENDERS[Number(e.detail.value)].value)}>
              <View className={styles.picker}>{GENDERS.find((g) => g.value === form.gender)?.label || '请选择'}</View>
            </Picker>
          </View>
          <View className={styles.field}>
            <Text className={styles.fieldLabel}>年龄<Text className={styles.required}>*</Text></Text>
            <Input className={styles.input} type='number' value={form.age} placeholder='例如 72'
              onInput={(e) => update('age', e.detail.value)} />
          </View>
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>联系电话<Text className={styles.required}>*</Text></Text>
          <Input className={styles.input} type='number' maxlength={11} value={form.phone} placeholder='家属联系电话'
            onInput={(e) => update('phone', e.detail.value)} />
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>户籍地址</Text>
          <Input className={styles.input} value={form.address} placeholder='可选'
            onInput={(e) => update('address', e.detail.value)} />
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>与老人关系</Text>
          <Input className={styles.input} value={form.familyRelation} placeholder='如：子女 / 配偶 / 其他'
            onInput={(e) => update('familyRelation', e.detail.value)} />
        </View>
      </View>

      {/* 健康情况 */}
      <View className={styles.card}>
        <Text className={styles.cardTitle}>健康情况（可留空）</Text>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>自理能力评估</Text>
          <Textarea className={styles.input} value={form.selfCare} placeholder='饮食/穿衣/起居等能力描述'
            onInput={(e) => update('selfCare', e.detail.value)} />
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>精神状态</Text>
          <Textarea className={styles.input} value={form.mentalState} placeholder='意识/情绪/认知状况'
            onInput={(e) => update('mentalState', e.detail.value)} />
        </View>
        <View className={styles.field}>
          <Text className={styles.fieldLabel}>既往疾病</Text>
          <Textarea className={styles.input} value={form.disease} placeholder='慢性病史、过敏史等'
            onInput={(e) => update('disease', e.detail.value)} />
        </View>
      </View>

      {/* CTA */}
      <View className={styles.cta}>
        <View className={styles.submitBtn} onClick={submit}>
          {submitting ? '提交中…' : '提交入住申请'}
        </View>
      </View>
    </ScrollView>
  )
}

