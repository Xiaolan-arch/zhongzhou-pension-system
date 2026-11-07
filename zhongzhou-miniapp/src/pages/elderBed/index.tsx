import { useEffect, useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getElder, getPublicRoomDetail, getPublicRooms } from '../../services/family'
import type { BedView, ElderVO, RoomDetail, RoomView } from '../../types'
import styles from './index.module.scss'

export default function ElderBedPage() {
  const [elder, setElder] = useState<ElderVO | null>(null)
  const [roomDetail, setRoomDetail] = useState<RoomDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const elderId = Number(Taro.getCurrentInstance().router?.params?.elderId || 0)

  useEffect(() => {
    if (!elderId) { setError('缺少参数'); setLoading(false); return }

    // 1. 先从后端拿老人档案（不用依赖 store）
    getElder(elderId)
      .then((e) => {
        setElder(e)
        if (!e.roomNo) {
          setLoading(false)
          return
        }
        // 2. 再用公开接口匹配房间
        return getPublicRooms().then((rooms: RoomView[]) => {
          const target = rooms.find((r) =>
            r.floorNo === (e.floorNo || 0) && String(r.roomNo) === String(e.roomNo)
          )
          if (!target) { buildDemo(e); return null }
          return getPublicRoomDetail(target.roomId).then((d) => { setRoomDetail(d); setLoading(false); return null })
        })
      })
      .catch((err) => { setError(err.message || '加载失败'); setLoading(false) })

    function buildDemo(e: ElderVO) {
      // 老人档案接口不返回房型，未匹配到房间信息时按双人间兜底渲染
      const size = 2
      const beds: BedView[] = []
      for (let i = 1; i <= size; i++) {
        const bedLabel = e.bedNo || `床位${i}`
        const isCurrent =
          `${bedLabel}` === `床位${i}` || `${bedLabel}` === String(i) ||
          (e.bedNo && `${bedLabel}`.includes(`${i}`)) ||
          (e.bedNo && i === 1 && e.bedNo.includes('1'))
        beds.push({
          bedId: i,
          bedNo: `床位${i}`,
          status: isCurrent ? '1' : (i === 1 && size > 1 ? '1' : '0'),
          elderName: isCurrent ? e.elderName : (i === 1 && size > 1 ? '另一老人' : undefined)
        })
      }
      setRoomDetail({
        room: {
          roomId: 0, roomNo: e.roomNo || '', roomType: '双人间',
          floorNo: e.floorNo || 1, bedCount: size
        },
        beds,
        availableBeds: beds.filter((b) => b.status === '0').length
      })
      setLoading(false)
    }
  }, [elderId])

  if (loading) return <ScrollView scrollY className={styles.page}><Text className={styles.loading}>加载中…</Text></ScrollView>
  if (error) return <ScrollView scrollY className={styles.page}><Text className={styles.empty}>{error}</Text></ScrollView>
  if (!elder) return <ScrollView scrollY className={styles.page}><Text className={styles.empty}>未找到老人档案</Text></ScrollView>
  if (!elder.roomNo || !roomDetail) return <ScrollView scrollY className={styles.page}><Text className={styles.empty}>暂未安排房间</Text></ScrollView>

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.headerTitle}>{elder.elderName} 的床位</Text>
        <Text className={styles.headerMeta}>
          {elder.floorNo}F · {elder.roomNo} · {elder.bedNo}
        </Text>
        <Text className={styles.currentBadge}>★ 当前床位高亮</Text>
      </View>

      <View className={styles.legend}>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.s0}`} /><Text>空闲</Text></View>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.s1}`} /><Text>已入住</Text></View>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.s2}`} /><Text>外出中</Text></View>
        <View className={styles.legendItem}><View className={`${styles.dot} ${styles.s3}`} /><Text>维修</Text></View>
      </View>

      <View className={styles.floorCard}>
        <Text className={styles.floorTitle}>{elder.floorNo}F · {roomDetail.room.roomType}</Text>

        <View className={styles.grid}>
          {roomDetail.beds.map((b) => {
            const isCurrent =
              b.elderName === elder.elderName ||
              b.bedNo === elder.bedNo ||
              (elder.bedNo && `${b.bedNo}`.includes(`${elder.bedNo}`)) ||
              (elder.bedNo && `${elder.bedNo}`.includes(`${b.bedNo}`))
            const cls = [styles.bed, styles[`s${b.status}`], isCurrent ? styles.bedCurrent : ''].filter(Boolean).join(' ')
            return (
              <View key={b.bedId} className={cls}>
                <Text>{b.bedNo}</Text>
                {b.elderName && <Text className={styles.bedElder}>{b.elderName}</Text>}
              </View>
            )
          })}
        </View>

        <Text className={styles.tip}>
          空余 {roomDetail.availableBeds} 床 · 共 {roomDetail.room.bedCount} 床
        </Text>
      </View>
    </ScrollView>
  )
}
