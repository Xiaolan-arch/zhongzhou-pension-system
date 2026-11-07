<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="d in bizTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="业务ID" prop="bizId">
        <el-input v-model="queryParams.bizId" placeholder="账单/入住ID" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作人" prop="operatorName">
        <el-input v-model="queryParams.operatorName" placeholder="请输入操作人" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker v-model="dateRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 340px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Refresh" @click="getList">刷新</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" border>
      <el-table-column label="日志ID" prop="logId" width="90" align="center" />
      <el-table-column label="业务类型" prop="bizType" width="140" align="center">
        <template #default="scope">
          <el-tag :type="bizTypeTag(scope.row.bizType)">{{ bizTypeText(scope.row.bizType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务ID" prop="bizId" width="90" align="center" />
      <el-table-column label="动作" prop="action" width="140" />
      <el-table-column label="操作人" prop="operatorName" width="120" />
      <el-table-column label="操作时间" prop="createTime" width="170" align="center">
        <template #default="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</template>
      </el-table-column>
      <el-table-column label="变更内容" prop="afterJson" show-overflow-tooltip>
        <template #default="scope">
          <span class="json-preview">{{ compactJson(scope.row.afterJson) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情弹窗 -->
    <el-dialog title="审计日志详情" v-model="detailOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="日志ID">{{ detail.logId }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ bizTypeText(detail.bizType) }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ detail.bizId }}</el-descriptions-item>
        <el-descriptions-item label="动作">{{ detail.action }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ parseTime(detail.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">变更前快照</el-divider>
      <pre class="json-block">{{ prettyJson(detail.beforeJson) || '（无）' }}</pre>
      <el-divider content-position="left">变更后快照</el-divider>
      <pre class="json-block">{{ prettyJson(detail.afterJson) || '（无）' }}</pre>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AuditLog">
import { ref, onMounted, getCurrentInstance } from 'vue';
import { listAuditLog } from '@/api/business/auditLog';
import { parseTime } from '@/utils/ruoyi';

const { proxy } = getCurrentInstance();
const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const logList = ref([]);
const dateRange = ref([]);
const detailOpen = ref(false);
const detail = ref({});

const bizTypeOptions = [
  { value: 'BILL_PAY', label: '账单收款' },
  { value: 'BILL_REFUND', label: '账单退款' },
  { value: 'CHECKIN', label: '入住办理' },
  { value: 'CHECKOUT', label: '退住办理' }
];

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  bizType: '',
  bizId: '',
  operatorName: '',
  params: {}
});

function bizTypeText(t) {
  return bizTypeOptions.find(d => d.value === t)?.label || t || '-';
}
function bizTypeTag(t) {
  return ({ 'BILL_PAY': 'success', 'BILL_REFUND': 'danger', 'CHECKIN': '', 'CHECKOUT': 'warning' })[t] || 'info';
}
function prettyJson(str) {
  if (!str) return '';
  try { return JSON.stringify(JSON.parse(str), null, 2); } catch (e) { return str; }
}
function compactJson(str) {
  if (!str) return '';
  try { return JSON.stringify(JSON.parse(str)); } catch (e) { return str; }
}

function getList() {
  loading.value = true;
  const params = { ...queryParams.value, params: {} };
  if (dateRange.value && dateRange.value.length === 2) {
    params.params.beginTime = dateRange.value[0];
    params.params.endTime = dateRange.value[1];
  }
  listAuditLog(params).then(res => {
    logList.value = res.rows;
    total.value = res.total;
  }).finally(() => { loading.value = false; });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm('queryRef');
  handleQuery();
}
function handleDetail(row) {
  detail.value = row;
  detailOpen.value = true;
}

onMounted(getList);
</script>

<style scoped>
.json-preview { color: #909399; font-size: 12px; }
.json-block {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  max-height: 260px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
