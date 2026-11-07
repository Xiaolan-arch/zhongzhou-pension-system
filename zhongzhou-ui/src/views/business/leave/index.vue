<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="leaveType">
        <el-select v-model="queryParams.leaveType" placeholder="请选择类型" clearable style="width: 160px">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 160px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="leaveList">
      <el-table-column label="编号" align="center" prop="leaveId" width="80" />
      <el-table-column label="老人" align="center" prop="elderName" min-width="90" />
      <el-table-column label="类型" align="center" prop="leaveType" width="100">
        <template #default="scope">
          <el-tag :type="typeTone(scope.row.leaveType)" size="small">{{ typeText(scope.row.leaveType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="外出时间" align="center" prop="startTime" width="160" />
      <el-table-column label="预计返回" align="center" prop="endTime" width="160" />
      <el-table-column label="事由" align="center" prop="reason" show-overflow-tooltip min-width="140" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="statusTone(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批意见" align="center" prop="auditOpinion" show-overflow-tooltip min-width="120" />
      <el-table-column label="操作" width="220" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" link type="success" icon="Check" @click="openAudit(scope.row, '1')" v-hasPermi="['business:leave:audit']">通过</el-button>
          <el-button v-if="scope.row.status === '0'" link type="warning" icon="Close" @click="openAudit(scope.row, '2')" v-hasPermi="['business:leave:audit']">驳回</el-button>
          <el-button v-if="scope.row.status === '1'" link type="primary" icon="Finished" @click="handleBack(scope.row)" v-hasPermi="['business:leave:audit']">销假</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:leave:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="auditTitle" v-model="auditOpen" width="480px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="老人">
          <span>{{ auditRow.elderName }}</span>
        </el-form-item>
        <el-form-item label="外出时间">
          <span>{{ auditRow.startTime }} ~ {{ auditRow.endTime }}</span>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditOpinion" type="textarea" :rows="3" maxlength="200" placeholder="请输入审批意见（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAudit">确 定</el-button>
          <el-button @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessLeave">
import { listLeave, auditLeave, backLeave, delLeave } from "@/api/business/leave";

const { proxy } = getCurrentInstance();

const typeOptions = [
  { value: "0", label: "请假" },
  { value: "1", label: "外出就医" },
  { value: "2", label: "回家居住" }
];
const statusOptions = [
  { value: "0", label: "待审批" },
  { value: "1", label: "已通过" },
  { value: "2", label: "已驳回" },
  { value: "3", label: "已销假" },
  { value: "4", label: "已撤销" }
];

const leaveList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const auditOpen = ref(false);
const auditTitle = ref("");
const auditOpinion = ref("");
const auditStatus = ref("1");
const auditRow = ref({});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    elderName: undefined,
    leaveType: undefined,
    status: undefined
  }
});
const { queryParams } = toRefs(data);

function typeText(value) {
  return (typeOptions.find((item) => item.value === value) || {}).label || "-";
}
function typeTone(value) {
  return value === "1" ? "warning" : value === "2" ? "info" : "primary";
}
function statusText(value) {
  return (statusOptions.find((item) => item.value === value) || {}).label || "-";
}
function statusTone(value) {
  if (value === "1" || value === "3") return "success";
  if (value === "2") return "danger";
  if (value === "4") return "info";
  return "warning";
}

function getList() {
  loading.value = true;
  listLeave(queryParams.value).then((response) => {
    leaveList.value = response.rows;
    total.value = response.total;
  }).finally(() => {
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function openAudit(row, status) {
  auditRow.value = row;
  auditStatus.value = status;
  auditOpinion.value = "";
  auditTitle.value = status === "1" ? "审批通过" : "审批驳回";
  auditOpen.value = true;
}

function submitAudit() {
  auditLeave(auditRow.value.leaveId, auditStatus.value, auditOpinion.value).then(() => {
    proxy.$modal.msgSuccess(auditStatus.value === "1" ? "已通过" : "已驳回");
    auditOpen.value = false;
    getList();
  });
}

function handleBack(row) {
  proxy.$modal.confirm('确认老人"' + row.elderName + '"已返回机构并完成销假？').then(function () {
    return backLeave(row.leaveId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("销假成功");
  }).catch(() => {});
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除请假记录"' + row.leaveId + '"？').then(function () {
    return delLeave(row.leaveId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
