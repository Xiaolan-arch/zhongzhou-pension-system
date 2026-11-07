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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-tag type="warning">当前列表仅显示待审核的入住申请</el-tag>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="checkinList">
      <el-table-column label="入住单号" align="center" prop="applyNo" width="180" />
      <el-table-column label="老人姓名" align="center" prop="elderName" width="110" />
      <el-table-column label="身份证号" align="center" prop="idCard" width="190" />
      <el-table-column label="入住日期" align="center" prop="checkinDate" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.checkinDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleAudit(scope.row)"
            v-hasPermi="['business:checkin:audit']"
          >审核</el-button>
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

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditOpen" title="入住审核" width="760px" append-to-body :close-on-click-modal="false">
      <el-descriptions :column="2" border class="mb20">
        <el-descriptions-item label="老人姓名">{{ detail.elderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.gender === '1' ? '女' : '男' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detail.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ parseTime(detail.checkinDate, '{y}-{m}-{d}') || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家属姓名">{{ detail.familyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家属电话">{{ detail.familyPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分配床位">{{ bedLabel }}</el-descriptions-item>
        <el-descriptions-item label="建议护理等级">{{ dictLabel(biz_care_level, detail.suggestCareLevel) }}</el-descriptions-item>
        <el-descriptions-item label="自理能力">{{ detail.selfCare || '-' }}</el-descriptions-item>
        <el-descriptions-item label="精神状态">{{ detail.mentalState || '-' }}</el-descriptions-item>
        <el-descriptions-item label="疾病情况" :span="2">{{ detail.disease || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评估结论" :span="2">{{ detail.assessmentResult || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="审核结果" prop="auditResult">
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio label="0">通过</el-radio>
            <el-radio label="1">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditOpinion">
          <el-input
            v-model="auditForm.auditOpinion"
            type="textarea"
            :rows="3"
            :placeholder="auditForm.auditResult === '1' ? '驳回时审核意见必填' : '请输入审核意见'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="submitAudit">提交审核</el-button>
          <el-button :disabled="submitLoading" @click="auditOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessCheckinAudit">
import { listCheckin, getCheckin, auditCheckin } from "@/api/business/checkin";
import { listRoom } from "@/api/business/room";
import { getAvailableBeds } from "@/api/business/residence";

const { proxy } = getCurrentInstance();
const { biz_care_level } = proxy.useDict("biz_care_level");

const checkinList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const auditOpen = ref(false);
const submitLoading = ref(false);
const detail = ref({});
const roomOptions = ref([]);
const bedOptions = ref([]);

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  elderName: undefined,
  applyStatus: "2"
});

const auditForm = ref({
  checkinId: undefined,
  auditResult: "0",
  auditOpinion: undefined
});

// 审核校验规则：驳回时审核意见必填
const auditRules = {
  auditResult: [{ required: true, message: "请选择审核结果", trigger: "change" }],
  auditOpinion: [{ validator: validateAuditOpinion, trigger: "blur" }]
};

function validateAuditOpinion(rule, value, callback) {
  if (auditForm.value.auditResult === "1" && !value) {
    callback(new Error("驳回时审核意见必填"));
    return;
  }
  callback();
}

const bedLabel = computed(() => {
  if (!detail.value.roomId && !detail.value.bedId) {
    return "-";
  }
  const bed = bedOptions.value.find(item => item.bedId === detail.value.bedId);
  if (bed) {
    return bed.floorNo + "楼 " + bed.roomNo + " " + bed.bedNo + "床";
  }
  const room = roomOptions.value.find(item => item.roomId === detail.value.roomId);
  if (room) {
    return room.floorNo + "楼 " + room.roomNo;
  }
  return "-";
});

/** 查询待审核列表 */
function getList() {
  loading.value = true;
  listCheckin(queryParams.value).then(response => {
    checkinList.value = response.rows;
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
  queryParams.value.applyStatus = "2";
  handleQuery();
}

function dictLabel(options, value) {
  if (value === undefined || value === null || value === "") {
    return "-";
  }
  const list = Array.isArray(options) ? options : (options.value || []);
  const item = list.find(dict => dict.value === String(value));
  return item ? item.label : String(value);
}

/** 加载房间与床位展示数据 */
function loadMasterData() {
  listRoom({ pageNum: 1, pageSize: 100 }).then(res => {
    roomOptions.value = res.rows || [];
  }).catch(() => {});
  getAvailableBeds().then(res => {
    bedOptions.value = res.data || [];
  }).catch(() => {});
}

/** 打开审核弹窗 */
function handleAudit(row) {
  detail.value = {};
  auditForm.value = { checkinId: row.checkinId, auditResult: "0", auditOpinion: undefined };
  loadMasterData();
  getCheckin(row.checkinId).then(response => {
    detail.value = response.data || {};
    auditOpen.value = true;
  }).catch(() => {});
}

/** 提交审核 */
function submitAudit() {
  if (submitLoading.value) {
    return;
  }
  proxy.$refs["auditRef"].validate(valid => {
    if (!valid) {
      proxy.$modal.msgWarning("请先完善审核信息");
      return;
    }
    submitLoading.value = true;
    auditCheckin({
      checkinId: auditForm.value.checkinId,
      auditResult: auditForm.value.auditResult,
      auditOpinion: auditForm.value.auditOpinion
    }).then(() => {
      proxy.$modal.msgSuccess("审核成功");
      auditOpen.value = false;
      getList();
    }).catch(() => {}).finally(() => {
      submitLoading.value = false;
    });
  });
}

getList();
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}
</style>
