import { useCallback, useEffect, useState } from 'react'
import { Input, ScrollView, Text, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import classnames from 'classnames'
import ElderSwitcher from '../../components/ElderSwitcher'
import EmptyState from '../../components/EmptyState'
import { getChatMessages, getChatSessions, sendChat } from '../../services/family'
import { hasAuth, useAuthStore } from '../../store/auth'
import type { ChatMessage } from '../../types'
import styles from './index.module.scss'

/** 本地临时消息（未落库前使用负数 ID 避免与后端 msgId 冲突） */
let localMsgSeq = -1

export default function ChatPage() {
  const hydrate = useAuthStore((s) => s.hydrate)
  const refreshElders = useAuthStore((s) => s.refreshElders)
  const elders = useAuthStore((s) => s.elders)

  const [activeElderId, setActiveElderId] = useState<number | null>(null)
  const [sessionId, setSessionId] = useState<number | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [inputValue, setInputValue] = useState('')
  const [sending, setSending] = useState(false)
  const [scrollIntoId, setScrollIntoId] = useState('')

  const scrollToBottom = useCallback(() => {
    // 延迟等待渲染完成后滚动
    setTimeout(() => setScrollIntoId(`msg-${Date.now()}`), 100)
  }, [])

  /** 加载最近一次会话的历史消息 */
  const loadLatest = useCallback(async () => {
    try {
      const sessions = await getChatSessions()
      if (sessions && sessions.length > 0) {
        const latest = sessions[0]
        setSessionId(latest.sessionId)
        const list = await getChatMessages(latest.sessionId)
        setMessages(list || [])
        scrollToBottom()
      }
    } catch (e) {
      console.error('[Chat] 加载会话失败', e)
    }
  }, [scrollToBottom])

  const init = useCallback(async () => {
    hydrate()
    if (!hasAuth()) {
      Taro.redirectTo({ url: '/pages/login/index' })
      return
    }
    try {
      const list = await refreshElders()
      if (list.length > 0) {
        setActiveElderId((prev) => prev ?? list[0].elderId)
      }
      await loadLatest()
    } catch (e) {
      console.error('[Chat] 初始化失败', e)
    }
  }, [hydrate, refreshElders, loadLatest])

  useEffect(() => {
    init()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useDidShow(() => {
    if (hasAuth()) {
      init()
    }
  })

  /** 切换咨询老人：开启新会话，隔离上下文 */
  const handleElderChange = (elderId: number) => {
    setActiveElderId(elderId)
    setSessionId(null)
    setMessages([])
  }

  const handleSend = async () => {
    const content = inputValue.trim()
    if (!content) {
      return
    }
    if (content.length > 500) {
      Taro.showToast({ title: '消息过长，请控制在 500 字以内', icon: 'none' })
      return
    }
    if (sending) {
      return
    }
    setInputValue('')
    setSending(true)

    // 本地先展示用户消息与「正在输入」占位
    const userMsg: ChatMessage = {
      msgId: localMsgSeq--,
      sessionId: sessionId ?? 0,
      role: 'user',
      content
    }
    const thinkingMsg: ChatMessage = {
      msgId: localMsgSeq--,
      sessionId: sessionId ?? 0,
      role: 'assistant',
      content: '正在输入…'
    }
    setMessages((prev) => [...prev, userMsg, thinkingMsg])
    scrollToBottom()

    try {
      const result = await sendChat({
        sessionId: sessionId ?? undefined,
        elderId: activeElderId ?? undefined,
        content
      })
      setSessionId(result.sessionId)
      const replyMsg: ChatMessage = {
        msgId: localMsgSeq--,
        sessionId: result.sessionId,
        role: 'assistant',
        content: result.reply
      }
      // 用真实回复替换「正在输入」占位
      setMessages((prev) => prev.map((m) => (m.msgId === thinkingMsg.msgId ? replyMsg : m)))
    } catch (e) {
      console.error('[Chat] 发送失败', e)
      setMessages((prev) => prev.filter((m) => m.msgId !== thinkingMsg.msgId))
      Taro.showToast({ title: (e as Error).message, icon: 'none' })
    } finally {
      setSending(false)
      scrollToBottom()
    }
  }

  return (
    <View className={styles.page}>
      {elders.length > 1 && (
        <View className={styles.elderBar}>
          <ElderSwitcher elders={elders} activeId={activeElderId} onChange={handleElderChange} />
        </View>
      )}

      <ScrollView
        className={styles.messageList}
        scrollY
        scrollIntoView={scrollIntoId}
        scrollWithAnimation
      >
        {messages.length === 0 ? (
          elders.length === 0 ? (
            <EmptyState text='暂未绑定老人，可先在「我的-绑定管理」中添加' />
          ) : (
            <View className={styles.tips}>
              <Text className={styles.tipsText}>
                您好，我是中州颐养 AI 客服小颐。{'\n'}
                可以问我老人照护、费用账单、服务记录、探访预约等问题～
              </Text>
            </View>
          )
        ) : (
          messages.map((msg) => (
            <View
              key={msg.msgId}
              id={msg.msgId === messages[messages.length - 1]?.msgId ? undefined : undefined}
              className={classnames(styles.row, {
                [styles.rowUser]: msg.role === 'user',
                [styles.rowAssistant]: msg.role !== 'user'
              })}
            >
              {msg.role !== 'user' && (
                <View className={classnames(styles.avatar, styles.avatarAssistant)}>
                  <Text>AI</Text>
                </View>
              )}
              <View
                className={classnames(styles.bubble, {
                  [styles.bubbleUser]: msg.role === 'user',
                  [styles.bubbleAssistant]: msg.role !== 'user',
                  [styles.thinking]: msg.content === '正在输入…'
                })}
              >
                <Text>{msg.content}</Text>
              </View>
              {msg.role === 'user' && (
                <View className={classnames(styles.avatar, styles.avatarUser)}>
                  <Text>我</Text>
                </View>
              )}
            </View>
          ))
        )}
        {/* 滚动锚点 */}
        <View id={scrollIntoId} />
      </ScrollView>

      <View className={styles.inputBar}>
        <Input
          className={styles.input}
          value={inputValue}
          placeholder='请输入想咨询的问题'
          confirmType='send'
          disabled={sending}
          onInput={(e) => setInputValue(e.detail.value)}
          onConfirm={handleSend}
        />
        <View
          className={classnames(styles.sendBtn, { [styles.sendBtnDisabled]: sending })}
          onClick={handleSend}
        >
          <Text>{sending ? '发送中' : '发送'}</Text>
        </View>
      </View>
    </View>
  )
}
