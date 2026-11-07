import { useEffect, useState } from 'react'
import { Image, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getPublicRooms } from '../../../services/family'
import { getRoomTypeMeta } from '../../../utils/roomType'
import type { RoomView } from '../../../types'
import styles from './index.module.scss'

export default function RoomsPage() {
  const [rooms, setRooms] = useState<RoomView[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getPublicRooms()
      .then((d) => { setRooms(d || []); setLoading(false) })
      .catch((e) => { setError(e.message || '加载失败'); setLoading(false) })
  }, [])

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.headerTitle}>房型与床位</Text>
        <Text className={styles.headerDesc}>舒适环境，专业照护，随时有空位</Text>
      </View>

      {loading && <Text className={styles.loading}>加载中…</Text>}
      {error && <Text className={styles.error}>{error}</Text>}
      {!loading && rooms.length === 0 && !error && <Text className={styles.empty}>暂无房型数据</Text>}

      <View className={styles.list}>
        {rooms.map((r) => {
          const meta = getRoomTypeMeta(r.roomType)
          return (
            <View
              key={r.roomId}
              className={styles.roomCard}
              onClick={() => Taro.navigateTo({ url: `/pages/public/roomDetail/index?roomId=${r.roomId}` })}
            >
              <View className={styles.roomCover}>
                <Image className={styles.roomImage} src={meta.image} mode="widthFix" />
                <View className={styles.roomTag}>
                  <Text className={styles.roomTagText}>{meta.label}</Text>
                </View>
                <View className={r.availableBeds > 0 ? styles.roomBadge : styles.roomBadgeFull}>
                  <Text className={styles.roomBadgeText}>
                    {r.availableBeds > 0 ? `空余 ${r.availableBeds} 床` : '已满'}
                  </Text>
                </View>
              </View>
              <View className={styles.roomContent}>
                <Text className={styles.roomDesc}>
                  我们为长辈提供 {meta.label} 照护空间，{r.floorNo}F · {r.roomNo} 房，共 {r.bedCount} 张床位，配备适老化设施与 24 小时护理响应。
                </Text>
                <View className={styles.roomFooter}>
                  <Text className={styles.roomMetaText}>总床位 {r.bedCount} · 空余 {r.availableBeds}</Text>
                  <Text className={styles.detailBtn}>查看布局 →</Text>
                </View>
              </View>
            </View>
          )
        })}
      </View>
    </ScrollView>
  )
}
