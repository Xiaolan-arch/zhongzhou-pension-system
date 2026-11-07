import { useCallback, useEffect, useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getCheckinDetail, signCheckin } from '../../services/family'
import type { CheckinApply } from '../../types'
import styles from './index.module.scss'

const STEPS = [
  { code: '0', label: '申请中' },
  { code: '1', label: '待评估' },
  { code: '2', label: '待审核' },
  { code: '3', label: '待配置' },
  { code: '4', label: '待签约' },
  { code: '5', label: '待缴费' },
  { code: '6', label: '已入住' }
]

export default function CheckinDetailPage() {
  const [c, setC] = useState<CheckinApply | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const checkinId = Number(Taro.getCurrentInstance().router?.params?.checkinId || 0)

  const load = useCallback(() => {
    if (!checkinId) {
      setError('申请ID缺失')
      setLoading(false)
      return
    }
    getCheckinDetail(checkinId)
      .then((d) => { setC(d); setError(''); setLoading(false) })
      .catch((e) => { setError(e.message || '加载失败'); setLoading(false) })
  }, [checkinId])

  useEffect(() => {
    load()
  }, [load])

  const handleSign = () => {
    if (!c || submitting) return
    Taro.showModal({
      title: '确认签约',
      content: '请在确认已阅读并同意《入住服务协议》及费用标准后完成签约，签约后将进入首期缴费环节。',
      confirmText: '确认签约',
      cancelText: '再想想',
      success: async (res) => {
        if (!res.confirm) return
        setSubmitting(true)
        try {
          await signCheckin(c.checkinId)
          Taro.showToast({ title: '签约成功', icon: 'success' })
          load()
        } catch (e) {
          Taro.showToast({ title: (e as Error).message || '签约失败', icon: 'none' })
        } finally {
          setSubmitting(false)
        }
      }
    })
  }

  const handleContract = () => {
    if (!c?.contractFile) return
    Taro.setClipboardData({ data: c.contractFile })
      .then(() => Taro.showToast({ title: '合同链接已复制', icon: 'none' }))
      .catch(() => Taro.showToast({ title: '复制失败，请联系护理站', icon: 'none' }))
  }

  if (loading) return <ScrollView scrollY className={styles.page}><Text>加载中…</Text></ScrollView>
  if (error || !c) return <ScrollView scrollY className={styles.page}><Text>{error || '申请不存在'}</Text></ScrollView>

  // 驳回/撤销态显示反馈
  const isRejected = c.applyStatus === '9'
  const hasAssessment = !!(c.assessmentResult || c.suggestCareLevel || c.assessor)
  const hasConfig = !!(c.careLevel || c.feeStandard || c.bedId)
  const hasContract = !!(c.contractNo || c.contractStart || c.signDate)

  return (
    <ScrollView scrollY className={styles.page}>
      {/* 步骤条 */}
      <View className={styles.card}>
        <Text className={styles.cardTitle}>办理进度</Text>
        <View className={styles.steps}>
          {STEPS.map((s, idx) => {
            const curIdx = STEPS.findIndex((x) => x.code === c.applyStatus)
            const done = curIdx > idx
            const current = curIdx === idx
            return (
              <View key={s.code} className={`${styles.stepItem} ${done ? styles.stepDone : ''} ${current ? styles.stepCurrent : ''}`}>
                <View className={styles.stepDot}>{idx + 1}</View>
                <Text className={styles.stepLabel}>{s.label}</Text>
                {idx < STEPS.length - 1 && <View className={`${styles.stepLine} ${done ? styles.stepDoneLine : ''}`} />}
              </View>
            )
          })}
        </View>
        {isRejected && c.auditOpinion && (
          <View className={styles.feedbackBox}>
            <Text className={styles.feedbackTitle}>撤销说明</Text>
            <Text className={styles.feedbackText}>{c.auditOpinion}</Text>
          </View>
        )}
        {c.applyStatus === '5' && (
          <View className={styles.tipBox}>
            <Text className={styles.tipText}>合同已签署，请前往「我的账单」完成首期缴费</Text>
          </View>
        )}
      </View>

      {/* 老人信息 */}
      <View className={styles.card}>
        <Text className={styles.cardTitle}>老人信息</Text>
        <View className={styles.infoRow}><Text className={styles.infoKey}>姓名</Text><Text className={styles.infoVal}>{c.elderName}</Text></View>
        <View className={styles.infoRow}><Text className={styles.infoKey}>性别</Text><Text className={styles.infoVal}>{c.gender === '0' ? '男' : '女'}</Text></View>
        <View className={styles.infoRow}><Text className={styles.infoKey}>年龄</Text><Text className={styles.infoVal}>{c.age} 岁</Text></View>
        <View className={styles.infoRow}><Text className={styles.infoKey}>联系电话</Text><Text className={styles.infoVal}>{c.phone}</Text></View>
        {c.address && <View className={styles.infoRow}><Text className={styles.infoKey}>地址</Text><Text className={styles.infoVal}>{c.address}</Text></View>}
      </View>

      {/* 家属信息 */}
      {(c.familyName || c.familyPhone || c.familyRelation) && (
        <View className={styles.card}>
          <Text className={styles.cardTitle}>家属信息</Text>
          {c.familyName && <View className={styles.infoRow}><Text className={styles.infoKey}>姓名</Text><Text className={styles.infoVal}>{c.familyName}</Text></View>}
          {c.familyRelation && <View className={styles.infoRow}><Text className={styles.infoKey}>与老人关系</Text><Text className={styles.infoVal}>{c.familyRelation}</Text></View>}
          {c.familyPhone && <View className={styles.infoRow}><Text className={styles.infoKey}>联系电话</Text><Text className={styles.infoVal}>{c.familyPhone}</Text></View>}
        </View>
      )}

      {/* 健康情况 */}
      {(c.selfCare || c.mentalState || c.disease) && (
        <View className={styles.card}>
          <Text className={styles.cardTitle}>健康情况</Text>
          {c.selfCare && <View className={styles.infoRow}><Text className={styles.infoKey}>自理能力</Text><Text className={styles.infoVal}>{c.selfCare}</Text></View>}
          {c.mentalState && <View className={styles.infoRow}><Text className={styles.infoKey}>精神状态</Text><Text className={styles.infoVal}>{c.mentalState}</Text></View>}
          {c.disease && <View className={styles.infoRow}><Text className={styles.infoKey}>既往病史</Text><Text className={styles.infoVal}>{c.disease}</Text></View>}
        </View>
      )}

      {/* 入住评估 */}
      {hasAssessment && (
        <View className={styles.card}>
          <Text className={styles.cardTitle}>入住评估</Text>
          {c.assessmentResult && (
            <View className={styles.infoRow}><Text className={styles.infoKey}>评估结论</Text><Text className={styles.infoVal}>{c.assessmentResult}</Text></View>
          )}
          {c.suggestCareLevel && (
            <View className={styles.infoRow}><Text className={styles.infoKey}>建议护理等级</Text><Text className={styles.infoVal}>{c.suggestCareLevel}</Text></View>
          )}
          {c.assessor && (
            <View className={styles.infoRow}><Text className={styles.infoKey}>评估人</Text><Text className={styles.infoVal}>{c.assessor}</Text></View>
          )}
          {c.assessTime && (
            <View className={styles.infoRow}><Text className={styles.infoKey}>评估时间</Text><Text className={styles.infoVal}>{c.assessTime}</Text></View>
          )}
        </View>
      )}

      {/* 入住配置 */}
      {hasConfig && (
        <View className={styles.card}>
          <Text className={styles.cardTitle}>入住配置</Text>
          {c.careLevel && <View className={styles.infoRow}><Text className={styles.infoKey}>护理等级</Text><Text className={styles.infoVal}>{c.careLevel}</Text></View>}
          {c.feeStandard !== undefined && c.feeStandard !== null && (
            <View className={styles.infoRow}><Text className={styles.infoKey}>费用标准</Text><Text className={styles.infoVal}>¥{c.feeStandard} / 月</Text></View>
          )}
          {c.bedId && <View className={styles.infoRow}><Text className={styles.infoKey}>床位</Text><Text className={styles.infoVal}>已分配</Text></View>}
          {c.checkinDate && <View className={styles.infoRow}><Text className={styles.infoKey}>入住日期</Text><Text className={styles.infoVal}>{c.checkinDate}</Text></View>}
        </View>
      )}

      {/* 合同信息 */}
      {hasContract && (
        <View className={styles.card}>
          <Text className={styles.cardTitle}>合同信息</Text>
          {c.contractNo && <View className={styles.infoRow}><Text className={styles.infoKey}>合同编号</Text><Text className={styles.infoVal}>{c.contractNo}</Text></View>}
          {c.contractStart && (
            <View className={styles.infoRow}>
              <Text className={styles.infoKey}>合同期限</Text>
              <Text className={styles.infoVal}>{c.contractStart} 至 {c.contractEnd || '长期'}</Text>
            </View>
          )}
          {c.signDate && <View className={styles.infoRow}><Text className={styles.infoKey}>签约日期</Text><Text className={styles.infoVal}>{c.signDate}</Text></View>}
          {c.contractFile && (
            <View className={styles.infoRow}>
              <Text className={styles.infoKey}>合同文件</Text>
              <Text className={styles.linkText} onClick={handleContract}>查看合同附件</Text>
            </View>
          )}
        </View>
      )}

      {/* 申请信息 */}
      <View className={styles.card}>
        <Text className={styles.cardTitle}>申请信息</Text>
        <View className={styles.infoRow}><Text className={styles.infoKey}>申请单号</Text><Text className={styles.infoVal}>{c.applyNo}</Text></View>
        {c.createTime && <View className={styles.infoRow}><Text className={styles.infoKey}>提交时间</Text><Text className={styles.infoVal}>{c.createTime}</Text></View>}
      </View>

      {/* 待签约操作 */}
      {c.applyStatus === '4' && (
        <View className={styles.actionBar}>
          <View className={`${styles.primaryBtn} ${submitting ? styles.primaryBtnDisabled : ''}`} onClick={handleSign}>
            <Text className={styles.primaryBtnText}>{submitting ? '签约中…' : '确认签约'}</Text>
          </View>
          <Text className={styles.actionTip}>请核对合同期限与费用标准，确认后即完成电子签约</Text>
        </View>
      )}
    </ScrollView>
  )
}
