<template>
  <div class="app-container">
    <el-alert
      title="消息通知为微信订阅消息发送记录。演示环境为 Mock 模式：仅记录发送内容，不真实调用微信接口。"
      type="info" :closable="false" show-icon class="mb8" />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="消息类型" prop="msgType">
        <el-select v-model="queryParams.msgType" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="d in msgTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="发送状态" prop="sendStatus">
        <el-select v-model="queryParams.sendStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in sendStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
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

    <el-table v-loading="loading" :data="messageList" border>
      <el-table-column label="记录ID" prop="logId" width="90" align="center" />
      <el-table-column label="老人姓名" prop="elderName" width="110" align="center" />
      <el-table-column label="家属手机号" prop="phone" width="130" align="center" />
      <el-table-column label="消息类型" prop="msgType" width="110" align="center">
        <template #default="scope">
          <el-tag :type="msgTypeTag(scope.row.msgType)">{{ msgTypeText(scope.row.msgType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="消息标题" prop="title" width="140" show-overflow-tooltip />
      <el-table-column label="消息内容" prop="content" show-overflow-tooltip />
      <el-table-column label="发送状态" prop="sendStatus" width="100" align="center">
        <template #default="scope">
          <el-tag :type="sendStatusTag(scope.row.sendStatus)">{{ sendStatusText(scope.row.sendStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" prop="createTime" width="170" align="center">
        <template #default="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="消息详情" v-model="detailOpen" width="620px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="老人姓名">{{ detail.elderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家属手机号">{{ detail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">{{ msgTypeText(detail.msgType) }}</el-descriptions-item>
        <el-descriptions-item label="发送状态">{{ sendStatusText(detail.sendStatus) }}</el-descriptions-item>
        <el-descriptions-item label="微信 openid" :span="2">{{ detail.openid || '未绑定' }}</el-descriptions-item>
        <el-descriptions-item label="发送时间" :span="2">
          {{ parseTime(detail.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
        <el-descriptions-item label="消息标题" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2">{{ detail.content || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结果说明" :span="2">{{ detail.errorMsg || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessMessage">
import { ref, onMounted, getCurrentInstance } from 'vue';
import { listMessage } from '@/api/business/message';
import { parseTime } from '@/utils/ruoyi';

const { proxy } = getCurrentInstance();
const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const messageList = ref([]);
const detailOpen = ref(false);
const detail = ref({});

const msgTypeOptions = [
  { value: '1', label: '账单到期' },
  { value: '2', label: '逾期提醒' },
  { value: '3', label: '缴费成功' }
];
const sendStatusOptions = [
  { value: '0', label: '成功' },
  { value: '1', label: '失败' },
  { value: '2', label: '跳过' }
];

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  elderName: '',
  msgType: '',
  sendStatus: ''
});

function msgTypeText(t) {
  return msgTypeOptions.find(d => d.value === t)?.label || '-';
}
function msgTypeTag(t) {
  return ({ '1': 'warning', '2': 'danger', '3': 'success' })[t] || 'info';
}
function sendStatusText(s) {
  return sendStatusOptions.find(d => d.value === s)?.label || '-';
}
function sendStatusTag(s) {
  return ({ '0': 'success', '1': 'danger', '2': 'info' })[s] || 'info';
}

function getList() {
  loading.value = true;
  listMessage(queryParams.value).then(res => {
    messageList.value = res.rows;
    total.value = res.total;
  }).finally(() => { loading.value = false; });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
function resetQuery() {
  proxy.resetForm('queryRef');
  handleQuery();
}
function handleDetail(row) {
  detail.value = row;
  detailOpen.value = true;
}

onMounted(getList);
</script>
