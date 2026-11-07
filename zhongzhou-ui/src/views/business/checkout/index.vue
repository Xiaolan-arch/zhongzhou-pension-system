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
      <el-form-item label="退住状态" prop="applyStatus">
        <el-select v-model="queryParams.applyStatus" placeholder="请选择退住状态" clearable style="width: 200px">
          <el-option
            v-for="dict in biz_checkout_status"
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
          @click="handleApply"
          v-hasPermi="['business:checkout:edit']"
        >发起退住申请</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="checkoutList">
      <el-table-column label="退住单号" align="center" prop="applyNo" width="180" />
      <el-table-column label="老人姓名" align="center" prop="elderName" width="110" />
      <el-table-column label="退住日期" align="center" prop="checkoutDate" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.checkoutDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退住原因" align="center" prop="reason" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="applyStatus" width="120">
        <template #default="scope">
          <dict-tag :options="biz_checkout_status" :value="scope.row.applyStatus" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleWizard(scope.row)"
            v-hasPermi="['business:checkout:edit']"
          >{{ isFinished(scope.row) ? '查看' : '办理' }}</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            :disabled="deleteLoading"
            @click="handleDelete(scope.row)"
            v-hasPermi="['business:checkout:remove']"
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

    <!-- 发起退住申请 -->
    <el-dialog v-model="applyOpen" title="发起退住申请" width="520px" append-to-body :close-on-click-modal="false">
      <el-form ref="applyRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="退住老人" prop="elderId">
          <el-select v-model="applyForm.elderId" placeholder="请选择在住老人" filterable style="width: 100%">
            <el-option
              v-for="elder in elderOptions"
              :key="elder.elderId"
              :label="elder.elderName + '（' + dictLabel(biz_care_level, elder.careLevel) + '）'"
              :value="elder.elderId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="退住日期" prop="checkoutDate">
          <el-date-picker
            v-model="applyForm.checkoutDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择退住日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退住原因" prop="reason">
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="请输入退住原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="applyLoading" :disabled="applyLoading" @click="submitApply">提 交</el-button>
          <el-button :disabled="applyLoading" @click="applyOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退住七步向导 -->
    <el-dialog
      v-model="wizardOpen"
      :title="wizardTitle"
      :fullscreen="true"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      @closed="handleWizardClosed"
    >
      <el-steps :active="stepIndex" align-center finish-status="success" class="mb20">
        <el-step v-for="(title, index) in stepTitles" :key="index" :title="title" />
      </el-steps>

      <el-alert
        v-if="readOnly"
        class="mb20"
        type="warning"
        :closable="false"
        show-icon
        :title="form.applyStatus === '9' ? '该申请已驳回，无法继续办理' : '该退住申请已完成，仅可查看'"
      />

      <el-form v-if="stepIndex < 6" ref="wizardRef" :model="form" :rules="wizardRules" label-width="130px" :disabled="readOnly">
        <!-- 第一步：申请退住 -->
        <template v-if="stepIndex === 0">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="退住老人">
                <el-input :model-value="form.elderName || '-'" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="退住日期" prop="checkoutDate">
                <el-date-picker
                  v-model="form.checkoutDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择退住日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="退住原因" prop="reason">
                <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请输入退住原因" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第二步：申请审批 -->
        <template v-if="stepIndex === 1">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="审批结果" prop="audit1Result">
                <el-radio-group v-model="form.audit1Result">
                  <el-radio label="0">通过</el-radio>
                  <el-radio label="1">驳回</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="审批意见" prop="audit1Opinion">
                <el-input v-model="form.audit1Opinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第三步：解除合同 -->
        <template v-if="stepIndex === 2">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="解除合同日期" prop="terminateDate">
                <el-date-picker
                  v-model="form.terminateDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择解除合同日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="解除合同文件" prop="terminateFile">
                <file-upload v-model="form.terminateFile" :limit="1" :file-size="20" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第四步：调整账单 -->
        <template v-if="stepIndex === 3">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="调整金额（元）" prop="adjustAmount">
                <el-input-number
                  v-model="form.adjustAmount"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="调整说明" prop="adjustRemark">
                <el-input v-model="form.adjustRemark" type="textarea" :rows="3" placeholder="请输入调整说明" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="调整金额为正数表示需补缴，为负数表示需退款。"
          />
        </template>

        <!-- 第五步：账单审批 -->
        <template v-if="stepIndex === 4">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="审批结果" prop="audit2Result">
                <el-radio-group v-model="form.audit2Result">
                  <el-radio label="0">通过</el-radio>
                  <el-radio label="1">驳回</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="审批意见" prop="audit2Opinion">
                <el-input v-model="form.audit2Opinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第六步：退住审批 -->
        <template v-if="stepIndex === 5">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="审批结果" prop="audit3Result">
                <el-radio-group v-model="form.audit3Result">
                  <el-radio label="0">通过</el-radio>
                  <el-radio label="1">驳回</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="审批意见" prop="audit3Opinion">
                <el-input v-model="form.audit3Opinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>

      <!-- 第七步：费用清算 -->
      <template v-if="stepIndex === 6">
        <el-descriptions :column="2" border class="mb20">
          <el-descriptions-item label="退住单号">{{ form.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ form.elderName }}</el-descriptions-item>
          <el-descriptions-item label="退住日期">{{ parseTime(form.checkoutDate, '{y}-{m}-{d}') || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退住原因">{{ form.reason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="解除合同日期">{{ parseTime(form.terminateDate, '{y}-{m}-{d}') || '-' }}</el-descriptions-item>
          <el-descriptions-item label="调整金额">{{ money(form.adjustAmount) }}</el-descriptions-item>
        </el-descriptions>
        <el-form ref="settleRef" :model="form" :rules="settleRules" label-width="130px" :disabled="readOnly">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="结算金额（元）" prop="settleAmount">
                <el-input-number
                  v-model="form.settleAmount"
                  :precision="2"
                  :min="0"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="退款方式" prop="refundMethod">
                <el-radio-group v-model="form.refundMethod">
                  <el-radio v-for="dict in biz_pay_method" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="确认费用清算无误后点击“完成退住”，系统将释放床位并更新老人状态。"
        />
      </template>

      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="stepIndex > 0" :disabled="submitLoading" @click="prevStep">上一步</el-button>
          <el-button
            v-if="stepIndex < 6"
            type="primary"
            :loading="submitLoading"
            :disabled="readOnly || submitLoading"
            @click="saveAndNext"
          >保存并下一步</el-button>
          <el-button
            v-if="stepIndex < 6"
            :loading="submitLoading"
            :disabled="readOnly || submitLoading"
            @click="saveAndClose"
          >保存并返回</el-button>
          <el-button
            v-if="stepIndex === 6 && !isCompleted"
            type="success"
            :loading="submitLoading"
            :disabled="readOnly || submitLoading"
            @click="handleComplete"
            v-hasPermi="['business:checkout:complete']"
          >完成退住</el-button>
          <el-button @click="wizardOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessCheckout">
import { listCheckout, getCheckout, addCheckout, stepCheckout, completeCheckout, delCheckout } from "@/api/business/checkout";
import { listElder, getElder } from "@/api/business/elder";

const { proxy } = getCurrentInstance();
const { biz_checkout_status, biz_care_level, biz_pay_method } = proxy.useDict(
  "biz_checkout_status",
  "biz_care_level",
  "biz_pay_method"
);

const stepTitles = ["申请退住", "申请审批", "解除合同", "调整账单", "账单审批", "退住审批", "费用清算"];

const checkoutList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const deleteLoading = ref(false);

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  elderName: undefined,
  applyStatus: undefined
});

const applyOpen = ref(false);
const applyLoading = ref(false);
const elderOptions = ref([]);
const elderCheckinDate = ref(undefined);
const applyForm = ref(createApplyForm());
const applyRules = {
  elderId: [{ required: true, message: "请选择退住老人", trigger: "change" }],
  checkoutDate: [
    { required: true, message: "请选择退住日期", trigger: "change" },
    { validator: validateApplyCheckoutDate, trigger: "change" }
  ],
  reason: [{ required: true, message: "请输入退住原因", trigger: "blur" }]
};

const wizardOpen = ref(false);
const wizardTitle = ref("退住办理");
const stepIndex = ref(0);
const checkoutId = ref(null);
const submitLoading = ref(false);
const readOnly = ref(false);
const isCompleted = ref(false);
const form = ref(createEmptyForm());

// 各步骤必填校验规则
const wizardRules = {
  checkoutDate: [
    { required: true, message: "请选择退住日期", trigger: "change" },
    { validator: validateCheckoutDate, trigger: "change" }
  ],
  reason: [{ required: true, message: "请输入退住原因", trigger: "blur" }],
  audit1Result: [{ required: true, message: "请选择审批结果", trigger: "change" }],
  audit1Opinion: [{ validator: createOpinionValidator("audit1Result"), trigger: "blur" }],
  terminateDate: [
    { required: true, message: "请选择解除合同日期", trigger: "change" },
    { validator: validateTerminateDate, trigger: "change" }
  ],
  adjustRemark: [{ validator: validateAdjustRemark, trigger: "blur" }],
  audit2Result: [{ required: true, message: "请选择审批结果", trigger: "change" }],
  audit2Opinion: [{ validator: createOpinionValidator("audit2Result"), trigger: "blur" }],
  audit3Result: [{ required: true, message: "请选择审批结果", trigger: "change" }],
  audit3Opinion: [{ validator: createOpinionValidator("audit3Result"), trigger: "blur" }]
};

// 费用清算校验规则
const settleRules = {
  settleAmount: [
    { required: true, message: "请填写结算金额", trigger: "change" },
    { validator: validateSettleAmount, trigger: "change" }
  ],
  refundMethod: [{ required: true, message: "请选择退款方式", trigger: "change" }]
};

function toDateText(value) {
  return value ? String(value).slice(0, 10) : "";
}

/** 发起申请时校验退住日期不早于入住日期 */
function validateApplyCheckoutDate(rule, value, callback) {
  if (!value || !applyForm.value.elderId) {
    callback();
    return;
  }
  const elder = elderOptions.value.find(item => item.elderId === applyForm.value.elderId);
  const checkinDate = elder ? toDateText(elder.checkinDate) : "";
  if (checkinDate && value < checkinDate) {
    callback(new Error("退住日期不能早于入住日期（" + checkinDate + "）"));
    return;
  }
  callback();
}

/** 向导中校验退住日期不早于入住日期 */
function validateCheckoutDate(rule, value, callback) {
  const checkinDate = toDateText(elderCheckinDate.value);
  if (value && checkinDate && value < checkinDate) {
    callback(new Error("退住日期不能早于入住日期（" + checkinDate + "）"));
    return;
  }
  callback();
}

/** 解除合同日期不能早于退住日期 */
function validateTerminateDate(rule, value, callback) {
  const checkoutDate = toDateText(form.value.checkoutDate);
  if (value && checkoutDate && value < checkoutDate) {
    callback(new Error("解除合同日期不能早于退住日期（" + checkoutDate + "）"));
    return;
  }
  callback();
}

/** 审批驳回时审批意见必填 */
function createOpinionValidator(resultField) {
  return (rule, value, callback) => {
    if (form.value[resultField] === "1" && !value) {
      callback(new Error("驳回时审批意见必填"));
      return;
    }
    callback();
  };
}

/** 调整金额非 0 时必须填写调整说明 */
function validateAdjustRemark(rule, value, callback) {
  const amount = Number(form.value.adjustAmount);
  if (!isNaN(amount) && amount !== 0 && !value) {
    callback(new Error("填写调整金额后需说明调整原因"));
    return;
  }
  callback();
}

/** 结算金额不能为负 */
function validateSettleAmount(rule, value, callback) {
  if (value !== undefined && value !== null && value !== "" && Number(value) < 0) {
    callback(new Error("结算金额不能为负"));
    return;
  }
  callback();
}

function createApplyForm() {
  return {
    elderId: undefined,
    checkoutDate: undefined,
    reason: undefined
  };
}

function createEmptyForm() {
  return {
    checkoutId: undefined,
    applyNo: undefined,
    elderId: undefined,
    elderName: undefined,
    checkoutDate: undefined,
    reason: undefined,
    applyStatus: undefined,
    audit1Result: undefined,
    audit1Opinion: undefined,
    terminateDate: undefined,
    terminateFile: undefined,
    adjustAmount: undefined,
    adjustRemark: undefined,
    audit2Result: undefined,
    audit2Opinion: undefined,
    audit3Result: undefined,
    audit3Opinion: undefined,
    settleAmount: undefined,
    refundMethod: undefined
  };
}

/** 查询退住列表 */
function getList() {
  loading.value = true;
  listCheckout(queryParams.value).then(response => {
    checkoutList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.elderName = undefined;
  queryParams.value.applyStatus = undefined;
  handleQuery();
}

function isFinished(row) {
  return row.applyStatus === "7" || row.applyStatus === "9";
}

function money(value) {
  const num = Number(value);
  return isNaN(num) ? "0.00" : num.toFixed(2);
}

function dictLabel(options, value) {
  if (value === undefined || value === null || value === "") {
    return "-";
  }
  const list = Array.isArray(options) ? options : (options.value || []);
  const item = list.find(dict => dict.value === String(value));
  return item ? item.label : String(value);
}

/** 发起退住申请 */
function handleApply() {
  applyForm.value = createApplyForm();
  applyOpen.value = true;
  listElder({ pageNum: 1, pageSize: 100, status: "0" }).then(res => {
    elderOptions.value = res.rows || [];
  }).catch(() => {});
}

/** 提交退住申请 */
function submitApply() {
  if (applyLoading.value) {
    return;
  }
  proxy.$refs["applyRef"].validate(valid => {
    if (!valid) {
      proxy.$modal.msgWarning("请先填写退住老人、退住日期和退住原因");
      return;
    }
    applyLoading.value = true;
    addCheckout({
      elderId: applyForm.value.elderId,
      checkoutDate: applyForm.value.checkoutDate,
      reason: applyForm.value.reason
    }).then(() => {
      proxy.$modal.msgSuccess("退住申请已提交");
      applyOpen.value = false;
      getList();
    }).catch(() => {}).finally(() => {
      applyLoading.value = false;
    });
  });
}

/** 重置向导 */
function resetWizard() {
  form.value = createEmptyForm();
  stepIndex.value = 0;
  checkoutId.value = null;
  submitLoading.value = false;
  readOnly.value = false;
  isCompleted.value = false;
  elderCheckinDate.value = undefined;
}

/** 查看/办理退住申请 */
function handleWizard(row) {
  resetWizard();
  wizardTitle.value = "退住办理 - " + (row.applyNo || "");
  wizardOpen.value = true;
  getCheckout(row.checkoutId).then(response => {
    const data = response.data || {};
    form.value = Object.assign(createEmptyForm(), data);
    // 查询老人入住日期，用于退住日期联动校验
    if (data.elderId) {
      getElder(data.elderId).then(elderRes => {
        elderCheckinDate.value = (elderRes.data || {}).checkinDate;
      }).catch(() => {});
    }
    const status = String(data.applyStatus || "0");
    checkoutId.value = data.checkoutId;
    readOnly.value = status === "9" || status === "7";
    isCompleted.value = status === "7";
    stepIndex.value = status === "9" ? 0 : Math.min(Number(status), 6);
  }).catch(() => {});
}

/** 触发当前步骤的表单校验 */
function validateCurrentForm() {
  return new Promise(resolve => {
    const formRef = proxy.$refs["wizardRef"];
    if (!formRef) {
      resolve(true);
      return;
    }
    try {
      formRef.validate(valid => resolve(valid));
    } catch (e) {
      resolve(false);
    }
  });
}

/** 下一步前置校验：必填字段与日期联动 */
async function ensureStepValid() {
  const valid = await validateCurrentForm();
  if (!valid) {
    proxy.$modal.msgWarning("请先完善当前步骤的必填信息");
    return false;
  }
  // 退住日期不能早于入住日期（异步获取的入住日期尚未返回时跳过，由提交接口兜底）
  if (stepIndex.value === 0) {
    const checkoutDate = toDateText(form.value.checkoutDate);
    const checkinDate = toDateText(elderCheckinDate.value);
    if (checkoutDate && checkinDate && checkoutDate < checkinDate) {
      proxy.$modal.msgWarning("退住日期不能早于入住日期（" + checkinDate + "）");
      return false;
    }
  }
  return true;
}

/** 保存当前步骤（无ID时先创建草稿） */
async function saveStep(step) {
  let id = checkoutId.value;
  if (!id) {
    await addCheckout(form.value);
    const res = await listCheckout({ pageNum: 1, pageSize: 1, elderName: form.value.elderName || undefined });
    const row = (res.rows || [])[0];
    if (!row) {
      throw new Error("退住申请创建失败");
    }
    id = row.checkoutId;
    checkoutId.value = id;
    form.value.applyNo = row.applyNo;
  }
  await stepCheckout(id, step, form.value);
}

function isRejected(step) {
  return (step === 2 && form.value.audit1Result === "1")
    || (step === 5 && form.value.audit2Result === "1")
    || (step === 6 && form.value.audit3Result === "1");
}

/** 保存并下一步 */
async function saveAndNext() {
  if (submitLoading.value || readOnly.value) {
    return;
  }
  if (!(await ensureStepValid())) {
    return;
  }
  submitLoading.value = true;
  try {
    const step = stepIndex.value + 1;
    await saveStep(step);
    if (isRejected(step)) {
      proxy.$modal.msgSuccess("申请已驳回");
      wizardOpen.value = false;
      getList();
      return;
    }
    form.value.applyStatus = String(step);
    stepIndex.value++;
  } catch (e) {
    // 请求层已提示错误
  } finally {
    submitLoading.value = false;
  }
}

/** 保存并返回 */
async function saveAndClose() {
  if (submitLoading.value || readOnly.value) {
    return;
  }
  if (stepIndex.value === 6) {
    proxy.$modal.msgWarning("请点击“完成退住”完成流程");
    return;
  }
  if (!(await ensureStepValid())) {
    return;
  }
  submitLoading.value = true;
  try {
    const step = stepIndex.value + 1;
    await saveStep(step);
    proxy.$modal.msgSuccess("保存成功");
    wizardOpen.value = false;
    getList();
  } catch (e) {
    // 请求层已提示错误
  } finally {
    submitLoading.value = false;
  }
}

/** 上一步（仅本地切换） */
function prevStep() {
  if (stepIndex.value > 0) {
    stepIndex.value--;
  }
}

/** 完成退住 */
function handleComplete() {
  if (!checkoutId.value || submitLoading.value) {
    return;
  }
  const settleRef = proxy.$refs["settleRef"];
  if (settleRef) {
    settleRef.validate(valid => {
      if (!valid) {
        proxy.$modal.msgWarning("请先完善费用清算信息");
        return;
      }
      if (Number(form.value.settleAmount) < 0) {
        proxy.$modal.msgWarning("费用结算金额不能为负");
        return;
      }
      submitComplete();
    });
    return;
  }
  submitComplete();
}

/** 提交完成退住 */
function submitComplete() {
  submitLoading.value = true;
  completeCheckout(checkoutId.value, {
    settleAmount: form.value.settleAmount,
    refundMethod: form.value.refundMethod
  }).then(() => {
    proxy.$modal.msgSuccess("退住办理完成");
    wizardOpen.value = false;
    getList();
  }).catch(() => {}).finally(() => {
    submitLoading.value = false;
  });
}

/** 向导关闭清理 */
function handleWizardClosed() {
  resetWizard();
  wizardOpen.value = false;
}

/** 删除退住申请 */
function handleDelete(row) {
  if (deleteLoading.value) {
    return;
  }
  proxy.$modal.confirm('是否确认删除退住申请"' + (row.applyNo || row.checkoutId) + '"？').then(() => {
    deleteLoading.value = true;
    return delCheckout(row.checkoutId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {}).finally(() => {
    deleteLoading.value = false;
  });
}

getList();
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}
</style>
