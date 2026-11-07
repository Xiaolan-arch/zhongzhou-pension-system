<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="护理员" prop="nurseName">
        <el-input v-model="queryParams.nurseName" placeholder="请输入护理员姓名" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="服务项目" prop="itemName">
        <el-input v-model="queryParams.itemName" placeholder="请输入服务项目" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="任务状态" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务日期">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
          range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px" />
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

    <el-table v-loading="loading" :data="taskList" border>
      <el-table-column label="任务编号" prop="taskId" width="90" align="center" />
      <el-table-column label="订单号" prop="orderNo" width="180" />
      <el-table-column label="老人" prop="elderName" width="100" />
      <el-table-column label="服务项目" prop="itemName" width="130" />
      <el-table-column label="护理员" prop="nurseName" width="100" />
      <el-table-column label="服务日期" prop="serviceDate" width="110" align="center">
        <template #default="scope">{{ parseTime(scope.row.serviceDate, '{y}-{m}-{d}') || '-' }}</template>
      </el-table-column>
      <el-table-column label="计划时间" prop="planTime" width="160" align="center">
        <template #default="scope">{{ parseTime(scope.row.planTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="taskStatus" width="100" align="center">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.taskStatus)">{{ statusText(scope.row.taskStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="执行反馈" prop="feedback" show-overflow-tooltip min-width="180" />
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.taskStatus === '0'" link type="primary" icon="VideoPlay"
            v-hasPermi="['business:serviceTask:edit']" @click="handleStatus(scope.row, '1')">开始执行</el-button>
          <el-button v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'" link type="success" icon="CircleCheck"
            v-hasPermi="['business:serviceTask:edit']" @click="handleStatus(scope.row, '2')">完成</el-button>
          <el-button v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'" link type="info" icon="CircleClose"
            v-hasPermi="['business:serviceTask:edit']" @click="handleStatus(scope.row, '3')">取消</el-button>
          <span v-if="!['0','1'].includes(scope.row.taskStatus)" class="text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 完成护理任务并填写执行反馈 -->
    <el-dialog v-model="feedbackOpen" title="完成护理任务" width="480px" append-to-body :close-on-click-modal="false">
      <el-form :model="feedbackForm" label-width="90px">
        <el-form-item label="服务项目">
          <span>{{ feedbackForm.itemName || '-' }}</span>
        </el-form-item>
        <el-form-item label="执行反馈">
          <el-input v-model="feedbackForm.feedback" type="textarea" :rows="4" maxlength="500" show-word-limit
            placeholder="请填写本次服务执行情况，如老人状态、异常情况等（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="submitFeedback">确 定</el-button>
          <el-button :disabled="submitLoading" @click="feedbackOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ServiceTask">
import { ref, onMounted, getCurrentInstance } from 'vue';
import { listServiceTask, updateServiceTaskStatus } from '@/api/business/serviceTask';
import { parseTime } from '@/utils/ruoyi';

const { proxy } = getCurrentInstance();
const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const taskList = ref([]);
const dateRange = ref([]);
const feedbackOpen = ref(false);
const feedbackForm = ref({ taskId: null, itemName: '', feedback: '' });
const submitLoading = ref(false);

const statusOptions = [
  { value: '0', label: '待执行' },
  { value: '1', label: '已执行' },
  { value: '2', label: '已完成' },
  { value: '3', label: '已取消' },
  { value: '4', label: '已逾期' }
];

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  elderName: '',
  nurseName: '',
  itemName: '',
  taskStatus: '',
  params: {}
});

function statusText(s) {
  return statusOptions.find(d => d.value === s)?.label || s;
}
function statusTagType(s) {
  return ({ '0': 'warning', '1': '', '2': 'success', '3': 'info', '4': 'danger' })[s] || '';
}

function getList() {
  loading.value = true;
  const params = { ...queryParams.value, params: {} };
  if (dateRange.value && dateRange.value.length === 2) {
    params.params.beginTime = dateRange.value[0];
    params.params.endTime = dateRange.value[1];
  }
  listServiceTask(params).then(res => {
    taskList.value = res.rows;
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
  queryParams.value.taskStatus = '';
  handleQuery();
}

function handleStatus(row, status) {
  // 完成时弹出反馈录入弹窗
  if (status === '2') {
    feedbackForm.value = { taskId: row.taskId, itemName: row.itemName, feedback: '' };
    feedbackOpen.value = true;
    return;
  }
  const label = statusText(status);
  proxy.$modal.confirm(`确认将任务「${row.itemName}」标记为${label}吗？`).then(() => {
    return updateServiceTaskStatus(row.taskId, status);
  }).then(() => {
    proxy.$modal.msgSuccess('操作成功');
    getList();
  }).catch(() => {});
}

/** 提交完成状态与执行反馈 */
function submitFeedback() {
  if (submitLoading.value) return;
  submitLoading.value = true;
  updateServiceTaskStatus(feedbackForm.value.taskId, '2', feedbackForm.value.feedback)
    .then(() => {
      proxy.$modal.msgSuccess('任务已完成');
      feedbackOpen.value = false;
      getList();
    })
    .catch(() => {})
    .finally(() => { submitLoading.value = false; });
}

onMounted(getList);
</script>

<style scoped>
.text-muted { color: #c0c4cc; }
</style>
