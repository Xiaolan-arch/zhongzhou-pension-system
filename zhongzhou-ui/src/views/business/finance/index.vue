<template>
  <div class="app-container">
    <!-- 逾期提醒条 -->
    <el-alert
      v-if="overdueList.length > 0"
      class="mb10"
      type="error"
      show-icon
      :closable="false"
      :title="`有 ${overdueList.length} 笔逾期账单（超过宽限期未付清，按日费率计收滞纳金），点击查看详情`"
      @click="openOverdueDialog"
      style="cursor: pointer"
    />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input v-model="queryParams.elderName" placeholder="请输入老人姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="账单月份" prop="billMonth">
        <el-date-picker v-model="queryParams.billMonth" type="month" value-format="YYYY-MM" placeholder="请选择账单月份" style="width: 200px" />
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="请选择支付状态" clearable style="width: 200px">
          <el-option v-for="dict in biz_bill_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleGenerate" v-hasPermi="['business:bill:generate']">生成月度账单</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['business:bill:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain icon="Warning" @click="openOverdueDialog" v-if="overdueList.length > 0">逾期账单（{{ overdueList.length }}）</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain icon="Refresh" :loading="penaltyLoading" :disabled="penaltyLoading" @click="handleRefreshPenalty" v-hasPermi="['business:bill:penalty']">重算滞纳金</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="billList">
      <el-table-column label="账单编号" align="center" prop="billNo" width="190" />
      <el-table-column label="老人姓名" align="center" prop="elderName" width="110" />
      <el-table-column label="账单月份" align="center" prop="billMonth" width="100" />
      <el-table-column label="账单金额" align="center" prop="amount" width="100">
        <template #default="scope"><span>{{ money(scope.row.amount) }}</span></template>
      </el-table-column>
      <el-table-column label="已收金额" align="center" prop="paidAmount" width="100">
        <template #default="scope"><span>{{ money(scope.row.paidAmount) }}</span></template>
      </el-table-column>
      <el-table-column label="待收金额" align="center" width="100">
        <template #default="scope">
          <span :class="{ 'text-danger': getPendingAmount(scope.row) > 0 }">{{ money(getPendingAmount(scope.row)) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="滞纳金" align="center" prop="penaltyAmount" width="100">
        <template #default="scope">
          <span :class="{ 'text-danger': Number(scope.row.penaltyAmount) > 0 }">
            {{ money(scope.row.penaltyAmount) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" prop="payStatus" width="100">
        <template #default="scope">
          <div style="display: flex; flex-direction: column; align-items: center; gap: 2px;">
            <dict-tag :options="biz_bill_status" :value="scope.row.payStatus" />
            <el-tag v-if="isOverdue(scope.row)" size="small" type="danger" effect="plain">逾期</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="账单周期" align="center" width="200">
        <template #default="scope">
          <span>{{ formatDate(scope.row.periodStart) }} ~ {{ formatDate(scope.row.periodEnd) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="420" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button
            v-if="scope.row.payStatus !== '1' && scope.row.payStatus !== '2'"
            link type="success" icon="Money" @click="handlePay(scope.row)"
            v-hasPermi="['business:bill:pay']"
          >缴费</el-button>
          <el-button
            v-if="scope.row.paidAmount > 0"
            link type="warning" icon="RefreshLeft" @click="handleRefund(scope.row)"
            v-hasPermi="['business:bill:pay']"
          >退款</el-button>
          <el-button
            v-if="scope.row.payStatus !== '1' && scope.row.payStatus !== '2'"
            link type="primary" icon="Bell" @click="handleNotify(scope.row)"
            v-hasPermi="['business:bill:notify']"
          >提醒</el-button>
          <el-button link type="info" icon="Tickets" @click="handleInvoice(scope.row)" v-hasPermi="['business:bill:invoice']">发票</el-button>
          <el-button link type="primary" icon="Delete" :disabled="deleteLoading" @click="handleDelete(scope.row)" v-hasPermi="['business:bill:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 账单详情 -->
    <el-dialog v-model="detailOpen" title="账单详情" width="900px" append-to-body>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="账单编号">{{ detailBill.billNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="老人姓名">{{ detailBill.elderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账单月份">{{ detailBill.billMonth || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账单周期" :span="2">{{ periodText }}</el-descriptions-item>
        <el-descriptions-item label="账单金额">{{ money(detailBill.amount) }}</el-descriptions-item>
        <el-descriptions-item label="押金">{{ money(detailBill.deposit) }}</el-descriptions-item>
        <el-descriptions-item label="预交款">{{ money(detailBill.prepaid) }}</el-descriptions-item>
        <el-descriptions-item label="已付金额">{{ money(detailBill.paidAmount) }}</el-descriptions-item>
        <el-descriptions-item label="应付金额">{{ money(payable) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态"><dict-tag :options="biz_bill_status" :value="detailBill.payStatus" /></el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ dictLabel(biz_pay_method, detailBill.payMethod) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatDateTime(detailBill.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="逾期天数">
          <el-tag v-if="Number(detailBill.overdueDays) > 0" type="danger" size="small">{{ detailBill.overdueDays }} 天</el-tag>
          <span v-else>未逾期</span>
        </el-descriptions-item>
        <el-descriptions-item label="滞纳金">
          <span :class="{ 'text-danger': Number(detailBill.penaltyAmount) > 0 }">{{ money(detailBill.penaltyAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="待收合计">
          <span class="text-danger bold">{{ money(getPendingAmount(detailBill) + (Number(detailBill.penaltyAmount) || 0)) }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <h4 class="detail-title">加项明细</h4>
      <el-table :data="addItems" size="small" border>
        <el-table-column label="费用项目" align="center" prop="feeName" />
        <el-table-column label="服务内容" align="center" prop="serviceContent" show-overflow-tooltip />
        <el-table-column label="金额" align="center" prop="amount" width="120">
          <template #default="scope"><span>{{ money(scope.row.amount) }}</span></template>
        </el-table-column>
      </el-table>

      <h4 class="detail-title">减项明细</h4>
      <el-table :data="subItems" size="small" border>
        <el-table-column label="费用项目" align="center" prop="feeName" />
        <el-table-column label="服务内容" align="center" prop="serviceContent" show-overflow-tooltip />
        <el-table-column label="金额" align="center" prop="amount" width="120">
          <template #default="scope"><span>{{ money(scope.row.amount) }}</span></template>
        </el-table-column>
      </el-table>

      <div class="detail-summary">
        <span>加项小计：<b>{{ money(addTotal) }}</b></span>
        <span>减项小计：<b>{{ money(subTotal) }}</b></span>
        <span>应付金额：<b class="payable">{{ money(payable) }}</b></span>
      </div>
      <el-alert class="mt10" type="info" :closable="false" show-icon title='应付金额按"账单金额 - 预交款 - 减项小计"估算，仅供参考。' />

      <!-- 收款历史 -->
      <h4 class="detail-title">收款历史（{{ recordList.length }} 笔）</h4>
      <el-table :data="recordList" size="small" border v-loading="recordLoading">
        <el-table-column label="收款单号" align="center" prop="recordNo" width="220" />
        <el-table-column label="收款金额" align="center" width="110">
          <template #default="scope"><span class="text-success">{{ money(scope.row.payAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="支付方式" align="center" width="100">
          <template #default="scope">{{ dictLabel(biz_pay_method, scope.row.payMethod) }}</template>
        </el-table-column>
        <el-table-column label="操作人" align="center" prop="operatorName" width="100" />
        <el-table-column label="收款时间" align="center" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      </el-table>

      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 生成月度账单（确认预览） -->
    <el-dialog v-model="generateOpen" title="生成月度账单" width="460px" append-to-body :close-on-click-modal="false">
      <el-alert class="mb20" type="info" :closable="false" show-icon title="将为该月份所有在住老人生成月度账单，已存在的账单会自动跳过" />
      <el-form label-width="100px">
        <el-form-item label="账单月份">
          <el-date-picker v-model="generateMonth" type="month" value-format="YYYY-MM" placeholder="请选择账单月份" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="generateLoading" :disabled="generateLoading" @click="submitGenerate">生 成</el-button>
          <el-button :disabled="generateLoading" @click="generateOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 缴费弹窗（支持部分收款） -->
    <el-dialog v-model="payOpen" title="账单缴费" width="640px" append-to-body :close-on-click-modal="false">
      <el-descriptions :column="2" border class="mb20">
        <el-descriptions-item label="账单编号">{{ payBillInfo.billNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="老人姓名">{{ payBillInfo.elderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账单金额">{{ money(payBillInfo.amount) }}</el-descriptions-item>
        <el-descriptions-item label="已收金额">{{ money(payBillInfo.paidAmount) }}</el-descriptions-item>
        <el-descriptions-item label="应付剩余">
          <span class="text-danger bold">{{ money(payPending) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态"><dict-tag :options="biz_bill_status" :value="payBillInfo.payStatus" /></el-descriptions-item>
      </el-descriptions>

      <el-form label-width="100px">
        <el-form-item label="本次收款金额" required>
          <el-input-number
            v-model="payForm.payAmount"
            :min="0.01"
            :max="Math.max(payPending, 0.01)"
            :step="100"
            :precision="2"
            controls-position="right"
            style="width: 100%"
            placeholder="默认 = 应付剩余"
          />
          <div class="form-tip">收款金额不能超过应付剩余，小于应付剩余则账单变为"部分已收"</div>
        </el-form-item>
        <el-form-item label="支付方式" required>
          <el-radio-group v-model="payForm.payMethod">
            <el-radio v-for="dict in biz_pay_method" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="payForm.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="可选，如：微信转账 + 截图已上传" />
        </el-form-item>
        <el-form-item label="支付凭证">
          <image-upload v-model="payForm.payVoucher" :limit="1" :file-size="5" />
        </el-form-item>
      </el-form>

      <!-- 历史收款记录（该账单的） -->
      <div class="record-history">
        <div class="record-history-title">该账单收款历史</div>
        <el-table :data="payRecordList" size="small" border v-loading="payRecordLoading" max-height="200">
          <el-table-column label="收款单号" align="center" prop="recordNo" width="180" />
          <el-table-column label="金额" align="center" width="100">
            <template #default="scope"><span class="text-success">{{ money(scope.row.payAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="方式" align="center" width="80">
            <template #default="scope">{{ dictLabel(biz_pay_method, scope.row.payMethod) }}</template>
          </el-table-column>
          <el-table-column label="时间" align="center" width="150">
            <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="payLoading" :disabled="payLoading" @click="submitPay">确认收款</el-button>
          <el-button :disabled="payLoading" @click="payOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退款弹窗 -->
    <el-dialog v-model="refundOpen" title="账单退款" width="560px" append-to-body :close-on-click-modal="false">
      <el-descriptions :column="2" border class="mb20">
        <el-descriptions-item label="账单编号">{{ refundBillInfo.billNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="老人姓名">{{ refundBillInfo.elderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账单金额">{{ money(refundBillInfo.amount) }}</el-descriptions-item>
        <el-descriptions-item label="已收金额">{{ money(refundBillInfo.paidAmount) }}</el-descriptions-item>
      </el-descriptions>

      <el-form label-width="100px">
        <el-form-item label="退款金额" required>
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0.01"
            :max="refundMax"
            :precision="2"
            controls-position="right"
            style="width: 100%"
            placeholder="默认 = 已收金额"
          />
          <div class="form-tip">退款金额需大于 0 且不超过已收金额 {{ money(refundBillInfo.paidAmount) }}</div>
        </el-form-item>
        <el-form-item label="退款原因" required>
          <el-input v-model="refundForm.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="refundLoading" :disabled="refundLoading" @click="submitRefund">确认退款</el-button>
          <el-button :disabled="refundLoading" @click="refundOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 逾期账单弹窗 -->
    <el-dialog v-model="overdueOpen" title="逾期账单列表（超过宽限期未付清）" width="980px" append-to-body>
      <el-table :data="overdueList" v-loading="overdueLoading">
        <el-table-column label="账单编号" prop="billNo" width="190" />
        <el-table-column label="老人姓名" prop="elderName" width="110" />
        <el-table-column label="账单月份" prop="billMonth" width="100" />
        <el-table-column label="账单金额" width="100">
          <template #default="scope">{{ money(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column label="已收" width="100">
          <template #default="scope">{{ money(scope.row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="待收" width="100">
          <template #default="scope"><span class="text-danger">{{ money(getPendingAmount(scope.row)) }}</span></template>
        </el-table-column>
        <el-table-column label="周期结束" width="110">
          <template #default="scope">{{ formatDate(scope.row.periodEnd) }}</template>
        </el-table-column>
        <el-table-column label="逾期天数" width="90">
          <template #default="scope"><el-tag type="danger">{{ scope.row.overdueDays || 0 }} 天</el-tag></template>
        </el-table-column>
        <el-table-column label="滞纳金" width="100">
          <template #default="scope"><span class="text-danger">{{ money(scope.row.penaltyAmount) }}</span></template>
        </el-table-column>
        <el-table-column label="待收合计" width="110">
          <template #default="scope"><span class="text-danger bold">{{ money(getPendingAmount(scope.row) + (Number(scope.row.penaltyAmount) || 0)) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button link type="success" @click="handlePay(scope.row)">缴费</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="overdueOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 发票打印弹窗 -->
    <el-dialog v-model="invoiceOpen" title="账单发票 / 收据" width="760px" append-to-body>
      <div id="print-area" class="invoice-print">
        <div class="invoice-header">
          <h2>中州养老院 · 费用收据</h2>
          <div class="invoice-meta">
            <span>收据编号：{{ invoiceData.bill?.billNo || '-' }}</span>
            <span>打印日期：{{ formatDateTime(new Date()) }}</span>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人姓名">{{ invoiceData.bill?.elderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="账单月份">{{ invoiceData.bill?.billMonth || '-' }}</el-descriptions-item>
          <el-descriptions-item label="账单周期" :span="2">
            {{ formatDate(invoiceData.bill?.periodStart) }} 至 {{ formatDate(invoiceData.bill?.periodEnd) }}
          </el-descriptions-item>
        </el-descriptions>

        <h4>费用明细</h4>
        <el-table :data="invoiceData.items || []" border size="small">
          <el-table-column label="项目" prop="feeName" />
          <el-table-column label="内容" prop="serviceContent" show-overflow-tooltip />
          <el-table-column label="类型" width="80">
            <template #default="scope">{{ scope.row.itemType === '1' ? '加项' : '减项' }}</template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="scope">{{ money(scope.row.amount) }}</template>
          </el-table-column>
        </el-table>

        <div class="invoice-summary">
          <div>账单金额：<b>{{ money(invoiceData.bill?.amount) }}</b></div>
          <div>预交款：<b>{{ money(invoiceData.bill?.prepaid) }}</b></div>
          <div>已收金额：<b class="text-success">{{ money(invoiceData.bill?.paidAmount) }}</b></div>
          <div>支付状态：<dict-tag :options="biz_bill_status" :value="invoiceData.bill?.payStatus" /></div>
        </div>

        <h4>收款记录</h4>
        <el-table :data="invoiceData.records || []" border size="small">
          <el-table-column label="收款单号" prop="recordNo" width="220" />
          <el-table-column label="金额" width="120" align="right">
            <template #default="scope">{{ money(scope.row.payAmount) }}</template>
          </el-table-column>
          <el-table-column label="方式" width="100">
            <template #default="scope">{{ dictLabel(biz_pay_method, scope.row.payMethod) }}</template>
          </el-table-column>
          <el-table-column label="时间" width="180">
            <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作人" prop="operatorName" width="100" />
        </el-table>

        <div class="invoice-sign">
          <div>财务签字：_______________</div>
          <div>老人/家属签字：_______________</div>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" icon="Download" @click="doDownloadPdf" v-hasPermi="['business:bill:invoice']">下载 PDF</el-button>
        <el-button type="primary" icon="Printer" @click="doPrint">打 印</el-button>
        <el-button @click="invoiceOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessFinance">
import { listBill, getBill, generateBill, payBill, refundBill, delBill, listPaymentRecords, getOverdueBills, getInvoiceData, downloadInvoicePdf, refreshPenalty, notifyBill } from "@/api/business/bill";

const { proxy } = getCurrentInstance();
const { biz_bill_status, biz_pay_method } = proxy.useDict("biz_bill_status", "biz_pay_method");

const billList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const deleteLoading = ref(false);
const exportLoading = ref(false);
const overdueList = ref([]);
const overdueLoading = ref(false);
const penaltyLoading = ref(false);

const queryParams = ref({ pageNum: 1, pageSize: 10, elderName: undefined, billMonth: undefined, payStatus: undefined });

// 详情
const detailOpen = ref(false);
const detailBill = ref({});
const detailItems = ref([]);
const recordList = ref([]);
const recordLoading = ref(false);

// 生成
const generateOpen = ref(false);
const generateMonth = ref(lastMonth());
const generateLoading = ref(false);

// 缴费
const payOpen = ref(false);
const payLoading = ref(false);
const payBillInfo = ref({});
const payForm = ref({ billId: undefined, payAmount: undefined, payMethod: undefined, payVoucher: undefined, remark: '' });
const payRecordList = ref([]);
const payRecordLoading = ref(false);

// 退款
const refundOpen = ref(false);
const refundLoading = ref(false);
const refundBillInfo = ref({});
const refundForm = ref({ billId: undefined, refundAmount: undefined, remark: '' });

// 逾期
const overdueOpen = ref(false);

// 发票
const invoiceOpen = ref(false);
const invoiceData = ref({ bill: null, items: [], records: [] });

const addItems = computed(() => detailItems.value.filter(item => item.itemType === "1"));
const subItems = computed(() => detailItems.value.filter(item => item.itemType === "2"));
const addTotal = computed(() => addItems.value.reduce((sum, item) => sum + (Number(item.amount) || 0), 0));
const subTotal = computed(() => subItems.value.reduce((sum, item) => sum + (Number(item.amount) || 0), 0));
const payable = computed(() => {
  const amount = Number(detailBill.value.amount) || 0;
  const prepaid = Number(detailBill.value.prepaid) || 0;
  return amount - prepaid - subTotal.value;
});
const payPending = computed(() => getPendingAmount(payBillInfo.value));
// 退款上限 = 当前已收金额
const refundMax = computed(() => Math.max(Number(refundBillInfo.value.paidAmount) || 0, 0.01));
const periodText = computed(() => {
  if (!detailBill.value.periodStart && !detailBill.value.periodEnd) return "-";
  return (formatDate(detailBill.value.periodStart) || "-") + " 至 " + (formatDate(detailBill.value.periodEnd) || "-");
});

// 生成账单默认上一个月份
function lastMonth() {
  const now = new Date();
  const date = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  const month = String(date.getMonth() + 1).padStart(2, "0");
  return date.getFullYear() + "-" + month;
}
function money(value) {
  const num = Number(value);
  return isNaN(num) ? "0.00" : num.toFixed(2);
}
function formatDate(val) {
  if (!val) return "-";
  return proxy.parseTime(val, "{y}-{m}-{d}");
}
function formatDateTime(val) {
  if (!val) return "-";
  return proxy.parseTime(val, "{y}-{m}-{d} {h}:{i}");
}
function dictLabel(options, value) {
  if (value === undefined || value === null || value === "") return "-";
  const list = Array.isArray(options) ? options : (options.value || []);
  const item = list.find(dict => dict.value === String(value));
  return item ? item.label : String(value);
}

/** 计算账单待收金额 = 账单金额 - 预交款 - 已付 - 减项小计 */
function getPendingAmount(row) {
  const amount = Number(row.amount) || 0;
  const prepaid = Number(row.prepaid) || 0;
  const paid = Number(row.paidAmount) || 0;
  return Math.max(0, amount - prepaid - paid);
}
/** 是否逾期：由后端按配置的宽限期计算（overdueDays > 0 即逾期） */
function isOverdue(row) {
  return Number(row.overdueDays) > 0;
}
/** 手动重算逾期账单滞纳金 */
function handleRefreshPenalty() {
  if (penaltyLoading.value) return;
  penaltyLoading.value = true;
  refreshPenalty().then(response => {
    proxy.$modal.msgSuccess("滞纳金重算完成，共更新 " + (response.data || 0) + " 条账单");
    getList();
    loadOverdue();
  }).catch(() => {}).finally(() => { penaltyLoading.value = false; });
}

/** 向家属发送微信账单提醒（Mock 模式仅记录发送日志） */
function handleNotify(row) {
  proxy.$modal.confirm('确认向「' + (row.elderName || '该老人') + '」的家属发送账单提醒？').then(() => {
    return notifyBill(row.billId, row.overdueDays > 0 ? '2' : '1');
  }).then(response => {
    proxy.$modal.msgSuccess('已发送提醒，共生成 ' + (response.count || 0) + ' 条消息记录');
    getList();
  }).catch(() => {});
}

/** 列表 */
function getList() {
  loading.value = true;
  listBill(queryParams.value).then(response => {
    billList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  }).catch(() => { loading.value = false; });
}
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.elderName = undefined;
  queryParams.value.billMonth = undefined;
  queryParams.value.payStatus = undefined;
  handleQuery();
}

/** 导出 */
function handleExport() {
  if (exportLoading.value) return;
  exportLoading.value = true;
  proxy.download("business/bill/export", { ...queryParams.value }, `bill_${new Date().getTime()}.xlsx`)
    .catch(() => {}).finally(() => { exportLoading.value = false; });
}

/** 生成（先确认月份再调用原接口） */
function handleGenerate() { generateMonth.value = lastMonth(); generateOpen.value = true; }
function submitGenerate() {
  if (generateLoading.value) return;
  if (!generateMonth.value) { proxy.$modal.msgWarning("请选择账单月份"); return; }
  generateLoading.value = true;
  generateBill({ billMonth: generateMonth.value }).then(response => {
    const count = response.data || 0;
    proxy.$modal.msgSuccess("本次生成 " + count + " 条账单");
    generateOpen.value = false;
    getList();
  }).catch(() => {}).finally(() => { generateLoading.value = false; });
}

/** 详情 */
function handleDetail(row) {
  getBill(row.billId).then(response => {
    const data = response.data || {};
    detailBill.value = data.bill || {};
    detailItems.value = data.items || [];
    detailOpen.value = true;
    loadRecords(row.billId);
  }).catch(() => {});
}
function loadRecords(billId) {
  recordLoading.value = true;
  listPaymentRecords(billId).then(response => {
    recordList.value = response.data || [];
  }).catch(() => {}).finally(() => { recordLoading.value = false; });
}

/** 缴费 */
function handlePay(row) {
  payBillInfo.value = row;
  const pending = getPendingAmount(row);
  payForm.value = { billId: row.billId, payAmount: pending > 0 ? Number(pending.toFixed(2)) : undefined, payMethod: undefined, payVoucher: undefined, remark: '' };
  payOpen.value = true;
  loadPayRecords(row.billId);
}
function loadPayRecords(billId) {
  payRecordLoading.value = true;
  listPaymentRecords(billId).then(response => {
    payRecordList.value = response.data || [];
  }).catch(() => {}).finally(() => { payRecordLoading.value = false; });
}
function submitPay() {
  if (payLoading.value) return;
  if (!payForm.value.payMethod) { proxy.$modal.msgWarning("请选择支付方式"); return; }
  const amount = Number(payForm.value.payAmount);
  if (!amount || amount <= 0) { proxy.$modal.msgWarning("请输入有效的收款金额"); return; }
  payLoading.value = true;
  payBill({
    billId: payForm.value.billId,
    payAmount: payForm.value.payAmount,
    payMethod: payForm.value.payMethod,
    payVoucher: payForm.value.payVoucher,
    remark: payForm.value.remark
  }).then(() => {
    proxy.$modal.msgSuccess("收款成功");
    payOpen.value = false;
    getList();
    loadOverdue();
  }).catch(() => {}).finally(() => { payLoading.value = false; });
}

/** 退款 */
function handleRefund(row) {
  refundBillInfo.value = row;
  const paid = Number(row.paidAmount) || 0;
  refundForm.value = { billId: row.billId, refundAmount: Number(paid.toFixed(2)), remark: '' };
  refundOpen.value = true;
}
function submitRefund() {
  if (refundLoading.value) return;
  const paid = Number(refundBillInfo.value.paidAmount) || 0;
  const amount = Number(refundForm.value.refundAmount);
  if (!amount || amount <= 0) { proxy.$modal.msgWarning("退款金额必须大于 0"); return; }
  if (amount > paid) { proxy.$modal.msgWarning("退款金额不能超过已收金额"); return; }
  if (!refundForm.value.remark || !refundForm.value.remark.trim()) { proxy.$modal.msgWarning("请输入退款原因"); return; }
  refundLoading.value = true;
  refundBill({
    billId: refundForm.value.billId,
    refundAmount: refundForm.value.refundAmount,
    remark: refundForm.value.remark.trim()
  }).then(() => {
    proxy.$modal.msgSuccess("退款成功");
    refundOpen.value = false;
    getList();
    loadOverdue();
  }).catch(() => {}).finally(() => { refundLoading.value = false; });
}

/** 删除 */
function handleDelete(row) {
  if (deleteLoading.value) return;
  proxy.$modal.confirm('是否确认删除账单"' + (row.billNo || row.billId) + '"？').then(() => {
    deleteLoading.value = true;
    return delBill(row.billId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {}).finally(() => { deleteLoading.value = false; });
}

/** 逾期账单 */
function loadOverdue() {
  overdueLoading.value = true;
  getOverdueBills().then(response => {
    overdueList.value = response.data || [];
  }).catch(() => {}).finally(() => { overdueLoading.value = false; });
}
function openOverdueDialog() { overdueOpen.value = true; }

/** 发票 */
function handleInvoice(row) {
  invoiceOpen.value = true;
  getInvoiceData(row.billId).then(response => {
    invoiceData.value = response.data || { bill: null, items: [], records: [] };
  }).catch(() => { invoiceOpen.value = false; });
}
function doPrint() {
  const el = document.getElementById('print-area');
  if (!el) return;
  const w = window.open('', '_blank');
  w.document.write(`<!DOCTYPE html><html><head><title>账单收据</title>
    <style>
      body { font-family: "Microsoft YaHei", sans-serif; padding: 20px; }
      h2 { text-align: center; margin-bottom: 4px; }
      table { width: 100%; border-collapse: collapse; margin: 10px 0; }
      th, td { border: 1px solid #333; padding: 6px 10px; font-size: 13px; }
      th { background: #f5f5f5; text-align: center; }
      .invoice-meta { text-align: center; color: #666; font-size: 12px; margin-bottom: 16px; }
      .invoice-summary { margin: 16px 0; padding: 12px; border: 1px dashed #999; }
      .invoice-summary div { padding: 3px 0; }
      .invoice-sign { margin-top: 30px; display: flex; justify-content: space-between; }
    </style></head><body>${el.innerHTML}</body></html>`);
  w.document.close();
  w.focus();
  setTimeout(() => { w.print(); }, 300);
}
/** 下载后端生成的 PDF */
function doDownloadPdf() {
  const billId = invoiceData.value?.bill?.billId;
  if (!billId) { proxy.$modal.msgWarning('发票数据未加载'); return; }
  downloadInvoicePdf(billId).catch(() => {});
}

getList();
loadOverdue();
</script>

<style scoped>
.mb10 { margin-bottom: 10px; }
.mt10 { margin-top: 10px; }
.mb20 { margin-bottom: 20px; }
.detail-title { margin: 18px 0 10px; font-size: 14px; font-weight: 600; color: #303133; }
.detail-summary { display: flex; justify-content: flex-end; gap: 24px; margin-top: 14px; font-size: 14px; }
.detail-summary .payable { color: #f56c6c; font-size: 16px; }
.text-danger { color: #f56c6c; font-weight: bold; }
.text-success { color: #2f9b6a; font-weight: bold; }
.bold { font-weight: bold; }
.form-tip { color: #909399; font-size: 12px; margin-top: 4px; }
.record-history { margin-top: 16px; }
.record-history-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; color: #606266; }

.invoice-print h4 { margin: 16px 0 8px; font-size: 14px; font-weight: 600; }
.invoice-header { text-align: center; margin-bottom: 12px; }
.invoice-header h2 { margin: 0; color: #2f9b6a; }
.invoice-meta { color: #909399; font-size: 12px; margin-top: 4px; display: flex; justify-content: center; gap: 24px; }
.invoice-summary { margin-top: 16px; padding: 12px; background: #f9f9f9; border-radius: 4px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px 16px; }
.invoice-sign { margin-top: 28px; display: flex; justify-content: space-between; }
</style>
