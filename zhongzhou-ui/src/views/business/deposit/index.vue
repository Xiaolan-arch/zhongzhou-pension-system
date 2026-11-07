<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="depositType">
        <el-select v-model="queryParams.depositType" placeholder="请选择类型" clearable style="width: 180px">
          <el-option v-for="dict in biz_deposit_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openCollect" v-hasPermi="['business:deposit:add']">收押金</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="RefreshLeft" @click="openRefund" v-hasPermi="['business:deposit:refund']">退押金</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="depositList">
      <el-table-column label="记录ID" align="center" prop="depositId" width="80" />
      <el-table-column label="老人姓名" align="center" prop="elderName" width="110" />
      <el-table-column label="类型" align="center" prop="depositType" width="100">
        <template #default="scope">
          <dict-tag :options="biz_deposit_type" :value="scope.row.depositType" />
        </template>
      </el-table-column>
      <el-table-column label="金额" align="center" prop="amount" width="120">
        <template #default="scope">
          <span :class="scope.row.depositType === '1' ? 'text-success' : 'text-warning'">
            {{ scope.row.depositType === '1' ? '+' : '-' }}{{ money(scope.row.amount) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" width="100">
        <template #default="scope">{{ dictLabel(biz_pay_method, scope.row.payMethod) }}</template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operatorName" width="100" />
      <el-table-column label="操作时间" align="center" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip min-width="150" />
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 收押金 -->
    <el-dialog v-model="collectOpen" title="收押金" width="520px" append-to-body :close-on-click-modal="false">
      <el-form ref="collectRef" :model="collectForm" :rules="rules" label-width="90px">
        <el-form-item label="老人" prop="elderId">
          <el-select v-model="collectForm.elderId" placeholder="请选择老人" filterable clearable style="width: 100%" @change="handleElderChange(collectForm)">
            <el-option v-for="elder in elderOptions" :key="elder.elderId" :label="elder.elderName" :value="elder.elderId" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前余额">
          <span class="text-success bold">{{ money(currentBalance) }}</span>
        </el-form-item>
        <el-form-item label="押金金额" prop="amount">
          <el-input-number v-model="collectForm.amount" :min="0.01" :step="1000" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-radio-group v-model="collectForm.payMethod">
            <el-radio v-for="dict in biz_pay_method" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="collectForm.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="可选，如：入住押金" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="submitCollect">确 定</el-button>
          <el-button :disabled="submitLoading" @click="collectOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退押金 -->
    <el-dialog v-model="refundOpen" title="退押金" width="520px" append-to-body :close-on-click-modal="false">
      <el-form ref="refundRef" :model="refundForm" :rules="rules" label-width="90px">
        <el-form-item label="老人" prop="elderId">
          <el-select v-model="refundForm.elderId" placeholder="请选择老人" filterable clearable style="width: 100%" @change="handleElderChange(refundForm)">
            <el-option v-for="elder in elderOptions" :key="elder.elderId" :label="elder.elderName" :value="elder.elderId" />
          </el-select>
        </el-form-item>
        <el-form-item label="可退余额">
          <span class="text-danger bold">{{ money(currentBalance) }}</span>
        </el-form-item>
        <el-form-item label="退款金额" prop="amount">
          <el-input-number
            v-model="refundForm.amount"
            :min="0.01"
            :max="Math.max(currentBalance, 0.01)"
            :step="100"
            :precision="2"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="可选，如：退住结算退还押金" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="submitRefund">确 定</el-button>
          <el-button :disabled="submitLoading" @click="refundOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessDeposit">
import { listDeposit, getDepositBalance, collectDeposit, refundDeposit } from "@/api/business/deposit";
import { listElder } from "@/api/business/elder";

const { proxy } = getCurrentInstance();
const { biz_deposit_type, biz_pay_method } = proxy.useDict("biz_deposit_type", "biz_pay_method");

const depositList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const elderOptions = ref([]);
const currentBalance = ref(0);
const submitLoading = ref(false);

const queryParams = ref({ pageNum: 1, pageSize: 10, elderName: undefined, depositType: undefined });

const collectOpen = ref(false);
const collectForm = ref({ elderId: undefined, elderName: undefined, amount: 10000, payMethod: undefined, remark: "" });

const refundOpen = ref(false);
const refundForm = ref({ elderId: undefined, elderName: undefined, amount: undefined, remark: "" });

const rules = {
  elderId: [{ required: true, message: "请选择老人", trigger: "change" }],
  amount: [{ required: true, message: "请输入金额", trigger: "blur" }],
  payMethod: [{ required: true, message: "请选择支付方式", trigger: "change" }]
};

function money(value) {
  const num = Number(value);
  return isNaN(num) ? "0.00" : num.toFixed(2);
}
function dictLabel(options, value) {
  if (value === undefined || value === null || value === "") return "-";
  const list = Array.isArray(options) ? options : (options.value || []);
  const item = list.find(dict => dict.value === String(value));
  return item ? item.label : String(value);
}

/** 查询押金记录 */
function getList() {
  loading.value = true;
  listDeposit(queryParams.value).then(response => {
    depositList.value = response.rows;
    total.value = response.total;
  }).catch(() => {}).finally(() => { loading.value = false; });
}
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.elderName = undefined;
  queryParams.value.depositType = undefined;
  handleQuery();
}

/** 老人下拉（在住老人） */
function loadElders() {
  listElder({ pageNum: 1, pageSize: 500, status: "0" }).then(response => {
    elderOptions.value = response.rows || [];
  }).catch(() => {});
}

/** 选择老人后刷新余额 */
function handleElderChange(form) {
  const elder = elderOptions.value.find(item => item.elderId === form.elderId);
  form.elderName = elder ? elder.elderName : undefined;
  currentBalance.value = 0;
  if (!form.elderId) return;
  getDepositBalance(form.elderId).then(response => {
    currentBalance.value = Number(response.balance) || 0;
  }).catch(() => {});
}

function openCollect() {
  currentBalance.value = 0;
  collectForm.value = { elderId: undefined, elderName: undefined, amount: 10000, payMethod: undefined, remark: "" };
  collectOpen.value = true;
}
function submitCollect() {
  proxy.$refs["collectRef"].validate(valid => {
    if (!valid || submitLoading.value) return;
    submitLoading.value = true;
    collectDeposit({ ...collectForm.value }).then(() => {
      proxy.$modal.msgSuccess("收押金成功");
      collectOpen.value = false;
      getList();
    }).catch(() => {}).finally(() => { submitLoading.value = false; });
  });
}

function openRefund() {
  currentBalance.value = 0;
  refundForm.value = { elderId: undefined, elderName: undefined, amount: undefined, remark: "" };
  refundOpen.value = true;
}
function submitRefund() {
  proxy.$refs["refundRef"].validate(valid => {
    if (!valid || submitLoading.value) return;
    const amount = Number(refundForm.value.amount);
    if (!amount || amount <= 0) { proxy.$modal.msgWarning("退款金额必须大于 0"); return; }
    if (amount > currentBalance.value) { proxy.$modal.msgWarning("退款金额不能超过可退余额"); return; }
    submitLoading.value = true;
    refundDeposit({ ...refundForm.value }).then(() => {
      proxy.$modal.msgSuccess("退押金成功");
      refundOpen.value = false;
      getList();
    }).catch(() => {}).finally(() => { submitLoading.value = false; });
  });
}

getList();
loadElders();
</script>

<style scoped>
.text-success { color: #2f9b6a; font-weight: bold; }
.text-warning { color: #e6a23c; font-weight: bold; }
.text-danger { color: #f56c6c; font-weight: bold; }
.bold { font-weight: bold; }
</style>
