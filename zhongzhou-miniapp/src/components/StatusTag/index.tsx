import { Text, View } from '@tarojs/components'
import classnames from 'classnames'
import styles from './index.module.scss'

export type TagTone = 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'gray'

interface StatusTagProps {
  text: string
  tone?: TagTone
}

/** 状态标签 */
export default function StatusTag({ text, tone = 'gray' }: StatusTagProps) {
  return (
    <View className={classnames(styles.tag, styles[tone])}>
      <Text className={styles.text}>{text}</Text>
    </View>
  )
}
