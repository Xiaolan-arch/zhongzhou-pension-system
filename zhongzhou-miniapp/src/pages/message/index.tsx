import { useCallback, useEffect, useState } from 'react'
import { Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import EmptyState from '../../components/EmptyState'
import { getMessages } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { MessageLog } from '../../types'
import { formatDateTime, msgTypeText } from '../../utils/format'
import styles from './index.module.scss'

const FILTERS = [
  { key: 'all', label: '全部' },
  { key: 'bill', label: '账单' },
  { key: 'overdue', label: '逾期' }
]

export default function MessagePage() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const [messages, setMessages] = useState<MessageLog[]>([])
  const [filter, setFilter] = useState('all')

  const init = useCallback(async () => {
    hydrate()
    if (!hasAuth()) {
      Taro.redirectTo({ url: '/pages/login/index' })
      return
    }
    try {
      const data = await getMessages()
      setMessages(data || [])
    } catch (e) {
      console.error('[Message] 加载失败', e)
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    }
  }, [hydrate])

  useEffect(() => {
    init()
  }, [init])

  const visible = messages.filter((item) => {
    if (filter === 'bill') return item.msgType === '1' || item.msgType === '3'
    if (filter === 'overdue') return item.msgType === '2'
    return true
  })

  const handleMessage = (item: MessageLog) => {
    if (item.msgType === '1' || item.msgType === '2' || item.msgType === '3') {
      Taro.switchTab({ url: '/pages/bill/index' })
    }
  }

  return (
    <View className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.title}>消息中心</Text>
        <Text className={styles.subtitle}>账单、逾期与机构通知</Text>
      </View>

      <View className={styles.filters}>
        {FILTERS.map((item) => (
          <Text
            key={item.key}
            className={`${styles.filter} ${filter === item.key ? styles.filterActive : ''}`}
            onClick={() => setFilter(item.key)}
          >
            {item.label}
          </Text>
        ))}
      </View>

      {visible.length === 0 ? (
        <EmptyState text='暂无消息' />
      ) : (
        visible.map((item) => (
          <View key={item.logId} className={styles.card} onClick={() => handleMessage(item)}>
            <View className={styles.cardTop}>
              <Text className={item.msgType === '2' ? styles.typeWarn : styles.type}>
                {msgTypeText(item.msgType)}
              </Text>
              <Text className={styles.time}>{formatDateTime(item.createTime)}</Text>
            </View>
            <Text className={styles.cardTitle}>{item.title}</Text>
            <Text className={styles.content}>{item.content}</Text>
            {!!item.elderName && <Text className={styles.elder}>老人：{item.elderName}</Text>}
          </View>
        ))
      )}

      <View className={styles.notice}>
        <Text className={styles.noticeText}>
          消息记录与微信订阅消息同步。若未收到微信提醒，请在小程序内查看最新通知。
        </Text>
      </View>
    </View>
  )
}
