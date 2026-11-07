import { useEffect, useState } from 'react'
import { Image, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getPublicServiceItems } from '../../../services/family'
import type { PublicServiceItem } from '../../../types'
import styles from './index.module.scss'

// ====== 本地服务项目图片 ======
import imgBath from '../../../assets/services/助浴@2x.png'
import imgMeal from '../../../assets/services/助餐@2x.png'
import imgWashHair from '../../../assets/services/洗头@2x.png'
import imgNail from '../../../assets/services/修剪指甲@2x.png'
import imgLaundry from '../../../assets/services/衣物清洁@2x.png'
import imgChat from '../../../assets/services/谈心@2x.png'
import imgTurn from '../../../assets/services/翻身拍背@2x.png'
import imgRehab from '../../../assets/services/复建运动@2x.png'
import imgFeet from '../../../assets/services/洗脚@2x.png'
import imgFace from '../../../assets/services/洗脸@2x.png'
import imgBed from '../../../assets/services/整体床铺@2x.png'
import imgClean from '../../../assets/services/公共空间清洁@2x.png'

/** 后端 itemName → 本地图片 */
const SERVICE_IMAGES: Record<string, string> = {
  '助浴': imgBath,
  '助餐': imgMeal,
  '洗头': imgWashHair,
  '修剪指甲': imgNail,
  '衣物清洁': imgLaundry,
  '谈心': imgChat,
  '翻身拍背': imgTurn,
  '复健运动': imgRehab,    // 后端叫「复健运动」，图片叫「复建运动」
  '洗脚': imgFeet,
  '洗脸': imgFace,
  '整体床铺': imgBed,
  '公共空间清洁': imgClean
}

function resolveImage(item: PublicServiceItem): string {
  // 1. 后端 image 字段有值且是 http 链接 → 用它
  if (item.image && item.image.startsWith('http')) return item.image
  // 2. 按 itemName 匹配本地图片
  if (item.itemName && SERVICE_IMAGES[item.itemName]) return SERVICE_IMAGES[item.itemName]
  // 3. 兜底：第一张
  return imgBath
}

export default function ServiceItemsPage() {
  const [items, setItems] = useState<PublicServiceItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getPublicServiceItems()
      .then((data) => {
        setItems(data || [])
        setLoading(false)
      })
      .catch((err) => {
        setError(err.message || '加载失败')
        setLoading(false)
      })
  }, [])

  const goDetail = (id: number) => {
    Taro.navigateTo({ url: `/pages/serviceItemDetail/index?itemId=${id}` })
  }

  return (
    <ScrollView scrollY className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.headerTitle}>专业照护服务</Text>
        <Text className={styles.headerDesc}>日常生活 · 康复护理 · 健康监测，一站式照护</Text>
      </View>

      {loading && <Text className={styles.loading}>加载中…</Text>}
      {error && <Text className={styles.error}>{error}</Text>}
      {!loading && !error && items.length === 0 && (
        <Text className={styles.empty}>暂未上架服务</Text>
      )}

      {!loading && items.length > 0 && (
        <View className={styles.grid}>
          {items.map((it) => (
            <View key={it.itemId} className={styles.card} onClick={() => goDetail(it.itemId)}>
              <Image
                className={styles.cardImage}
                src={resolveImage(it)}
                mode='aspectFill'
              />
              <Text className={styles.cardName}>{it.itemName}</Text>
              <Text className={styles.cardPrice}>
                {it.price ? `¥${it.price.toFixed(0)}` : '待咨询'}
                {it.unit ? ` / ${it.unit}` : ''}
              </Text>
            </View>
          ))}
        </View>
      )}
    </ScrollView>
  )
}
