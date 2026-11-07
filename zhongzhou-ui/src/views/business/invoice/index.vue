<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="账单编号" prop="billNo">
        <el-input v-model="queryParams.billNo" placeholder="请输入账单编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
          <el-option label="待开票" value="0" />
          <el-option label="已开票" value="1" />
          <el-option label="已驳回" value="2" />
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

    <el-table v-loading="loading" :data="invoiceList">
      <el-table-column label="编号" align="center" prop="invoiceId" width="70" />
      <el-table-column label="老人" align="center" prop="elderName" width="90" />
      <el-table-column label="账单编号" align="center" prop="billNo" width="160" show-overflow-tooltip />
      <el-table-column label="发票抬头" align="center" prop="title" min-width="150" show-overflow-tooltip />
      <el-table-column label="税号" align="center" prop="taxNo" width="170" show-overflow-tooltip />
      <el-table-column label="金额" align="center" prop="amount" width="100">
        <template #default="scope">￥{{ scope.row.amount }}</template>
      </el-table-column>
      <el-table-column label="接收邮箱" align="center" prop="email" width="170" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="statusTone(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发票号码" align="center" prop="invoiceNo" width="140" />
      <el-table-column label="操作" width="150" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" link type="primary" icon="Edit" @click="openHandle(scope.row)" v-hasPermi="['business:invoice:handle']">处理</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:invoice:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="处理开票申请" v-model="open" width="500px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="老人">
          <span>{{ handleRow.elderName }}</span>
        </el-form-item>
        <el-form-item label="发票抬头">
          <span>{{ handleRow.title }}</span>
        </el-form-item>
        <el-form-item label="开票金额">
          <span>￥{{ handleRow.amount }}</span>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleForm.status">
            <el-radio label="1">已开票</el-radio>
            <el-radio label="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="handleForm.status === '1'" label="发票号码">
          <el-input v-model="handleForm.invoiceNo" placeholder="请输入发票号码" maxlength="50" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="handleForm.remark" type="textarea" :rows="3" maxlength="500" placeholder="驳回原因或开票备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitHandle">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessInvoice">
import { listInvoice, handleInvoice, delInvoice } from "@/api/business/invoice";

const { proxy } = getCurrentInstance();

const invoiceList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const open = ref(false);
const handleRow = ref({});
const handleForm = ref({ status: "1", invoiceNo: "", remark: "" });

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    elderName: undefined,
    billNo: undefined,
    status: undefined
  }
});
const { queryParams } = toRefs(data);

function statusText(value) {
  if (value === "1") return "已开票";
  if (value === "2") return "已驳回";
  return "待开票";
}
function statusTone(value) {
  if (value === "1") return "success";
  if (value === "2") return "danger";
  return "warning";
}

function getList() {
  loading.value = true;
  listInvoice(queryParams.value).then((response) => {
    invoiceList.value = response.rows;
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

function openHandle(row) {
  handleRow.value = row;
  handleForm.value = { status: "1", invoiceNo: "", remark: "" };
  open.value = true;
}

function submitHandle() {
  if (handleForm.value.status === "1" && !handleForm.value.invoiceNo.trim()) {
    proxy.$modal.msgWarning("请填写发票号码");
    return;
  }
  handleInvoice({
    invoiceId: handleRow.value.invoiceId,
    status: handleForm.value.status,
    invoiceNo: handleForm.value.invoiceNo,
    remark: handleForm.value.remark
  }).then(() => {
    proxy.$modal.msgSuccess("处理成功");
    open.value = false;
    getList();
  });
}

function handleDelete(row) {
  proxy.$modal.confirm("是否确认删除该开票申请？").then(function () {
    return delInvoice(row.invoiceId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
