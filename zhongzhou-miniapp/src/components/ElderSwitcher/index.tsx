import { ScrollView, Text, View } from '@tarojs/components'
import classnames from 'classnames'
import type { ElderVO } from '../../types'
import styles from './index.module.scss'

interface ElderSwitcherProps {
  elders: ElderVO[]
  activeId?: number
  onChange: (elderId: number) => void
}

/** 多老人切换条（仅绑定多位老人时显示） */
export default function ElderSwitcher({ elders, activeId, onChange }: ElderSwitcherProps) {
  if (!elders || elders.length <= 1) return null
  return (
    <ScrollView className={styles.scroll} scrollX enableFlex>
      <View className={styles.row}>
        {elders.map((elder) => (
          <View
            key={elder.elderId}
            className={classnames(styles.chip, elder.elderId === activeId && styles.activeChip)}
            onClick={() => onChange(elder.elderId)}
          >
            <Text className={classnames(styles.chipText, elder.elderId === activeId && styles.activeText)}>
              {elder.elderName}
            </Text>
          </View>
        ))}
      </View>
    </ScrollView>
  )
}
