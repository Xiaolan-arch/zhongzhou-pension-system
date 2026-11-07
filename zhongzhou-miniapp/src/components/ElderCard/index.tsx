import { Image, Text, View } from '@tarojs/components'
import classnames from 'classnames'
import type { ElderVO } from '../../types'
import { careLevelText, elderStatusText, genderText } from '../../utils/format'
import StatusTag from '../StatusTag'
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

interface ElderCardProps {
  elder: ElderVO
  /** 紧凑模式：用于「我的」页面 */
  compact?: boolean
  onClick?: () => void
}

/** 老人档案卡片 */
export default function ElderCard({ elder, compact = false, onClick }: ElderCardProps) {
  const avatarSrc = AVATARS[elder.elderId % AVATARS.length]
  const statusTone = elder.status === '0' ? 'success' : elder.status === '2' ? 'warning' : 'gray'

  return (
    <View
      className={classnames(styles.card, compact && styles.compact, onClick && styles.pressable)}
      onClick={onClick}
    >
      <Image
        className={styles.avatar}
        src={avatarSrc}
        mode='aspectFill'
        onError={() => console.error('[ElderCard] 头像加载失败', elder.elderId)}
      />
      <View className={styles.info}>
        <View className={styles.nameRow}>
          <Text className={styles.name}>{elder.elderName}</Text>
          <StatusTag text={elderStatusText(elder.status)} tone={statusTone} />
        </View>
        <Text className={styles.meta}>
          {genderText(elder.gender)} · {elder.age}岁 · {careLevelText(elder.careLevel)}
        </Text>
        <View className={styles.locationRow}>
          <Text className={styles.location}>
            {elder.roomNo ? `${elder.floorNo || 1}楼 ${elder.roomNo}房` : '暂未安排房间'}
            {elder.bedNo ? ` · ${elder.bedNo}床` : ''}
          </Text>
          {!compact && !!elder.nurseNames && <Text className={styles.nurse}>护理：{elder.nurseNames}</Text>}
        </View>
      </View>
    </View>
  )
}
