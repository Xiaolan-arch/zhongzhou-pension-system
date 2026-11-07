<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="护理类型" prop="careType">
        <el-select v-model="queryParams.careType" placeholder="请选择护理类型" clearable style="width: 160px">
          <el-option v-for="item in careTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:careLog:add']">新增日志</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="careLogList">
      <el-table-column label="日期" align="center" prop="logDate" width="120" />
      <el-table-column label="老人" align="center" prop="elderName" width="100" />
      <el-table-column label="护理类型" align="center" prop="careType" width="110">
        <template #default="scope">
          <el-tag size="small" :type="careTypeTone(scope.row.careType)">{{ careTypeText(scope.row.careType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="护理内容" align="center" prop="content" show-overflow-tooltip min-width="280" />
      <el-table-column label="护理员" align="center" prop="nurseName" width="110" />
      <el-table-column label="操作" width="150" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:careLog:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:careLog:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="careLogRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="老人" prop="elderId">
          <el-select v-model="form.elderId" placeholder="请选择老人" filterable style="width: 100%">
            <el-option v-for="elder in elderOptions" :key="elder.elderId" :label="elder.elderName" :value="elder.elderId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日志日期" prop="logDate">
          <el-date-picker v-model="form.logDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="护理类型" prop="careType">
          <el-select v-model="form.careType" placeholder="请选择护理类型" style="width: 100%">
            <el-option v-for="item in careTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="护理内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="请输入护理内容" />
        </el-form-item>
        <el-form-item label="护理员" prop="nurseName">
          <el-input v-model="form.nurseName" placeholder="请输入护理员姓名" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessCareLog">
import { listCareLog, getCareLog, addCareLog, updateCareLog, delCareLog } from "@/api/business/careLog";
import { listElder } from "@/api/business/elder";

const { proxy } = getCurrentInstance();

const careTypeOptions = [
  { value: "0", label: "日常照护" },
  { value: "1", label: "饮食" },
  { value: "2", label: "清洁" },
  { value: "3", label: "康复" },
  { value: "4", label: "心理关怀" }
];

const careLogList = ref([]);
const elderOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    elderName: undefined,
    careType: undefined
  },
  rules: {
    elderId: [{ required: true, message: "请选择老人", trigger: "change" }],
    logDate: [{ required: true, message: "请选择日志日期", trigger: "change" }],
    careType: [{ required: true, message: "请选择护理类型", trigger: "change" }],
    content: [{ required: true, message: "护理内容不能为空", trigger: "blur" }]
  }
});
const { queryParams, form, rules } = toRefs(data);

function careTypeText(value) {
  return (careTypeOptions.find((item) => item.value === value) || {}).label || "-";
}
function careTypeTone(value) {
  const tones = { "0": "primary", "1": "success", "2": "info", "3": "warning", "4": "danger" };
  return tones[value] || "info";
}

function getList() {
  loading.value = true;
  listCareLog(queryParams.value).then((response) => {
    careLogList.value = response.rows;
    total.value = response.total;
  }).finally(() => {
    loading.value = false;
  });
}

function getElderOptions() {
  listElder({ pageNum: 1, pageSize: 200 }).then((response) => {
    elderOptions.value = response.rows || [];
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    logId: undefined,
    elderId: undefined,
    logDate: undefined,
    careType: "0",
    content: undefined,
    nurseName: undefined
  };
  proxy.resetForm("careLogRef");
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增护理日志";
}

function handleUpdate(row) {
  reset();
  getCareLog(row.logId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "修改护理日志";
  });
}

function submitForm() {
  proxy.$refs["careLogRef"].validate((valid) => {
    if (valid) {
      const action = form.value.logId ? updateCareLog : addCareLog;
      action(form.value).then(() => {
        proxy.$modal.msgSuccess("保存成功");
        open.value = false;
        getList();
      });
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除"' + row.elderName + '"的护理日志？').then(function () {
    return delCareLog(row.logId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
getElderOptions();
</script>
