import { useEffect, useState } from 'react'
import { Button, Image, Text, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import StatusTag from '../../components/StatusTag'
import { getElder } from '../../services/family'
import type { ElderVO } from '../../types'
import { careLevelText, elderStatusText, formatDate, genderText } from '../../utils/format'
import styles from './index.module.scss'
import avatar2 from '../../assets/avatars/elder-2.png'
import avatar3 from '../../assets/avatars/elder-3.png'
import avatar4 from '../../assets/avatars/elder-4.png'
import avatar5 from '../../assets/avatars/elder-5.png'
import avatar6 from '../../assets/avatars/elder-6.png'
import avatar7 from '../../assets/avatars/elder-7.png'
import avatar8 from '../../assets/avatars/elder-8.png'
import avatar9 from '../../assets/avatars/elder-9.png'
import avatar10 from '../../assets/avatars/elder-10.png'

/** 本地老人头像，按 elderId 取模分配 */
const AVATARS = [avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatar9, avatar10]

export default function ElderPage() {
  const router = useRouter()
  const elderId = Number(router.params.elderId || 0)
  const [elder, setElder] = useState<ElderVO | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      if (!elderId) {
        setError('缺少老人档案参数')
        setLoading(false)
        return
      }
      setLoading(true)
      try {
        const data = await getElder(elderId)
        setElder(data)
        Taro.setNavigationBarTitle({ title: `${data.elderName || '老人'}档案` })
      } catch (e) {
        console.error('[Elder] 加载老人档案失败', e)
        setError((e as Error).message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [elderId])

  if (loading) {
    return (
      <View className={styles.state}>
        <Text className={styles.stateText}>档案加载中...</Text>
      </View>
    )
  }

  if (!elder) {
    return (
      <View className={styles.state}>
        <Text className={styles.stateText}>{error || '老人档案不存在'}</Text>
        <Button className={styles.backButton} onClick={() => Taro.navigateBack()}>
          返回
        </Button>
      </View>
    )
  }

  const statusTone = elder.status === '0' ? 'success' : elder.status === '2' ? 'warning' : 'gray'
  const avatarSrc = AVATARS[elder.elderId % AVATARS.length]
  const roomText = elder.roomNo ? `${elder.floorNo || 1} 楼 · ${elder.roomNo} 房间` : '暂未安排房间'

  return (
    <View className={styles.page}>
      <View className={styles.hero}>
        <Image
          className={styles.avatar}
          src={avatarSrc}
          mode='aspectFill'
          onError={() => console.error('[Elder] 头像加载失败', elder.elderId)}
        />
        <View className={styles.heroInfo}>
          <View className={styles.nameRow}>
            <Text className={styles.name}>{elder.elderName}</Text>
            <StatusTag text={elderStatusText(elder.status)} tone={statusTone} />
          </View>
          <Text className={styles.meta}>
            {genderText(elder.gender)} · {elder.age} 岁 · {careLevelText(elder.careLevel)}
          </Text>
          <Text className={styles.location}>
            {roomText}
            {elder.bedNo ? ` · ${elder.bedNo} 床` : ''}
          </Text>
        </View>
      </View>

      <View className={styles.content}>
        <View className={styles.card}>
          <Text className={styles.cardTitle}>基础档案</Text>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>性别</Text>
            <Text className={styles.rowValue}>{genderText(elder.gender)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>年龄</Text>
            <Text className={styles.rowValue}>{elder.age} 岁</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>出生日期</Text>
            <Text className={styles.rowValue}>{formatDate(elder.birthday)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>护理等级</Text>
            <Text className={styles.rowValue}>{careLevelText(elder.careLevel)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>入住日期</Text>
            <Text className={styles.rowValue}>{formatDate(elder.checkinDate)}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>联系电话</Text>
            <Text className={styles.rowValue}>{elder.phone || '--'}</Text>
          </View>
        </View>

        <View className={styles.card}>
          <Text className={styles.cardTitle}>在住信息</Text>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>所在楼层</Text>
            <Text className={styles.rowValue}>{elder.floorNo ? `${elder.floorNo} 楼` : '--'}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>房间号</Text>
            <Text className={styles.rowValue}>{elder.roomNo || '--'}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>床位号</Text>
            <View style={{ flex: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text className={styles.rowValue}>{elder.bedNo || '--'}</Text>
              {elder.roomNo && (
                <Text
                  style={{ fontSize: 24, color: '#2fa36b' }}
                  onClick={() => Taro.navigateTo({ url: `/pages/elderBed/index?elderId=${elder.elderId}` })}
                >
                  查看布局 →
                </Text>
              )}
            </View>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>责任护理员</Text>
            <Text className={styles.rowValue}>{elder.nurseNames || '待分配'}</Text>
          </View>
        </View>

        <View className={styles.card}>
          <Text className={styles.cardTitle}>家属信息</Text>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>家属姓名</Text>
            <Text className={styles.rowValue}>{elder.familyName || '--'}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>与老人关系</Text>
            <Text className={styles.rowValue}>{elder.familyRelation || '--'}</Text>
          </View>
          <View className={styles.row}>
            <Text className={styles.rowLabel}>家庭住址</Text>
            <Text className={styles.rowValue}>{elder.address || '--'}</Text>
          </View>
        </View>

        <View className={styles.actions}>
          <Button
            className={styles.actionPrimary}
            onClick={() => Taro.navigateTo({ url: `/pages/report/index?elderId=${elder.elderId}` })}
          >
            查看健康报告
          </Button>
          <Button
            className={styles.actionSecond}
            onClick={() => Taro.navigateTo({ url: `/pages/visit/index?elderId=${elder.elderId}` })}
          >
            预约探访
          </Button>
        </View>

        <Text className={styles.tip}>档案信息由机构维护，如有疑问请联系护理站核对。</Text>
      </View>
    </View>
  )
}
