import { useEffect, useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getBindings, unbindElder } from '../../services/family'
import { useAuthStore } from '../../store/auth'
import type { FamilyBinding } from '../../types'
import styles from './index.module.scss'

export default function BindListPage() {
  const [list, setList] = useState<FamilyBinding[]>([])
  const [loading, setLoading] = useState(true)
  const refreshElders = useAuthStore((s) => s.refreshElders)

  const load = () => {
    setLoading(true)
    getBindings().then((d) => { setList(d || []); setLoading(false) })
      .catch(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onUnbind = (b: FamilyBinding) => {
    Taro.showModal({
      title: '确认解绑',
      content: `确定要解除与 ${b.elderName || `老人 #${b.elderId}`} 的绑定吗？`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await unbindElder(b.elderId)
          await refreshElders()
          Taro.showToast({ title: '已解绑', icon: 'success' })
          load()
        } catch (err) {
          Taro.showToast({ title: (err as Error).message || '解绑失败', icon: 'none' })
        }
      }
    })
  }

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.newBtn} onClick={() => Taro.navigateTo({ url: '/pages/bind/index' })}>+</View>

      {loading && <Text className={styles.loading}>加载中…</Text>}
      {!loading && list.length === 0 && (
        <Text className={styles.empty}>暂无绑定老人，点击右下角 + 发起绑定</Text>
      )}

      <View className={styles.list}>
        {list.map((b) => {
          const active = b.status === '0'
          return (
            <View key={b.familyId} className={styles.card}>
              <View className={styles.cardRow}>
                <Text className={styles.elder}>{b.elderName || `老人档案 #${b.elderId}`}</Text>
                <Text className={`${styles.tag} ${active ? styles.tag0 : styles.tag1}`}>
                  {active ? '已绑定' : '已解绑'}
                </Text>
              </View>
              <Text className={styles.meta}>
                {b.roomNo ? `${b.floorNo ? `${b.floorNo}楼 ` : ''}${b.roomNo} 房${b.bedNo ? ` · ${b.bedNo} 床` : ''}` : '房号床位待分配'}
              </Text>
              <Text className={styles.meta}>与老人关系：{b.relation}{b.createTime ? ` · 绑定于 ${b.createTime.slice(0, 10)}` : ''}</Text>
              <View className={styles.footer}>
                {active && <Text className={styles.unbindBtn} onClick={() => onUnbind(b)}>解除绑定</Text>}
              </View>
            </View>
          )
        })}
      </View>
    </ScrollView>
  )
}
