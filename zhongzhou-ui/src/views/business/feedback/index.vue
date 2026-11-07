<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="类型" prop="feedbackType">
        <el-select v-model="queryParams.feedbackType" placeholder="请选择类型" clearable style="width: 140px">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
          <el-option label="待处理" value="0" />
          <el-option label="已回复" value="2" />
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

    <el-table v-loading="loading" :data="feedbackList">
      <el-table-column label="编号" align="center" prop="feedbackId" width="70" />
      <el-table-column label="老人" align="center" prop="elderName" width="90" />
      <el-table-column label="家属" align="center" width="140">
        <template #default="scope">
          {{ scope.row.familyName || "-" }}<br />
          <span class="text-muted">{{ scope.row.familyPhone }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="feedbackType" width="80">
        <template #default="scope">
          <el-tag :type="typeTone(scope.row.feedbackType)" size="small">{{ typeText(scope.row.feedbackType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="反馈内容" align="center" prop="content" show-overflow-tooltip min-width="220" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === '2' ? 'success' : 'warning'" size="small">
            {{ scope.row.status === '2' ? '已回复' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="回复内容" align="center" prop="reply" show-overflow-tooltip min-width="200" />
      <el-table-column label="提交时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" width="150" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="ChatDotRound" @click="openReply(scope.row)" v-hasPermi="['business:feedback:reply']">回复</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:feedback:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="回复家属反馈" v-model="open" width="520px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="家属">
          <span>{{ replyRow.familyName || "-" }}（{{ replyRow.familyPhone }}）</span>
        </el-form-item>
        <el-form-item label="反馈内容">
          <span>{{ replyRow.content }}</span>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input v-model="replyContent" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReply">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessFeedback">
import { listFeedback, replyFeedback, delFeedback } from "@/api/business/feedback";

const { proxy } = getCurrentInstance();

const typeOptions = [
  { value: "0", label: "投诉" },
  { value: "1", label: "建议" },
  { value: "2", label: "表扬" }
];

const feedbackList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const open = ref(false);
const replyContent = ref("");
const replyRow = ref({});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    feedbackType: undefined,
    status: undefined
  }
});
const { queryParams } = toRefs(data);

function typeText(value) {
  return (typeOptions.find((item) => item.value === value) || {}).label || "-";
}
function typeTone(value) {
  if (value === "0") return "danger";
  if (value === "2") return "success";
  return "primary";
}

function getList() {
  loading.value = true;
  listFeedback(queryParams.value).then((response) => {
    feedbackList.value = response.rows;
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

function openReply(row) {
  replyRow.value = row;
  replyContent.value = row.reply || "";
  open.value = true;
}

function submitReply() {
  if (!replyContent.value.trim()) {
    proxy.$modal.msgWarning("请填写回复内容");
    return;
  }
  replyFeedback({ feedbackId: replyRow.value.feedbackId, reply: replyContent.value }).then(() => {
    proxy.$modal.msgSuccess("回复成功");
    open.value = false;
    getList();
  });
}

function handleDelete(row) {
  proxy.$modal.confirm("是否确认删除该条反馈？").then(function () {
    return delFeedback(row.feedbackId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
