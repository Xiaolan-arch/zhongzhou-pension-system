import { useEffect, useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { cancelCheckin, getCheckins } from '../../services/family'
import type { CheckinApply } from '../../types'
import styles from './index.module.scss'

const STATUS_TEXT: Record<string, string> = {
  '0': '申请中', '1': '待评估', '2': '待审核', '3': '待配置',
  '4': '待签约', '5': '待缴费', '6': '已入住', '9': '已撤销'
}

const CANCELABLE = new Set(['0', '1', '9'])

export default function CheckinListPage() {
  const [list, setList] = useState<CheckinApply[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    getCheckins()
      .then((d) => { setList(d || []); setLoading(false) })
      .catch((e) => { setError(e.message || '加载失败'); setLoading(false) })
  }

  useEffect(() => { load() }, [])

  const onCancel = (id: number, applyNo: string) => {
    Taro.showModal({
      title: '确认撤销',
      content: `确认撤销申请 ${applyNo}？`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await cancelCheckin(id)
          Taro.showToast({ title: '已撤销', icon: 'success' })
          load()
        } catch (err) {
          Taro.showToast({ title: (err as Error).message || '撤销失败', icon: 'none' })
        }
      }
    })
  }

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.newBtn} onClick={() => Taro.navigateTo({ url: '/pages/checkinApply/index' })}>
        +
      </View>

      {loading && <Text className={styles.loading}>加载中…</Text>}
      {error && <Text className={styles.error}>{error}</Text>}
      {!loading && !error && list.length === 0 && (
        <Text className={styles.empty}>暂无入住申请，点击右下角 + 发起</Text>
      )}

      <View className={styles.list}>
        {list.map((c) => {
          const cls = CANCELABLE.has(c.applyStatus) ? '9' : c.applyStatus
          return (
            <View key={c.checkinId} className={styles.card} onClick={() => Taro.navigateTo({ url: `/pages/checkinDetail/index?checkinId=${c.checkinId}` })}>
              <View className={styles.cardRow}>
                <Text className={styles.elder}>{c.elderName}（{Number(c.age)}岁）</Text>
                <Text className={`${styles.tag} ${styles[`tag${cls}`]}`}>{STATUS_TEXT[c.applyStatus] || '处理中'}</Text>
              </View>
              <Text className={styles.meta}>申请号 {c.applyNo} · {c.gender === '0' ? '男' : '女'}</Text>
              {c.createTime && <Text className={styles.meta}>提交于 {c.createTime}</Text>}
              <View className={styles.cardFooter}>
                <Text className={styles.link}>查看详情 →</Text>
                {CANCELABLE.has(c.applyStatus) && (
                  <Text className={styles.cancelBtn} onClick={(e) => { e.stopPropagation(); onCancel(c.checkinId, c.applyNo) }}>
                    撤销
                  </Text>
                )}
              </View>
            </View>
          )
        })}
      </View>
    </ScrollView>
  )
}

