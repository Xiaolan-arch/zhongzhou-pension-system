import { useCallback, useEffect, useState } from 'react'
import { Image, Text, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
// 共享组件统一引入顺序：ElderSwitcher -> ElderCard -> StatusTag，避免 CSS 打包顺序冲突
import ElderSwitcher from '../../components/ElderSwitcher'
import ElderCard from '../../components/ElderCard'
import StatusTag from '../../components/StatusTag'
import { getBills, getPublicRooms, getReports, getServices } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import { formatMoney, riskLevelText } from '../../utils/format'
import { getRoomTypeMeta } from '../../utils/roomType'
import type { RoomView } from '../../types'
import styles from './index.module.scss'

// ====== 首页配图（来自「规范+切图」） ======
import bannerImg from '../../assets/home/banner.png'
import consultantImg from '../../assets/home/consultant.png'
import iconInstitution from '../../assets/home/icon-institution.png'
import iconFamily from '../../assets/home/icon-family.png'
import iconService from '../../assets/home/icon-service.png'
import iconBill from '../../assets/home/icon-bill.png'
import consultChat from '../../assets/home/consult-chat.png'
import consultBook from '../../assets/home/consult-book.png'

type Entry = {
  key: string
  title: string
  desc?: string
  url: string
  icon?: string
  tab?: boolean
  public?: boolean
  /** 是否需要在跳转链接上携带当前老人 elderId */
  elderScoped?: boolean
}

/** 四宫格快捷入口 */
const ICON_ENTRIES: Entry[] = [
  { key: 'intro', title: '养老院介绍', url: '/pages/public/serviceItems/index', icon: iconInstitution, public: true },
  { key: 'bind', title: '绑定家人', url: '/pages/bindList/index', icon: iconFamily },
  { key: 'service', title: '我的服务', url: '/pages/service/index', icon: iconService, tab: true },
  { key: 'bill', title: '我的账单', url: '/pages/bill/index', icon: iconBill, tab: true }
]

/** 彩色功能卡：探访预约 / 参观预约 */
const FEATURE_ENTRIES: (Entry & { icon: string; tone: string })[] = [
  { key: 'visit', title: '探访预约', desc: '老人家预约探访', url: '/pages/visit/index', icon: consultChat, tone: 'green' },
  { key: 'visitRoom', title: '参观预约', desc: '预约参观养老院', url: '/pages/public/rooms/index', icon: consultBook, tone: 'orange', public: true }
]

/** 照护服务入口（登录后展示） */
const CARE_ENTRIES: Entry[] = [
  { key: 'checkin', title: '入住申请', desc: '在线提交审核', url: '/pages/checkinApply/index' },
  { key: 'health', title: '体征查询', desc: '血压心率血糖', url: '/pages/health/index', elderScoped: true },
  { key: 'report', title: '健康报告', desc: 'AI 照护评估', url: '/pages/report/index', elderScoped: true },
  { key: 'leave', title: '请假外出', desc: '申请与审批', url: '/pages/leave/index', elderScoped: true },
  { key: 'care', title: '照护记录', desc: '用药护理日志', url: '/pages/care/index', elderScoped: true },
  { key: 'feedback', title: '投诉建议', desc: '意见直达院区', url: '/pages/feedback/index' }
]

export default function HomePage() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const family = useAuthStore((s) => s.family)
  const elders = useAuthStore((s) => s.elders)
  const [activeId, setActiveId] = useState<number | null>(null)
  const [serviceCount, setServiceCount] = useState(0)
  const [unpaid, setUnpaid] = useState(0)
  const [score, setScore] = useState<number | null>(null)
  const [risk, setRisk] = useState('')
  const [hotRooms, setHotRooms] = useState<RoomView[]>([])

  const [isGuest, setIsGuest] = useState(false)

  const loadSummary = useCallback(async (elderId: number) => {
    try {
      const [services, bills, reports] = await Promise.all([
        getServices(elderId),
        getBills(elderId),
        getReports(elderId)
      ])
      setServiceCount(services.filter((item) => item.status === '2').length)
      setUnpaid(
        bills
          .filter((bill) => bill.payStatus !== '1' && bill.payStatus !== '2')
          .reduce(
            (sum, bill) =>
              sum +
              Number(bill.amount || 0) -
              Number(bill.prepaid || 0) -
              Number(bill.paidAmount || 0) +
              Number(bill.penaltyAmount || 0),
            0
          )
      )
      const latest = reports[0]
      setScore(latest ? latest.score : null)
      setRisk(latest ? latest.riskLevel : '')
    } catch (e) {
      console.error('[Home] 汇总数据加载失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    }
  }, [])

  const loadHotRooms = useCallback(async () => {
    try {
      const list = await getPublicRooms()
      setHotRooms((list || []).slice(0, 3))
    } catch (e) {
      console.warn('[Home] 房型加载失败', e)
    }
  }, [])

  const init = useCallback(async () => {
    hydrate()
    if (!hasAuth()) {
      setIsGuest(true)
      return
    }
    setIsGuest(false)
    try {
      const list = await refreshElders()
      const current = list[0]
      if (current) {
        setActiveId(current.elderId)
        await loadSummary(current.elderId)
      }
    } catch (e) {
      console.error('[Home] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadSummary])

  useEffect(() => {
    init()
    loadHotRooms()
  }, [init, loadHotRooms])

  useDidShow(() => {
    hydrate()
    if (hasAuth()) {
      setIsGuest(false)
      refreshElders().catch((e) => console.warn('[Home] 刷新老人列表失败', e))
    } else {
      setIsGuest(true)
    }
  })

  useEffect(() => {
    if (activeId && activeId !== elders[0]?.elderId) {
      loadSummary(activeId)
    }
  }, [activeId, elders, loadSummary])

  const activeElder = elders.find((item) => item.elderId === activeId) || elders[0]

  const goLogin = () => Taro.navigateTo({ url: '/pages/login/index' })

  const openEntry = (entry: Entry) => {
    if (entry.tab) {
      Taro.switchTab({ url: entry.url })
      return
    }
    if (!entry.public && !hasAuth()) {
      Taro.showModal({
        title: '需要登录',
        content: `「${entry.title}」需要登录家属账号后使用`,
        confirmText: '去登录',
        cancelText: '再想想',
        success: (res) => {
          if (res.confirm) goLogin()
        }
      })
      return
    }
    const url = entry.elderScoped && activeElder
      ? `${entry.url}${entry.url.includes('?') ? '&' : '?'}elderId=${activeElder.elderId}`
      : entry.url
    Taro.navigateTo({ url })
  }

  const goConsult = () => {
    Taro.showActionSheet({
      itemList: ['AI 智能客服', '人工入住申请'],
      success: (res) => {
        if (res.tapIndex === 0) {
          openEntry({ key: 'aiChat', title: 'AI客服', url: '/pages/chat/index' })
        } else if (res.tapIndex === 1) {
          openEntry({ key: 'checkin', title: '入住咨询', url: '/pages/checkinApply/index' })
        }
      }
    })
  }

  return (
    <View className={styles.page}>
      <View className={styles.hero}>
        <Text className={styles.hello}>
          {isGuest ? '欢迎光临' : `您好，${family?.familyName || '家属'}`}
        </Text>
        <Text className={styles.relation}>
          {isGuest
            ? '中州颐养 · 专业照护，值得托付'
            : family?.relation
              ? `我是老人的${family.relation}`
              : '中州颐养 · 家属关爱平台'}
        </Text>
      </View>

      <View className={styles.content}>
        {/* ============ 顶部 Banner ============ */}
        <Image
          className={styles.banner}
          src={bannerImg}
          mode='widthFix'
          onClick={() => Taro.navigateTo({ url: '/pages/public/serviceItems/index' })}
        />

        {/* ============ 入住咨询 ============ */}
        <View className={styles.consultCard}>
          <Image className={styles.consultAvatar} src={consultantImg} mode='aspectFill' />
          <View className={styles.consultInfo}>
            <Text className={styles.consultTitle}>入住咨询</Text>
            <Text className={styles.consultDesc}>贴心服务 · 欢迎咨询</Text>
          </View>
          <View className={styles.consultBtn} onClick={goConsult}>
            <Text className={styles.consultBtnText}>立即咨询</Text>
          </View>
        </View>

        {/* ============ 四宫格 ============ */}
        <View className={styles.iconNav}>
          {ICON_ENTRIES.map((entry) => (
            <View key={entry.key} className={styles.iconNavItem} onClick={() => openEntry(entry)}>
              <Image className={styles.iconNavImage} src={entry.icon || ''} mode='aspectFit' />
              <Text className={styles.iconNavLabel}>{entry.title}</Text>
            </View>
          ))}
        </View>

        {/* ============ 彩色功能卡 ============ */}
        <View className={styles.featureRow}>
          {FEATURE_ENTRIES.map((item) => (
            <View
              key={item.key}
              className={`${styles.featureCard} ${item.tone === 'green' ? styles.featureGreen : styles.featureOrange}`}
              onClick={() => openEntry(item)}
            >
              <View className={styles.featureText}>
                <Text className={styles.featureTitle}>{item.title}</Text>
                <Text className={styles.featureDesc}>{item.desc}</Text>
              </View>
              <Image className={styles.featureIcon} src={item.icon} mode='aspectFit' />
            </View>
          ))}
        </View>

        {/* ============ 登录态：老人档案与数据 ============ */}
        {isGuest ? (
          <View className={styles.guestHero}>
            <Text className={styles.guestTitle}>登录后查看长辈照护数据</Text>
            <Text className={styles.guestDesc}>
              查看绑定老人档案、体征健康、服务记录与费用账单，在线预约探访与入住申请
            </Text>
            <View className={styles.guestLoginBtn} onClick={goLogin}>
              <Text className={styles.guestLoginText}>立即登录家属端</Text>
            </View>
          </View>
        ) : (
          <View>
            <ElderSwitcher elders={elders} activeId={activeId || undefined} onChange={setActiveId} />

            {activeElder ? (
              <ElderCard
                elder={activeElder}
                onClick={() => Taro.navigateTo({ url: `/pages/elder/index?elderId=${activeElder.elderId}` })}
              />
            ) : (
              <View className={styles.noElder}>
                <Text className={styles.noElderText}>暂无绑定老人，请联系机构登记家属关系</Text>
              </View>
            )}

            <View className={styles.statsCard}>
              <View className={styles.statItem}>
                <Text className={styles.statValue}>{serviceCount}</Text>
                <Text className={styles.statLabel}>本月已完成服务</Text>
              </View>
              <View className={styles.statDivider} />
              <View className={styles.statItem}>
                <Text className={styles.statValue}>{unpaid > 0 ? formatMoney(unpaid) : '¥0.00'}</Text>
                <Text className={styles.statLabel}>待缴费用</Text>
              </View>
              <View className={styles.statDivider} />
              <View className={styles.statItem}>
                <Text className={styles.statValue}>{score === null ? '--' : score}</Text>
                <Text className={styles.statLabel}>最新健康评分</Text>
              </View>
            </View>

            {score !== null && (
              <View className={styles.riskRow}>
                <Text className={styles.riskLabel}>综合状态</Text>
                <StatusTag
                  text={riskLevelText(risk)}
                  tone={risk === '0' ? 'success' : risk === '1' ? 'warning' : 'danger'}
                />
              </View>
            )}
          </View>
        )}

        {/* ============ 照护服务 ============ */}
        <View className={styles.sectionHeader}>
          <Text className={styles.sectionTitle}>照护服务</Text>
          <Text className={styles.sectionMore}>入住 · 体征 · 照护 · 反馈</Text>
        </View>
        <View className={styles.healthRow}>
          {CARE_ENTRIES.map((entry) => (
            <View key={entry.key} className={styles.healthCard} onClick={() => openEntry(entry)}>
              <Text className={styles.healthTitle}>{entry.title}</Text>
              <Text className={styles.healthDesc}>{entry.desc}</Text>
            </View>
          ))}
        </View>

        {/* ============ 热门房型 ============ */}
        <View className={styles.sectionHeader}>
          <Text className={styles.sectionTitle}>热门房型</Text>
          <Text
            className={styles.sectionMore}
            onClick={() => Taro.navigateTo({ url: '/pages/public/rooms/index' })}
          >
            全部房型 →
          </Text>
        </View>

        {hotRooms.length === 0 ? (
          <View className={styles.roomEmpty}>
            <Text className={styles.roomEmptyText}>房型信息正在更新，可稍后再来看看</Text>
          </View>
        ) : (
          <View className={styles.roomList}>
            {hotRooms.map((room) => {
              const meta = getRoomTypeMeta(room.roomType)
              return (
                <View
                  key={room.roomId}
                  className={styles.roomCard}
                  onClick={() => Taro.navigateTo({ url: `/pages/public/roomDetail/index?roomId=${room.roomId}` })}
                >
                  <View className={styles.roomCover}>
                    <Image className={styles.roomImage} src={meta.image} mode='widthFix' />
                    <View className={styles.roomTag}>
                      <Text className={styles.roomTagText}>{meta.label}</Text>
                    </View>
                    <View className={styles.roomBadge}>
                      <Text className={styles.roomBadgeText}>
                        {room.availableBeds > 0 ? `空余 ${room.availableBeds} 床` : '已满'}
                      </Text>
                    </View>
                  </View>
                  <View className={styles.roomBody}>
                    <Text className={styles.roomDesc}>
                      我们为长辈提供 {meta.label} 照护空间，{room.floorNo}F · {room.roomNo} 房，共 {room.bedCount} 张床位，配备适老化设施与 24 小时护理响应。
                    </Text>
                    <View className={styles.roomFooter}>
                      <Text className={styles.roomFooterText}>查看床位布局</Text>
                      <Text className={styles.roomFooterArrow}>→</Text>
                    </View>
                  </View>
                </View>
              )
            })}
          </View>
        )}

        <View className={styles.notice}>
          <Text className={styles.noticeTitle}>照护提示</Text>
          <Text className={styles.noticeText}>
            家属端展示的信息由机构实时同步，如有疑问请联系护理站。探访请提前一天预约，到院后配合登记。
          </Text>
        </View>
      </View>
    </View>
  )
}
