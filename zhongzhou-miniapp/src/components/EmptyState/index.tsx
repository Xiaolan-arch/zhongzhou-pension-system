import { Text, View } from '@tarojs/components'
import styles from './index.module.scss'

interface EmptyStateProps {
  text?: string
}

/** 空状态占位 */
export default function EmptyState({ text = '暂无数据' }: EmptyStateProps) {
  return (
    <View className={styles.empty}>
      <View className={styles.circle}>
        <Text className={styles.mark}>~</Text>
      </View>
      <Text className={styles.text}>{text}</Text>
    </View>
  )
}
