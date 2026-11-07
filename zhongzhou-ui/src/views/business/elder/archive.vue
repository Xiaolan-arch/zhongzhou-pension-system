<template>
  <div class="app-container" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="mb10">
      <el-button link icon="Back" @click="goBack">返回老人列表</el-button>
    </div>

    <!-- 老人基本信息卡片 -->
    <el-card v-if="elder" shadow="never" class="elder-header">
      <template #header>
        <div class="elder-title">
          <el-avatar :size="56" :style="{ background: '#2f9b6a' }">{{ elder.elderName?.charAt(0) || '?' }}</el-avatar>
          <div class="elder-info">
            <h2>{{ elder.elderName || '-' }}</h2>
            <div class="elder-meta">
              <el-tag>{{ elder.gender === '0' ? '男' : '女' }}</el-tag>
              <span class="sep">·</span>
              <span>{{ elder.age || '-' }} 岁</span>
              <span class="sep">·</span>
              <span>身份证 {{ elder.idCard?.replace(/^(\d{6})\d+(\d{4})$/, '$1**********$2') || '-' }}</span>
              <span class="sep">·</span>
              <el-tag :type="elderStatusType">{{ elderStatusText }}</el-tag>
            </div>
            <div class="elder-meta">
              <span>联系电话：{{ elder.phone || '-' }}</span>
              <span class="sep">·</span>
              <span>家庭联系人：{{ elder.familyName || '-' }}（{{ elder.familyRelation || '-' }}）{{ elder.familyPhone ? ' / ' + elder.familyPhone : '' }}</span>
            </div>
            <el-alert
              v-if="needAllocateBed"
              class="bed-alert"
              type="warning"
              :closable="false"
              show-icon
              title="该档案尚未分配床位（家属端自助创建或待入住）"
            >
              <template #default>
                请前往「入住办理」完善档案信息并分配房间床位。
                <el-button link type="primary" @click="goCheckinApply">前往入住办理</el-button>
              </template>
            </el-alert>
          </div>
        </div>
      </template>
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="自理能力">{{ selfCareText }}</el-descriptions-item>
        <el-descriptions-item label="护理等级">{{ careLevelText }}</el-descriptions-item>
        <el-descriptions-item label="房间床位">{{ roomBedText }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ formatDate(elder.checkinDate) }}</el-descriptions-item>
        <el-descriptions-item label="精神状态">{{ elder.mentalState || '-' }}</el-descriptions-item>
        <el-descriptions-item label="既往病史" :span="3">{{ elder.disease || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="4">{{ elder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Tab 聚合 -->
    <el-tabs v-model="activeTab" class="elder-tabs">
      <!-- 健康档案 -->
      <el-tab-pane label="健康档案" name="health">
        <el-table :data="healthList" border size="small" style="width: 100%">
          <el-table-column prop="recordDate" label="记录日期" width="120">
            <template #default="scope">{{ formatDate(scope.row.recordDate) }}</template>
          </el-table-column>
          <el-table-column prop="recordType" label="类型" width="90">
            <template #default="scope">{{ healthTypeText(scope.row.recordType) }}</template>
          </el-table-column>
          <el-table-column prop="systolic" label="血压(收缩/舒张)" width="140">
            <template #default="scope">{{ scope.row.systolic || '-' }} / {{ scope.row.diastolic || '-' }}</template>
          </el-table-column>
          <el-table-column prop="heartRate" label="心率" width="80">
            <template #default="scope">{{ scope.row.heartRate ? scope.row.heartRate + ' 次/分' : '-' }}</template>
          </el-table-column>
          <el-table-column prop="bloodSugar" label="血糖" width="80">
            <template #default="scope">{{ scope.row.bloodSugar ? scope.row.bloodSugar + ' mmol/L' : '-' }}</template>
          </el-table-column>
          <el-table-column prop="weight" label="体重" width="80">
            <template #default="scope">{{ scope.row.weight ? scope.row.weight + ' kg' : '-' }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
        </el-table>
        <el-empty v-if="healthList.length === 0" description="暂无健康记录" />
      </el-tab-pane>

      <!-- 服务记录 -->
      <el-tab-pane label="服务订单" name="order">
        <el-table :data="orderList" border size="small" style="width: 100%">
          <el-table-column prop="orderNo" label="订单编号" width="180" />
          <el-table-column prop="itemName" label="服务项目" width="140" />
          <el-table-column prop="serviceDate" label="服务日期" width="120">
            <template #default="scope">{{ formatDate(scope.row.serviceDate) }}</template>
          </el-table-column>
          <el-table-column prop="serviceTime" label="时段" width="100" />
          <el-table-column prop="nurseName" label="护理员" width="110" />
          <el-table-column prop="orderAmount" label="金额" width="90">
            <template #default="scope">{{ money(scope.row.orderAmount) }}</template>
          </el-table-column>
          <el-table-column prop="orderStatus" label="状态" width="90">
            <template #default="scope">
              <el-tag size="small" :type="orderStatusType(scope.row.orderStatus)">{{ orderStatusText(scope.row.orderStatus) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="orderList.length === 0" description="暂无服务订单" />
      </el-tab-pane>

      <!-- 账单历史 -->
      <el-tab-pane label="账单历史" name="bill">
        <el-table :data="billList" border size="small" style="width: 100%">
          <el-table-column prop="billNo" label="账单编号" width="180" />
          <el-table-column prop="billMonth" label="账单月份" width="110" />
          <el-table-column prop="periodStart" label="周期起始" width="110">
            <template #default="scope">{{ formatDate(scope.row.periodStart) }}</template>
          </el-table-column>
          <el-table-column prop="periodEnd" label="周期结束" width="110">
            <template #default="scope">{{ formatDate(scope.row.periodEnd) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="账单金额" width="100">
            <template #default="scope">{{ money(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column prop="paidAmount" label="已收" width="90">
            <template #default="scope" class="text-success">{{ money(scope.row.paidAmount) }}</template>
          </el-table-column>
          <el-table-column prop="payStatus" label="状态" width="90">
            <template #default="scope">
              <dict-tag :options="biz_bill_status" :value="scope.row.payStatus" />
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="billList.length === 0" description="暂无账单" />
      </el-tab-pane>

      <!-- 探访记录 -->
      <el-tab-pane label="探访记录" name="visit">
        <el-table :data="visitList" border size="small" style="width: 100%">
          <el-table-column prop="visitNo" label="预约编号" width="180" />
          <el-table-column prop="familyName" label="家属姓名" width="100" />
          <el-table-column prop="familyRelation" label="关系" width="80" />
          <el-table-column prop="visitDate" label="探访日期" width="120">
            <template #default="scope">{{ formatDate(scope.row.visitDate) }}</template>
          </el-table-column>
          <el-table-column prop="visitTime" label="时段" width="100" />
          <el-table-column prop="visitStatus" label="状态" width="90">
            <template #default="scope">{{ visitStatusText(scope.row.visitStatus) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
        </el-table>
        <el-empty v-if="visitList.length === 0" description="暂无探访记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="ElderArchive">
import { ref, onMounted, computed, watch, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getElder } from '@/api/business/elder';
import { listHealthRecord } from '@/api/business/health';
import { listOrder } from '@/api/business/service';
import { listBill } from '@/api/business/bill';
import { listVisit } from '@/api/business/visit';
import { parseTime } from '@/utils/ruoyi';

const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();
const { biz_bill_status } = proxy.useDict('biz_bill_status');

const loading = ref(false);
const elder = ref(null);
const activeTab = ref('health');
const healthList = ref([]);
const orderList = ref([]);
const billList = ref([]);
const visitList = ref([]);

const elderId = computed(() => Number(route.params.elderId));

// 字段映射
const selfCareText = computed(() => ({'0':'未评估','1':'完全自理','2':'半自理','3':'重度失能','4':'特护'}[elder.value?.selfCare] || '-'));
const careLevelText = computed(() => ({'0':'未评估','1':'完全自理','2':'半自理','3':'重度失能','4':'特护'}[elder.value?.suggestCareLevel] || '-'));
const roomBedText = computed(() => elder.value?.roomNo || elder.value?.bedNo
  ? `${elder.value.roomNo || '-'}房 ${elder.value.bedNo || '-'}床`
  : '未分配');
// 老人状态：0 在住 / 1 已退住 / 2 待入住（与后端 biz_elder.status 保持一致）
const elderStatusText = computed(() => ({ '0': '在住', '1': '已退住', '2': '待入住' }[elder.value?.status] || '未知'));
const elderStatusType = computed(() => (elder.value?.status === '0' ? 'success' : elder.value?.status === '2' ? 'warning' : 'info'));
// 待入住或未分配床位时，提示到「入住办理」补齐档案并分配床位
const needAllocateBed = computed(() => !!elder.value && (elder.value.status === '2' || !elder.value.bedNo));

function loadElder() {
  if (!elderId.value) return;
  getElder(elderId.value).then(response => {
    elder.value = response.data;
  }).catch(() => { proxy.$modal.msgError('老人不存在'); goBack(); });
}

function loadHealth() {
  listHealthRecord({ elderId: elderId.value, pageNum: 1, pageSize: 50 }).then(r => { healthList.value = r.rows || []; }).catch(() => {});
}
function loadOrder() {
  listOrder({ elderId: elderId.value, pageNum: 1, pageSize: 50 }).then(r => { orderList.value = r.rows || []; }).catch(() => {});
}
function loadBill() {
  listBill({ elderId: elderId.value, pageNum: 1, pageSize: 50 }).then(r => { billList.value = r.rows || []; }).catch(() => {});
}
function loadVisit() {
  listVisit({ elderName: elder.value?.elderName, pageNum: 1, pageSize: 50 }).then(r => { visitList.value = r.rows || []; }).catch(() => {});
}

watch(activeTab, (tab) => {
  if (tab === 'health' && healthList.value.length === 0) loadHealth();
  else if (tab === 'order' && orderList.value.length === 0) loadOrder();
  else if (tab === 'bill' && billList.value.length === 0) loadBill();
  else if (tab === 'visit' && visitList.value.length === 0) loadVisit();
});

function goBack() { router.push('/residence/overview'); }
function goCheckinApply() { router.push('/checkin/apply'); }

function formatDate(d) { return d ? parseTime(d, '{y}-{m}-{d}') : '-'; }
function money(v) {
  if (v == null) return '¥0.00';
  const n = Number(v);
  if (isNaN(n)) return '¥0.00';
  return '¥' + n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}
function healthTypeText(t) { return ({'0':'体检','1':'日常监测','2':'就诊记录','3':'用药记录'})[t] || '其他'; }
function orderStatusText(s) { return ({'0':'待执行','1':'已执行','2':'已完成','3':'已取消','4':'已退款'})[s] || s; }
function orderStatusType(s) { return ({'0':'warning','1':'','2':'success','3':'info','4':'danger'})[s] || ''; }
function visitStatusText(s) { return ({'0':'待确认','1':'已确认','2':'已探访','3':'已取消','4':'已拒绝'})[s] || s; }

onMounted(() => {
  loading.value = true;
  Promise.all([
    getElder(elderId.value),
    listHealthRecord({ elderId: elderId.value, pageNum: 1, pageSize: 50 }),
    listOrder({ elderId: elderId.value, pageNum: 1, pageSize: 50 }),
    listBill({ elderId: elderId.value, pageNum: 1, pageSize: 50 }),
  ]).then(([elderResp, healthResp, orderResp, billResp]) => {
    elder.value = elderResp.data;
    healthList.value = healthResp.rows || [];
    orderList.value = orderResp.rows || [];
    billList.value = billResp.rows || [];
    // 探访没 elderId 直接过滤，改用 elderName 查
    if (elder.value?.elderName) {
      listVisit({ elderName: elder.value.elderName, pageNum: 1, pageSize: 50 }).then(r => { visitList.value = r.rows || []; }).catch(() => {});
    }
  }).catch(() => {}).finally(() => { loading.value = false; });
});
</script>

<style scoped lang="scss">
.elder-header { margin-bottom: 16px; }
.elder-title { display: flex; align-items: center; gap: 16px; }
.elder-info h2 { margin: 0 0 6px; color: #1f2d3d; }
.elder-meta { color: #606266; font-size: 13px; margin-top: 3px; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.sep { color: #c0c4cc; }
.elder-tabs { margin-top: 8px; }
.bed-alert { margin-top: 12px; }
.text-success { color: #67c23a; }
</style>
