import { useEffect, useState } from 'react'
import { Image, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getPublicRoomDetail } from '../../../services/family'
import { getRoomTypeMeta } from '../../../utils/roomType'
import type { RoomDetail } from '../../../types'
import styles from './index.module.scss'

const STATUS_LABEL: Record<string, string> = { '0': '空闲', '1': '已入住', '2': '外出中', '3': '维修' }

export default function RoomDetailPage() {
  const [data, setData] = useState<RoomDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const roomId = Number(Taro.getCurrentInstance().router?.params?.roomId || 0)
    if (!roomId) { setError('房型ID缺失'); setLoading(false); return }
    getPublicRoomDetail(roomId)
      .then((d) => { setData(d); setLoading(false) })
      .catch((e) => { setError(e.message || '加载失败'); setLoading(false) })
  }, [])

  if (loading) return <ScrollView scrollY className={styles.page}><Text className={styles.stateText}>加载中…</Text></ScrollView>
  if (error || !data) return <ScrollView scrollY className={styles.page}><Text className={styles.stateText}>{error || '房型不存在'}</Text></ScrollView>

  const r = data.room
  const meta = getRoomTypeMeta(r.roomType)
  const beds = data.beds || []
  const available = data.availableBeds ?? beds.filter((b) => b.status === '0').length
  const occupied = beds.filter((b) => b.status === '1').length
  const outing = beds.filter((b) => b.status === '2').length
  const repair = beds.filter((b) => b.status === '3').length

  return (
    <ScrollView scrollY className={styles.page}>
      <Image className={styles.roomImage} src={meta.image} mode="widthFix" />

      <View className={styles.banner}>
        <Text className={styles.bannerTitle}>{meta.label} · {r.roomNo}</Text>
        <Text className={styles.bannerMeta}>
          {r.floorNo}F 共 {r.bedCount} 床 · 空余 <Text className={styles.bannerStrong}>{available}</Text> 床
        </Text>
      </View>

      <View className={styles.stats}>
        <View className={styles.statItem}>
          <Text className={styles.statValue}>{r.bedCount}</Text>
          <Text className={styles.statLabel}>总床位</Text>
        </View>
        <View className={styles.statItem}>
          <Text className={`${styles.statValue} ${styles.statAvailable}`}>{available}</Text>
          <Text className={styles.statLabel}>空闲</Text>
        </View>
        <View className={styles.statItem}>
          <Text className={styles.statValue}>{occupied}</Text>
          <Text className={styles.statLabel}>已入住</Text>
        </View>
        <View className={styles.statItem}>
          <Text className={styles.statValue}>{outing + repair}</Text>
          <Text className={styles.statLabel}>外出/维修</Text>
        </View>
      </View>

      <View className={styles.legend}>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.legendDot0}`} /><Text>空闲</Text></View>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.legendDot1}`} /><Text>已入住</Text></View>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.legendDot2}`} /><Text>外出中</Text></View>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.legendDot3}`} /><Text>维修</Text></View>
      </View>

      {beds.length === 0 ? (
        <View className={styles.empty}>
          <Text className={styles.emptyTitle}>暂无床位信息</Text>
          <Text className={styles.emptyDesc}>该房型的床位尚未配置，可联系护理站咨询</Text>
        </View>
      ) : (
        <View className={styles.grid}>
          {beds.map((b) => (
            <View key={b.bedId} className={`${styles.bed} ${styles[`bed${b.status}`] || styles.bed0}`}>
              <Text className={styles.bedNo}>{b.bedNo}</Text>
              <Text className={styles.bedStatus}>{STATUS_LABEL[b.status] || '未知'}</Text>
              {b.elderName && <Text className={styles.bedElder}>{b.elderName}</Text>}
            </View>
          ))}
        </View>
      )}

      <View className={styles.footer}>
        <Text>如需预定床位，请登录小程序后提交入住申请</Text>
      </View>
    </ScrollView>
  )
}
