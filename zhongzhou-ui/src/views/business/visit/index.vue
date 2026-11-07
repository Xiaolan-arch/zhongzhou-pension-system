<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="来访人" prop="visitorName">
        <el-input
          v-model="queryParams.visitorName"
          placeholder="请输入来访人姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来访类型" prop="visitType">
        <el-select v-model="queryParams.visitType" placeholder="请选择来访类型" clearable style="width: 200px">
          <el-option
            v-for="dict in biz_visit_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option
            v-for="dict in biz_visit_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['business:visit:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="visitList">
      <el-table-column label="来访ID" align="center" prop="visitId" width="90" />
      <el-table-column label="类型" align="center" prop="visitType" width="90">
        <template #default="scope">
          <dict-tag :options="biz_visit_type" :value="scope.row.visitType" />
        </template>
      </el-table-column>
      <el-table-column label="来访人" align="center" prop="visitorName" min-width="100" />
      <el-table-column label="电话" align="center" prop="visitorPhone" width="130" />
      <el-table-column label="探访老人" align="center" prop="elderName" min-width="100" />
      <el-table-column label="来访日期" align="center" prop="visitDate" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.visitDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来访时间" align="center" prop="visitTime" width="100" />
      <el-table-column label="人数" align="center" prop="peopleCount" width="80" />
      <el-table-column label="预约码" align="center" prop="visitCode" width="120" />
      <el-table-column label="核销时间" align="center" prop="checkinTime" width="160" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="biz_visit_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip min-width="120" />
      <el-table-column label="操作" width="330" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="success"
            icon="Check"
            @click="handleConfirm(scope.row)"
            v-hasPermi="['business:visit:confirm']"
          >确认</el-button>
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '1'"
            link
            type="primary"
            icon="Finished"
            @click="handleCheckin(scope.row)"
            v-hasPermi="['business:visit:checkin']"
          >核销</el-button>
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '1'"
            link
            type="warning"
            icon="CircleClose"
            @click="handleCancel(scope.row)"
            v-hasPermi="['business:visit:confirm']"
          >取消</el-button>
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['business:visit:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['business:visit:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改来访对话框 -->
    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="visitRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="来访类型" prop="visitType">
          <el-radio-group v-model="form.visitType" @change="handleTypeChange">
            <el-radio
              v-for="dict in biz_visit_type"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="来访人" prop="visitorName">
          <el-input v-model="form.visitorName" placeholder="请输入来访人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="visitorPhone">
          <el-input v-model="form.visitorPhone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item v-if="form.visitType === '1'" label="探访老人" prop="elderId">
          <el-select v-model="form.elderId" placeholder="请选择探访老人" filterable clearable style="width: 100%">
            <el-option
              v-for="elder in elderOptions"
              :key="elder.elderId"
              :label="elder.elderName"
              :value="elder.elderId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="来访日期" prop="visitDate">
          <el-date-picker
            v-model="form.visitDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择来访日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="来访时间" prop="visitTime">
          <el-time-select
            v-model="form.visitTime"
            start="00:00"
            step="00:15"
            end="23:45"
            placeholder="请选择来访时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="来访人数" prop="peopleCount">
          <el-input-number v-model="form.peopleCount" :min="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

<script setup name="BusinessVisit">
import { listVisit, getVisit, addVisit, updateVisit, delVisit, confirmVisit, cancelVisit, checkinVisit } from "@/api/business/visit";
import { listElder } from "@/api/business/elder";

const { proxy } = getCurrentInstance();
const { biz_visit_type, biz_visit_status } = proxy.useDict("biz_visit_type", "biz_visit_status");

const visitList = ref([]);
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
    visitorName: undefined,
    visitType: undefined,
    status: undefined
  },
  rules: {
    visitType: [{ required: true, message: "请选择来访类型", trigger: "change" }],
    visitorName: [{ required: true, message: "来访人不能为空", trigger: "blur" }],
    visitorPhone: [
      { required: true, message: "联系电话不能为空", trigger: "blur" },
      { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }
    ],
    elderId: [{ validator: validateElder, trigger: "change" }],
    visitDate: [{ required: true, message: "请选择来访日期", trigger: "change" }],
    visitTime: [{ required: true, message: "请选择来访时间", trigger: "change" }],
    peopleCount: [{ required: true, message: "来访人数不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 探访类型校验探访老人 */
function validateElder(rule, value, callback) {
  if (form.value.visitType === "1" && !value) {
    callback(new Error("请选择探访老人"));
  } else {
    callback();
  }
}

/** 查询来访列表 */
function getList() {
  loading.value = true;
  listVisit(queryParams.value).then(response => {
    visitList.value = response.rows;
    total.value = response.total;
  }).finally(() => {
    loading.value = false;
  });
}

/** 查询老人下拉数据 */
function getElderOptions() {
  listElder({ pageNum: 1, pageSize: 100 }).then(response => {
    elderOptions.value = response.rows || [];
  });
}

/** 来访类型切换 */
function handleTypeChange(value) {
  if (value !== "1") {
    form.value.elderId = undefined;
  }
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    visitId: undefined,
    visitType: "0",
    visitorName: undefined,
    visitorPhone: undefined,
    elderId: undefined,
    elderName: undefined,
    visitDate: undefined,
    visitTime: undefined,
    peopleCount: 1,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("visitRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加来访";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  getVisit(row.visitId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改来访";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["visitRef"].validate(valid => {
    if (valid) {
      if (form.value.visitId != undefined) {
        updateVisit(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addVisit(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 确认来访 */
function handleConfirm(row) {
  proxy.$modal.confirm('是否确认来访编号为"' + row.visitId + '"的数据项？').then(function() {
    return confirmVisit(row.visitId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("确认成功");
  }).catch(() => {});
}

/** 到访核销 */
function handleCheckin(row) {
  proxy.$modal.confirm('确认来访人"' + row.visitorName + '"已到院并完成核销？').then(function() {
    return checkinVisit(row.visitId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("核销成功");
  }).catch(() => {});
}

/** 取消来访 */
function handleCancel(row) {
  proxy.$modal.confirm('是否确认取消来访编号为"' + row.visitId + '"的数据项？').then(function() {
    return cancelVisit(row.visitId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("取消成功");
  }).catch(() => {});
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除来访编号为"' + row.visitId + '"的数据项？').then(function() {
    return delVisit(row.visitId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
getElderOptions();
</script>
