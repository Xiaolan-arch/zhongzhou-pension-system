<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="药品名称" prop="medicineName">
        <el-input v-model="queryParams.medicineName" placeholder="请输入药品名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
          <el-option label="进行中" value="0" />
          <el-option label="已停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:medication:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="medicationList">
      <el-table-column label="编号" align="center" prop="medicationId" width="80" />
      <el-table-column label="老人" align="center" prop="elderName" min-width="90" />
      <el-table-column label="药品名称" align="center" prop="medicineName" min-width="150" show-overflow-tooltip />
      <el-table-column label="用量" align="center" prop="dosage" width="110" />
      <el-table-column label="频次" align="center" prop="frequency" width="100" />
      <el-table-column label="服用时间" align="center" prop="timePoints" width="130" />
      <el-table-column label="开始日期" align="center" prop="startDate" width="110" />
      <el-table-column label="结束日期" align="center" prop="endDate" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '进行中' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用药说明" align="center" prop="remark" show-overflow-tooltip min-width="150" />
      <el-table-column label="操作" width="150" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:medication:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:medication:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="medicationRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="老人" prop="elderId">
          <el-select v-model="form.elderId" placeholder="请选择老人" filterable style="width: 100%">
            <el-option v-for="elder in elderOptions" :key="elder.elderId" :label="elder.elderName" :value="elder.elderId" />
          </el-select>
        </el-form-item>
        <el-form-item label="药品名称" prop="medicineName">
          <el-input v-model="form.medicineName" placeholder="请输入药品名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="每次用量" prop="dosage">
          <el-input v-model="form.dosage" placeholder="如 5mg（1片）" maxlength="50" />
        </el-form-item>
        <el-form-item label="服用频次" prop="frequency">
          <el-select v-model="form.frequency" placeholder="请选择服用频次" style="width: 100%">
            <el-option label="每日1次" value="每日1次" />
            <el-option label="每日2次" value="每日2次" />
            <el-option label="每日3次" value="每日3次" />
            <el-option label="按需服用" value="按需服用" />
          </el-select>
        </el-form-item>
        <el-form-item label="服用时间" prop="timePoints">
          <el-input v-model="form.timePoints" placeholder="如 08:00,18:00" maxlength="100" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">进行中</el-radio>
            <el-radio label="1">已停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用药说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" maxlength="500" placeholder="请输入用药说明" />
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

<script setup name="BusinessMedication">
import { listMedication, getMedication, addMedication, updateMedication, delMedication } from "@/api/business/medication";
import { listElder } from "@/api/business/elder";

const { proxy } = getCurrentInstance();

const medicationList = ref([]);
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
    medicineName: undefined,
    status: undefined
  },
  rules: {
    elderId: [{ required: true, message: "请选择老人", trigger: "change" }],
    medicineName: [{ required: true, message: "药品名称不能为空", trigger: "blur" }],
    frequency: [{ required: true, message: "请选择服用频次", trigger: "change" }],
    timePoints: [{ required: true, message: "请填写服用时间", trigger: "blur" }]
  }
});
const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listMedication(queryParams.value).then((response) => {
    medicationList.value = response.rows;
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
    medicationId: undefined,
    elderId: undefined,
    medicineName: undefined,
    dosage: undefined,
    frequency: "每日1次",
    timePoints: "08:00",
    startDate: undefined,
    endDate: undefined,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("medicationRef");
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
  title.value = "新增用药提醒";
}

function handleUpdate(row) {
  reset();
  getMedication(row.medicationId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = "修改用药提醒";
  });
}

function submitForm() {
  proxy.$refs["medicationRef"].validate((valid) => {
    if (valid) {
      const action = form.value.medicationId ? updateMedication : addMedication;
      action(form.value).then(() => {
        proxy.$modal.msgSuccess("保存成功");
        open.value = false;
        getList();
      });
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除用药记录"' + row.medicineName + '"？').then(function () {
    return delMedication(row.medicationId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
getElderOptions();
</script>
