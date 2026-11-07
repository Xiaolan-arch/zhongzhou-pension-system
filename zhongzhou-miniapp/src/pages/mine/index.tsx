import { useCallback, useEffect } from 'react'
import { Button, Text, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
// 共享组件统一引入顺序：EmptyState -> ElderCard（ElderCard 内部会引入 StatusTag），避免 CSS 打包顺序冲突
import EmptyState from '../../components/EmptyState'
import ElderCard from '../../components/ElderCard'
import { hasAuth, useAuthStore } from '../../store/auth'
import { nameInitial } from '../../utils/format'
import styles from './index.module.scss'

const MENU = [
  { key: 'report', icon: '报', title: '健康报告', desc: 'AI 照护评估与历史记录', url: '/pages/report/index' },
  { key: 'aiChat', icon: 'AI', title: 'AI客服', desc: '在线咨询照护与费用问题', url: '/pages/chat/index' },
  { key: 'health', icon: '体', title: '体征查询', desc: '血压·心率·血糖等实时数据', url: '/pages/health/index' },
  { key: 'visit', icon: '访', title: '探访预约', desc: '预约到院探访并查看进度', url: '/pages/visit/index' },
  { key: 'leave', icon: '假', title: '请假外出', desc: '提交外出申请并查看审批', url: '/pages/leave/index' },
  { key: 'care', icon: '护', title: '照护记录', desc: '用药提醒与护理日志', url: '/pages/care/index' },
  { key: 'invoiceApply', icon: '开', title: '申请发票', desc: '已缴清账单在线开票', url: '/pages/invoiceApply/index' },
  { key: 'feedback', icon: '诉', title: '投诉建议', desc: '意见直达院区管理人员', url: '/pages/feedback/index' },
  { key: 'message', icon: '信', title: '消息中心', desc: '账单、逾期与机构通知', url: '/pages/message/index' },
  { key: 'deposit', icon: '押', title: '押金', desc: '押金余额与收退流水', url: '/pages/deposit/index' },
  { key: 'invoice', icon: '票', title: '电子票据', desc: '费用结算单预览', url: '/pages/invoice/index' },
  { key: 'bind', icon: '关', title: '绑定管理', desc: '添加/解除老人绑定', url: '/pages/bindList/index' },
  { key: 'checkinList', icon: '申', title: '入住申请', desc: '查看申请进度/提交新申请', url: '/pages/checkinList/index' },
  { key: 'service', icon: '护', title: '服务记录', desc: '查看护理服务明细', url: '/pages/service/index', tab: true },
  { key: 'bill', icon: '账', title: '费用账单', desc: '查看缴费与费用明细', url: '/pages/bill/index', tab: true }
]

export default function MinePage() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const logout = useAuthStore((s) => s.logout)
  const family = useAuthStore((s) => s.family)
  const elders = useAuthStore((s) => s.elders)

  const init = useCallback(async () => {
    hydrate()
    if (!hasAuth()) {
      Taro.redirectTo({ url: '/pages/login/index' })
      return
    }
    try {
      await refreshElders()
    } catch (e) {
      console.warn('[Mine] 刷新老人列表失败', e)
    }
  }, [hydrate, refreshElders])

  useEffect(() => {
    init()
  }, [init])

  useDidShow(() => {
    if (hasAuth()) {
      refreshElders().catch((e) => console.warn('[Mine] 刷新老人列表失败', e))
    }
  })

  const goMenu = (item: (typeof MENU)[number]) => {
    if (item.tab) {
      Taro.switchTab({ url: item.url })
      return
    }
    const elderId = elders[0]?.elderId
    Taro.navigateTo({ url: elderId ? `${item.url}?elderId=${elderId}` : item.url })
  }

  const handleLogout = async () => {
    const res = await Taro.showModal({
      title: '退出登录',
      content: '退出后需重新使用手机号验证码登录',
      confirmText: '退出',
      confirmColor: '#f05a5a'
    })
    if (!res.confirm) return
    try {
      await logout()
      Taro.redirectTo({ url: '/pages/login/index' })
    } catch (e) {
      console.error('[Mine] 退出登录失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    }
  }

  return (
    <View className={styles.page}>
      <View className={styles.hero}>
        <View className={styles.avatar}>
          <Text className={styles.avatarText}>{nameInitial(family?.familyName)}</Text>
        </View>
        <View className={styles.heroInfo}>
          <Text className={styles.name}>{family?.familyName || '家属'}</Text>
          <Text className={styles.relation}>
            {family?.relation ? `老人的${family.relation}` : '家属关爱平台'}
            {family?.phone ? ` · ${family.phone}` : ''}
          </Text>
        </View>
      </View>

      <View className={styles.content}>
        <View className={styles.card}>
          <View className={styles.cardHeader}>
            <Text className={styles.cardTitle}>绑定的老人</Text>
            <Text className={styles.cardTip}>{elders.length} 位</Text>
          </View>
          {elders.length === 0 ? (
            <EmptyState text='暂无绑定老人，请联系机构登记家属关系' />
          ) : (
            elders.map((elder) => (
              <View key={elder.elderId} className={styles.elderItem}>
                <ElderCard
                  elder={elder}
                  compact
                  onClick={() => Taro.navigateTo({ url: `/pages/elder/index?elderId=${elder.elderId}` })}
                />
              </View>
            ))
          )}
        </View>

        <View className={styles.card}>
          {MENU.map((item) => (
            <View key={item.key} className={styles.menuItem} onClick={() => goMenu(item)}>
              <View className={styles.menuIcon}>
                <Text className={styles.menuIconText}>{item.icon}</Text>
              </View>
              <View className={styles.menuInfo}>
                <Text className={styles.menuTitle}>{item.title}</Text>
                <Text className={styles.menuDesc}>{item.desc}</Text>
              </View>
              <Text className={styles.arrow}>›</Text>
            </View>
          ))}
        </View>

        <View className={styles.notice}>
          <Text className={styles.noticeTitle}>信息说明</Text>
          <Text className={styles.noticeText}>
            老人档案、服务与费用信息由养老机构统一维护，家属端仅作查看。如发现信息有误，请联系护理站核对。
          </Text>
        </View>

        <Button className={styles.logout} onClick={handleLogout}>
          退出登录
        </Button>
        <Text className={styles.version}>中州颐养 · 家属端 v1.0</Text>
      </View>
    </View>
  )
}
