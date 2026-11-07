<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="老人姓名" prop="elderName">
        <el-input
          v-model="queryParams.elderName"
          placeholder="请输入老人姓名"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="报告类型" prop="reportType">
        <el-select v-model="queryParams.reportType" placeholder="请选择报告类型" clearable style="width: 180px">
          <el-option
            v-for="dict in biz_report_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="风险等级" prop="riskLevel">
        <el-select v-model="queryParams.riskLevel" placeholder="请选择风险等级" clearable style="width: 160px">
          <el-option
            v-for="dict in biz_report_risk"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 160px">
          <el-option
            v-for="dict in biz_report_status"
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
          icon="MagicStick"
          @click="handleGenerate"
          v-hasPermi="['business:report:generate']"
        >生成健康报告</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reportList">
      <el-table-column label="老人姓名" align="center" prop="elderName" width="110" />
      <el-table-column label="报告标题" align="center" prop="reportTitle" min-width="200" show-overflow-tooltip />
      <el-table-column label="报告类型" align="center" prop="reportType" width="120">
        <template #default="scope">
          <dict-tag :options="biz_report_type" :value="scope.row.reportType" />
        </template>
      </el-table-column>
      <el-table-column label="统计周期" align="center" width="200">
        <template #default="scope">
          <span>{{ periodText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="健康评分" align="center" prop="score" width="100">
        <template #default="scope">
          <span :class="scoreClass(scope.row.score)">{{ scope.row.score }}</span>
        </template>
      </el-table-column>
      <el-table-column label="风险等级" align="center" prop="riskLevel" width="100">
        <template #default="scope">
          <dict-tag :options="biz_report_risk" :value="scope.row.riskLevel" />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="biz_report_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="模型来源" align="center" prop="modelSource" width="110">
        <template #default="scope">
          <dict-tag :options="biz_report_model" :value="scope.row.modelSource" />
        </template>
      </el-table-column>
      <el-table-column label="生成时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="info" icon="Clock" @click="handleHistory(scope.row)">历史</el-button>
          <el-button
            v-if="scope.row.status === '1'"
            link
            type="success"
            icon="Download"
            @click="handleExportWord(scope.row)"
            v-hasPermi="['business:report:export']"
          >Word</el-button>
          <el-button
            v-if="scope.row.status === '1'"
            link
            type="warning"
            icon="Document"
            @click="handleExportPdf(scope.row)"
          >PDF</el-button>
          <el-button
            v-if="scope.row.status !== '0'"
            link
            type="primary"
            icon="Refresh"
            @click="handleRegenerate(scope.row)"
            v-hasPermi="['business:report:generate']"
          >重新生成</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['business:report:remove']"
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

    <!-- 生成健康报告 -->
    <el-dialog v-model="generateOpen" title="生成健康报告" width="560px" append-to-body :close-on-click-modal="false">
      <el-form ref="generateRef" :model="generateForm" :rules="generateRules" label-width="96px">
        <el-form-item label="在住老人" prop="elderId">
          <el-select
            v-model="generateForm.elderId"
            filterable
            placeholder="请选择在住老人"
            style="width: 100%"
            :loading="elderLoading"
          >
            <el-option
              v-for="elder in elderList"
              :key="elder.elderId"
              :label="elderNameText(elder)"
              :value="elder.elderId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报告类型" prop="reportType">
          <el-radio-group v-model="generateForm.reportType">
            <el-radio v-for="dict in biz_report_type" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="统计周期" prop="periodRange">
          <el-date-picker
            v-model="generateForm.periodRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="补充说明" prop="remark">
          <el-input
            v-model="generateForm.remark"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="可填写本期需重点关注的健康或照护事项，供 AI 参考"
          />
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="未配置千帆凭据时使用本地规则模版生成，配置后自动切换为大模型生成。"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="generateLoading" :disabled="generateLoading" @click="submitGenerate">生 成</el-button>
          <el-button :disabled="generateLoading" @click="generateOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 报告详情 -->
    <el-dialog v-model="detailOpen" title="健康报告详情" width="900px" append-to-body top="6vh">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="老人姓名">{{ detailReport.elderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报告类型">
          <dict-tag :options="biz_report_type" :value="detailReport.reportType" />
        </el-descriptions-item>
        <el-descriptions-item label="统计周期">{{ periodText(detailReport) }}</el-descriptions-item>
        <el-descriptions-item label="健康评分">
          <span :class="scoreClass(detailReport.score)">{{ detailReport.score }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <dict-tag :options="biz_report_risk" :value="detailReport.riskLevel" />
        </el-descriptions-item>
        <el-descriptions-item label="生成模型">{{ detailReport.modelName || detailReport.modelSource || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ parseTime(detailReport.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="归档文件" :span="2">
          <span v-if="detailReport.archivedUrl">{{ detailReport.archivedUrl }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-alert
        v-if="detailReport.status === '2'"
        class="mt10"
        type="error"
        :closable="false"
        show-icon
        :title="'报告生成失败：' + (detailReport.errorMsg || '未知原因')"
      />
      <el-alert
        v-else
        class="mt10"
        type="success"
        :closable="false"
        show-icon
        :title="detailReport.summary || '暂无摘要'"
      />

      <el-scrollbar max-height="52vh" class="report-scroll">
        <div class="report-body">
          <template v-for="(block, index) in reportBlocks" :key="index">
            <h3 v-if="block.type === 'h2'" class="report-h2">{{ block.text }}</h3>
            <h4 v-else-if="block.type === 'h3'" class="report-h3">{{ block.text }}</h4>
            <p v-else-if="block.type === 'li'" class="report-li">• {{ block.text }}</p>
            <p v-else class="report-p">{{ block.text }}</p>
          </template>
          <p v-if="reportBlocks.length === 0" class="report-p">暂无报告正文</p>
        </div>
      </el-scrollbar>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" icon="Document" @click="handleExportPdf(detailReport)">导出 PDF</el-button>
          <el-button icon="Download" @click="handleExportWord(detailReport)" v-hasPermi="['business:report:export']">导出 Word</el-button>
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 历史版本 -->
    <el-dialog v-model="historyOpen" title="历史报告版本" width="880px" append-to-body top="8vh">
      <el-table v-loading="historyLoading" :data="historyList" size="small">
        <el-table-column label="报告标题" prop="reportTitle" min-width="200" show-overflow-tooltip />
        <el-table-column label="报告类型" align="center" prop="reportType" width="110">
          <template #default="scope">
            <dict-tag :options="biz_report_type" :value="scope.row.reportType" />
          </template>
        </el-table-column>
        <el-table-column label="统计周期" align="center" width="190">
          <template #default="scope">
            <span>{{ periodText(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" align="center" prop="score" width="80">
          <template #default="scope">
            <span :class="scoreClass(scope.row.score)">{{ scope.row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="风险" align="center" prop="riskLevel" width="90">
          <template #default="scope">
            <dict-tag :options="biz_report_risk" :value="scope.row.riskLevel" />
          </template>
        </el-table-column>
        <el-table-column label="生成时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="90">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="historyOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- PDF 导出渲染区（离屏，不展示给用户） -->
    <div v-show="pdfVisible" ref="pdfRef" class="pdf-sheet">
      <div class="pdf-title">{{ pdfReport.reportTitle || 'AI 健康报告' }}</div>
      <div class="pdf-meta">
        <span>老人姓名：{{ pdfReport.elderName || '-' }}</span>
        <span>统计周期：{{ periodText(pdfReport) }}</span>
        <span>健康评分：{{ pdfReport.score }}（{{ riskLabel(pdfReport.riskLevel) }}）</span>
        <span>生成时间：{{ parseTime(pdfReport.createTime) || '-' }}</span>
      </div>
      <div class="pdf-summary">摘要：{{ pdfReport.summary || '-' }}</div>
      <div class="pdf-body">
        <template v-for="(block, index) in pdfBlocks" :key="index">
          <h3 v-if="block.type === 'h2'" class="report-h2">{{ block.text }}</h3>
          <h4 v-else-if="block.type === 'h3'" class="report-h3">{{ block.text }}</h4>
          <p v-else-if="block.type === 'li'" class="report-li">• {{ block.text }}</p>
          <p v-else class="report-p">{{ block.text }}</p>
        </template>
      </div>
      <div class="pdf-footnote">本报告由 AI 生成，仅供机构照护与家属沟通参考，不作为医疗诊断依据。</div>
    </div>
  </div>
</template>

<script setup name="BusinessReport">
import { saveAs } from "file-saver";
import html2canvas from "html2canvas";
import { jsPDF } from "jspdf";
import { listReport, getReport, generateReport, regenerateReport, delReport, exportReport } from "@/api/business/report";
import { listElder } from "@/api/business/elder";
import { blobValidate } from "@/utils/ruoyi";

const { proxy } = getCurrentInstance();
const { biz_report_type, biz_report_risk, biz_report_status, biz_report_model, biz_care_level } = proxy.useDict(
  "biz_report_type", "biz_report_risk", "biz_report_status", "biz_report_model", "biz_care_level"
);

const reportList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  elderName: undefined,
  reportType: undefined,
  riskLevel: undefined,
  status: undefined
});

const generateOpen = ref(false);
const generateLoading = ref(false);
const elderLoading = ref(false);
const elderList = ref([]);
const generateForm = ref({
  elderId: undefined,
  reportType: "0",
  periodRange: defaultPeriod(),
  remark: ""
});
const generateRules = {
  elderId: [{ required: true, message: "请选择在住老人", trigger: "change" }],
  reportType: [{ required: true, message: "请选择报告类型", trigger: "change" }],
  periodRange: [{ required: true, message: "请选择统计周期", trigger: "change" }]
};

const detailOpen = ref(false);
const detailReport = ref({});

const historyOpen = ref(false);
const historyLoading = ref(false);
const historyList = ref([]);

const pdfVisible = ref(false);
const pdfRef = ref();
const pdfReport = ref({});
const pdfGenerating = ref(false);

const reportBlocks = computed(() => parseMarkdown(detailReport.value.content));
const pdfBlocks = computed(() => parseMarkdown(pdfReport.value.content));

function pad(num) {
  return String(num).padStart(2, "0");
}

function defaultPeriod() {
  const end = new Date();
  const start = new Date();
  start.setDate(start.getDate() - 30);
  return [formatDate(start), formatDate(end)];
}

function formatDate(date) {
  return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate());
}

function periodText(row) {
  if (!row) {
    return "-";
  }
  const start = proxy.parseTime(row.periodStart, "{y}-{m}-{d}");
  const end = proxy.parseTime(row.periodEnd, "{y}-{m}-{d}");
  if (!start && !end) {
    return "-";
  }
  return (start || "-") + " 至 " + (end || "-");
}

function scoreClass(score) {
  const value = Number(score);
  if (value >= 80) {
    return "score-good";
  }
  if (value >= 65) {
    return "score-attention";
  }
  return "score-warning";
}

function riskLabel(value) {
  const list = biz_report_risk.value || [];
  const item = list.find(dict => dict.value === String(value));
  return item ? item.label : "-";
}

function elderNameText(elder) {
  const parts = [elder.elderName];
  if (elder.careLevel) {
    const list = biz_care_level.value || [];
    const item = list.find(dict => dict.value === String(elder.careLevel));
    if (item) {
      parts.push(item.label);
    }
  }
  if (elder.checkinDate) {
    parts.push(proxy.parseTime(elder.checkinDate, "{y}-{m}-{d}") + " 入住");
  }
  return parts.join(" · ");
}

/** 轻量 Markdown 解析：仅识别标题、无序列表与普通段落，避免直接渲染 AI 原文 HTML */
function parseMarkdown(content) {
  if (!content) {
    return [];
  }
  const blocks = [];
  content.split(/\r?\n/).forEach(line => {
    let text = line.trim();
    if (!text) {
      return;
    }
    text = text.replace(/\*\*/g, "").replace(/`/g, "").replace(/\s+$/g, "");
    const heading = text.match(/^(#{1,4})\s+(.*)$/);
    if (heading) {
      blocks.push({ type: heading[1].length <= 2 ? "h2" : "h3", text: heading[2] });
      return;
    }
    const listItem = text.match(/^[-*]\s+(.*)$/);
    if (listItem) {
      blocks.push({ type: "li", text: listItem[1] });
      return;
    }
    blocks.push({ type: "p", text });
  });
  return blocks;
}

/** 查询报告列表 */
function getList() {
  loading.value = true;
  listReport(queryParams.value).then(response => {
    reportList.value = response.rows || [];
    total.value = response.total || 0;
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
  queryParams.value.reportType = undefined;
  queryParams.value.riskLevel = undefined;
  queryParams.value.status = undefined;
  handleQuery();
}

/** 打开生成弹窗：加载在住老人 */
function handleGenerate() {
  generateForm.value = {
    elderId: undefined,
    reportType: "0",
    periodRange: defaultPeriod(),
    remark: ""
  };
  generateOpen.value = true;
  loadElders();
}

function loadElders() {
  elderLoading.value = true;
  listElder({ pageNum: 1, pageSize: 100, status: "0" }).then(response => {
    elderList.value = response.rows || [];
    elderLoading.value = false;
  }).catch(() => {
    elderLoading.value = false;
  });
}

/** 提交生成 */
function submitGenerate() {
  if (generateLoading.value) {
    return;
  }
  proxy.$refs["generateRef"].validate(valid => {
    if (!valid) {
      return;
    }
    if (generateForm.value.periodRange && generateForm.value.periodRange.length === 2) {
      generateForm.value.periodStart = generateForm.value.periodRange[0];
      generateForm.value.periodEnd = generateForm.value.periodRange[1];
    }
    generateLoading.value = true;
    generateReport({
      elderId: generateForm.value.elderId,
      reportType: generateForm.value.reportType,
      periodStart: generateForm.value.periodStart,
      periodEnd: generateForm.value.periodEnd,
      remark: generateForm.value.remark
    }).then(response => {
      proxy.$modal.msgSuccess("报告生成完成");
      generateOpen.value = false;
      getList();
      const generated = response.data || {};
      if (generated.reportId) {
        showDetail(generated);
      }
    }).catch(() => {}).finally(() => {
      generateLoading.value = false;
    });
  });
}

/** 查看详情 */
function handleDetail(row) {
  if (!row || !row.reportId) {
    return;
  }
  if (row.content !== undefined) {
    showDetail(row);
    return;
  }
  getReport(row.reportId).then(response => {
    showDetail(response.data || {});
  }).catch(() => {});
}

function showDetail(report) {
  detailReport.value = report;
  detailOpen.value = true;
}

/** 历史版本 */
function handleHistory(row) {
  historyOpen.value = true;
  historyLoading.value = true;
  historyList.value = [];
  listReport({ pageNum: 1, pageSize: 20, elderId: row.elderId }).then(response => {
    historyList.value = response.rows || [];
    historyLoading.value = false;
  }).catch(() => {
    historyLoading.value = false;
  });
}

/** 重新生成 */
function handleRegenerate(row) {
  proxy.$modal.confirm('是否基于"' + (row.reportTitle || row.elderName) + '"的历史参数重新生成报告？').then(() => {
    return regenerateReport(row.reportId);
  }).then(response => {
    proxy.$modal.msgSuccess("报告重新生成完成");
    getList();
    const generated = response.data || {};
    if (generated.reportId) {
      showDetail(generated);
    }
  }).catch(() => {});
}

/** 导出 Word */
function handleExportWord(row) {
  if (!row || !row.reportId) {
    return;
  }
  exportReport(row.reportId).then(async data => {
    if (blobValidate(data)) {
      saveAs(new Blob([data]), "AI健康报告_" + (row.elderName || row.reportId) + ".docx");
    } else {
      const text = await data.text();
      const result = JSON.parse(text);
      proxy.$modal.msgError(result.msg || "导出失败");
    }
  }).catch(() => {});
}

/** 导出 PDF：整篇报告渲染后按 A4 分页 */
async function handleExportPdf(row) {
  if (!row || !row.reportId || pdfGenerating.value) {
    return;
  }
  if (row.content === undefined) {
    const response = await getReport(row.reportId);
    row = response.data || {};
  }
  if (!row.content) {
    proxy.$modal.msgWarning("报告正文为空，无法导出 PDF");
    return;
  }
  pdfGenerating.value = true;
  pdfReport.value = row;
  pdfVisible.value = true;
  try {
    await nextTick();
    const canvas = await html2canvas(pdfRef.value, { scale: 2, useCORS: true, backgroundColor: "#ffffff" });
    const pdf = new jsPDF("p", "mm", "a4");
    const pageWidth = 210;
    const pageHeight = 297;
    const margin = 10;
    const imgWidth = pageWidth - margin * 2;
    const pageCanvasHeight = Math.floor((pageHeight - margin * 2) * canvas.width / imgWidth);
    let offset = 0;
    let pageIndex = 0;
    while (offset < canvas.height) {
      const sliceHeight = Math.min(pageCanvasHeight, canvas.height - offset);
      const pageCanvas = document.createElement("canvas");
      pageCanvas.width = canvas.width;
      pageCanvas.height = sliceHeight;
      const ctx = pageCanvas.getContext("2d");
      ctx.fillStyle = "#ffffff";
      ctx.fillRect(0, 0, pageCanvas.width, pageCanvas.height);
      ctx.drawImage(canvas, 0, offset, canvas.width, sliceHeight, 0, 0, canvas.width, sliceHeight);
      if (pageIndex > 0) {
        pdf.addPage();
      }
      pdf.addImage(
        pageCanvas.toDataURL("image/jpeg", 0.92),
        "JPEG",
        margin,
        margin,
        imgWidth,
        sliceHeight * imgWidth / canvas.width
      );
      offset += sliceHeight;
      pageIndex++;
    }
    pdf.save("AI健康报告_" + (row.elderName || row.reportId) + ".pdf");
  } catch (e) {
    proxy.$modal.msgError("PDF 导出失败，请稍后重试");
  } finally {
    pdfVisible.value = false;
    pdfGenerating.value = false;
  }
}

/** 删除 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除报告"' + (row.reportTitle || row.elderName) + '"？').then(() => {
    return delReport(row.reportId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style scoped>
.mt10 {
  margin-top: 10px;
}
.score-good {
  color: #67c23a;
  font-weight: 600;
}
.score-attention {
  color: #e6a23c;
  font-weight: 600;
}
.score-warning {
  color: #f56c6c;
  font-weight: 600;
}
.report-scroll {
  margin-top: 14px;
}
.report-body {
  padding: 4px 8px 12px;
}
.report-h2 {
  margin: 18px 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}
.report-h3 {
  margin: 12px 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}
.report-p {
  margin: 6px 0;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  text-indent: 2em;
}
.report-li {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  padding-left: 12px;
}
.pdf-sheet {
  position: fixed;
  top: 0;
  left: -2000px;
  width: 760px;
  padding: 32px 40px;
  background: #ffffff;
  color: #303133;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  box-sizing: border-box;
  z-index: -1;
}
.pdf-title {
  font-size: 22px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 16px;
}
.pdf-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  font-size: 12px;
  color: #606266;
  padding-bottom: 10px;
  border-bottom: 1px solid #dcdfe6;
}
.pdf-summary {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
  background: #f5f7fa;
  padding: 10px 12px;
  border-radius: 4px;
}
.pdf-body {
  margin-top: 8px;
}
.pdf-footnote {
  margin-top: 18px;
  padding-top: 10px;
  border-top: 1px dashed #dcdfe6;
  font-size: 11px;
  color: #909399;
  text-align: center;
}
</style>
