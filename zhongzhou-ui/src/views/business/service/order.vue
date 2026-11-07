<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input
          v-model="queryParams.elderName"
          placeholder="请输入老人姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option
            v-for="dict in biz_order_status"
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
          v-hasPermi="['business:service:order:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          :type="myView ? 'success' : 'default'"
          :icon="myView ? 'UserFilled' : 'User'"
          @click="toggleMyView"
          plain
        >{{ myView ? '护理员视角（我的待办）' : '全部订单' }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery" />
    </el-row>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="订单号" align="center" prop="orderNo" width="170" show-overflow-tooltip />
      <el-table-column label="老人" align="center" prop="elderName" min-width="100" />
      <el-table-column label="服务项目" align="center" prop="itemName" min-width="110" />
      <el-table-column label="护理员" align="center" prop="nurseName" min-width="100">
        <template #default="scope">{{ scope.row.nurseName || "-" }}</template>
      </el-table-column>
      <el-table-column label="服务时间" align="center" prop="serviceTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.serviceTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="金额" align="center" prop="amount" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="biz_order_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="打卡记录" align="center" width="220">
        <template #default="scope">
          <div class="checkin-cell">
            <div v-if="scope.row.checkinTime" class="checkin-line">
              <span class="checkin-label text-success">签到</span>
              {{ parseTime(scope.row.checkinTime, '{m}-{d} {h}:{i}') }}
              <span class="checkin-op">· {{ scope.row.checkinOperator }}</span>
            </div>
            <div v-else-if="scope.row.status === '0'" class="text-gray">待签到</div>
            <div v-if="scope.row.finishTime" class="checkin-line">
              <span class="checkin-label text-primary">完成</span>
              {{ parseTime(scope.row.finishTime, '{m}-{d} {h}:{i}') }}
              <span class="checkin-op">· {{ scope.row.finishOperator }}</span>
            </div>
            <el-tooltip v-if="scope.row.serviceRemark" :content="scope.row.serviceRemark" placement="top">
              <div class="service-remark">📝 {{ scope.row.serviceRemark }}</div>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="290" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="success"
            icon="Location"
            @click="handleExecute(scope.row)"
            v-hasPermi="['business:service:order:execute']"
          >签到</el-button>
          <el-button
            v-if="scope.row.status === '1'"
            link
            type="success"
            icon="CircleCheck"
            @click="handleComplete(scope.row)"
            v-hasPermi="['business:service:order:execute']"
          >完成</el-button>
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['business:service:order:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['business:service:order:remove']"
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

    <!-- 添加或修改服务订单对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="老人" prop="elderId">
          <el-select
            v-model="form.elderId"
            placeholder="请选择老人"
            filterable
            clearable
            style="width: 100%"
            @change="handleElderChange"
          >
            <el-option
              v-for="elder in elderOptions"
              :key="elder.elderId"
              :label="elder.elderName"
              :value="elder.elderId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务项目" prop="itemId">
          <el-select
            v-model="form.itemId"
            placeholder="请选择服务项目"
            filterable
            clearable
            style="width: 100%"
            @change="handleItemChange"
          >
            <el-option
              v-for="item in itemOptions"
              :key="item.itemId"
              :label="item.itemName"
              :value="item.itemId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="护理员" prop="nurseId">
          <el-select
            v-model="form.nurseId"
            placeholder="请选择护理员"
            filterable
            clearable
            style="width: 100%"
            @change="handleNurseChange"
          >
            <el-option
              v-for="nurse in nurseOptions"
              :key="nurse.nurseId"
              :label="nurse.nurseName"
              :value="nurse.nurseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务时间" prop="serviceTime">
          <el-date-picker
            v-model="form.serviceTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择服务时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
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

<script setup name="BusinessServiceOrder">
import {
  listOrder,
  getOrder,
  addOrder,
  updateOrder,
  delOrder,
  executeOrder,
  completeOrder,
  listMyOrders,
  listItem,
  listNurse
} from "@/api/business/service";
import { listElder } from "@/api/business/elder";

const { proxy } = getCurrentInstance();
const { biz_order_status } = proxy.useDict("biz_order_status");

const orderList = ref([]);
const elderOptions = ref([]);
const itemOptions = ref([]);
const nurseOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");
const myView = ref(false);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    elderName: undefined,
    status: undefined
  },
  rules: {
    elderId: [{ required: true, message: "请选择老人", trigger: "change" }],
    itemId: [{ required: true, message: "请选择服务项目", trigger: "change" }],
    serviceTime: [{ required: true, message: "请选择服务时间", trigger: "change" }],
    amount: [{ required: true, message: "金额不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询服务订单列表 */
function getList() {
  loading.value = true;
  if (myView.value) {
    listMyOrders().then(response => {
      orderList.value = response.data || [];
      total.value = orderList.value.length;
    }).finally(() => {
      loading.value = false;
    });
    return;
  }
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows;
    total.value = response.total;
  }).finally(() => {
    loading.value = false;
  });
}

/** 护理员视角切换 */
function toggleMyView() {
  myView.value = !myView.value;
  getList();
}

/** 加载下拉数据 */
function loadOptions() {
  listElder({ pageNum: 1, pageSize: 100 }).then(response => {
    elderOptions.value = response.rows || [];
  });
  listItem({ pageNum: 1, pageSize: 100 }).then(response => {
    itemOptions.value = response.rows || [];
  });
  listNurse({ pageNum: 1, pageSize: 100 }).then(response => {
    nurseOptions.value = response.rows || [];
  });
}

/** 老人选择变化 */
function handleElderChange(elderId) {
  const elder = elderOptions.value.find(item => item.elderId === elderId);
  form.value.elderName = elder ? elder.elderName : undefined;
}

/** 服务项目选择变化，自动带出项目名称和金额 */
function handleItemChange(itemId) {
  const item = itemOptions.value.find(row => row.itemId === itemId);
  if (item) {
    form.value.itemName = item.itemName;
    form.value.amount = item.price != null ? Number(item.price) : 0;
  } else {
    form.value.itemName = undefined;
  }
}

/** 护理员选择变化 */
function handleNurseChange(nurseId) {
  const nurse = nurseOptions.value.find(item => item.nurseId === nurseId);
  form.value.nurseName = nurse ? nurse.nurseName : undefined;
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    orderId: undefined,
    orderNo: undefined,
    elderId: undefined,
    elderName: undefined,
    itemId: undefined,
    itemName: undefined,
    nurseId: undefined,
    nurseName: undefined,
    serviceTime: undefined,
    amount: 0,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("orderRef");
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
  title.value = "添加服务订单";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  getOrder(row.orderId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改服务订单";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["orderRef"].validate(valid => {
    if (valid) {
      if (form.value.orderId != undefined) {
        updateOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 签到打卡 */
function handleExecute(row) {
  proxy.$modal.confirm(
    '是否为订单"' + row.orderNo + '"执行签到打卡？打卡后系统将自动记录签到时间和操作人。'
  ).then(function() {
    return executeOrder(row.orderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("签到成功");
  }).catch(() => {});
}

/** 完成服务（可填写服务记录） */
function handleComplete(row) {
  proxy.$prompt(
    '请输入服务记录/备注（可选），完成后系统将自动记录完成时间和操作人：',
    '完成订单 "' + row.orderNo + '"',
    {
      confirmButtonText: '确 认完成',
      cancelButtonText: '取 消',
      inputType: 'textarea',
      inputPlaceholder: '如：老人反馈良好，按时服药，血压稳定...',
      inputMaxlength: 200,
      inputValidator: (value) => {
        if (value !== undefined && value !== null && value.length > 200) return '最多 200 字';
        return true;
      }
    }
  ).then(({ value }) => {
    return completeOrder(row.orderId, value || "");
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("完成成功");
  }).catch(() => {});
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除订单编号为"' + row.orderNo + '"的数据项？').then(function() {
    return delOrder(row.orderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
loadOptions();
</script>

<style scoped>
.checkin-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
  padding: 0 4px;
}
.checkin-line {
  font-size: 12px;
  line-height: 1.5;
}
.checkin-label {
  display: inline-block;
  width: 36px;
  font-weight: 600;
}
.checkin-op {
  color: #909399;
  font-size: 11px;
}
.service-remark {
  font-size: 11px;
  color: #606266;
  background: #f5f7fa;
  border-radius: 3px;
  padding: 1px 6px;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}
.text-success { color: #2f9b6a; font-weight: 600; }
.text-primary { color: #409eff; font-weight: 600; }
.text-gray { color: #c0c4cc; font-size: 12px; }
</style>
