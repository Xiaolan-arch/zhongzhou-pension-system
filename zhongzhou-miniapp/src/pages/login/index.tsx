import { useEffect, useState } from 'react'
import { Button, Input, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { sendCode } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import styles from './index.module.scss'

const DEMO_ACCOUNTS = [
  { phone: '13822220001', name: '张敏', elder: '张伟 · 中州养老院' },
  { phone: '13844440001', name: '钱小华', elder: '钱国华 · 春晖颐养院' }
]

const PHONE_REG = /^1[3-9]\d{9}$/
const LAST_PHONE_KEY = 'family_last_phone'

export default function LoginPage() {
  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [demoCode, setDemoCode] = useState('')
  const [countdown, setCountdown] = useState(0)
  const [sending, setSending] = useState(false)
  const [logging, setLogging] = useState(false)
  const login = useAuthStore((s) => s.login)
  const hydrate = useAuthStore((s) => s.hydrate)

  useEffect(() => {
    hydrate()
    const lastPhone = Taro.getStorageSync(LAST_PHONE_KEY) || ''
    if (lastPhone) setPhone(lastPhone)
    // 本地已有登录态时直接进入首页，无需重复登录
    if (hasAuth()) {
      Taro.switchTab({ url: '/pages/home/index' })
    }
  }, [hydrate])

  useEffect(() => {
    if (countdown <= 0) return
    const timer = setTimeout(() => setCountdown((c) => c - 1), 1000)
    return () => clearTimeout(timer)
  }, [countdown])

  const handleSendCode = async () => {
    if (!PHONE_REG.test(phone)) {
      Taro.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    setSending(true)
    try {
      const data = await sendCode(phone)
      setDemoCode(data.demoCode)
      setCountdown(60)
      Taro.showToast({ title: '验证码已发送', icon: 'success' })
    } catch (e) {
      console.error('[Login] 发送验证码失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setSending(false)
    }
  }

  const handleLogin = async () => {
    if (!PHONE_REG.test(phone)) {
      Taro.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    if (!code.trim()) {
      Taro.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }
    setLogging(true)
    try {
      // 微信小程序端获取登录 code 用于绑定 openid（H5 预览等环境失败时忽略）
      let wxCode: string | undefined
      if (process.env.TARO_ENV === 'weapp') {
        try {
          const res = await Taro.login()
          wxCode = res.code
        } catch (err) {
          console.warn('[Login] 获取微信登录 code 失败，跳过 openid 绑定', err)
        }
      }
      await login(phone, code.trim(), wxCode)
      Taro.setStorageSync(LAST_PHONE_KEY, phone)
      console.info('[Login] 登录成功', phone)
      Taro.switchTab({ url: '/pages/home/index' })
    } catch (e) {
      console.error('[Login] 登录失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setLogging(false)
    }
  }

  const fillDemo = (demoPhone: string) => {
    setPhone(demoPhone)
    setCode('')
    setDemoCode('')
  }

  return (
    <View className={styles.page}>
      <View className={styles.header}>
        <View className={styles.badge}>
          <Text className={styles.badgeText}>中州颐养</Text>
        </View>
        <Text className={styles.title}>家属关爱平台</Text>
        <Text className={styles.subtitle}>随时了解家人的照护动态</Text>
      </View>

      <View className={styles.formCard}>
        <Text className={styles.label}>手机号</Text>
        <Input
          className={styles.input}
          type='number'
          maxlength={11}
          placeholder='请输入家属登记手机号'
          value={phone}
          onInput={(e) => setPhone(e.detail.value)}
        />

        <Text className={styles.label}>验证码</Text>
        <View className={styles.codeRow}>
          <Input
            className={styles.codeInput}
            type='number'
            maxlength={6}
            placeholder='请输入 6 位验证码'
            value={code}
            onInput={(e) => setCode(e.detail.value)}
          />
          <Button
            className={styles.codeButton}
            disabled={countdown > 0 || sending}
            onClick={handleSendCode}
          >
            {countdown > 0 ? `${countdown}s 后重发` : sending ? '发送中' : '获取验证码'}
          </Button>
        </View>

        {!!demoCode && (
          <View className={styles.demoHint}>
            <Text className={styles.demoLabel}>演示环境验证码</Text>
            <Text className={styles.demoCode}>{demoCode}</Text>
          </View>
        )}

        <Button className={styles.loginButton} loading={logging} onClick={handleLogin}>
          登录 / 绑定
        </Button>
        <Text className={styles.tip}>仅限已在本机构登记家属关系的手机号登录</Text>
      </View>

      <View className={styles.demoAccounts}>
        <Text className={styles.demoTitle}>演示账号（点击填入）</Text>
        {DEMO_ACCOUNTS.map((item) => (
          <View key={item.phone} className={styles.demoItem} onClick={() => fillDemo(item.phone)}>
            <View>
              <Text className={styles.demoName}>{item.name}</Text>
              <Text className={styles.demoElder}>{item.elder}</Text>
            </View>
            <Text className={styles.demoPhone}>{item.phone}</Text>
          </View>
        ))}
      </View>
    </View>
  )
}
