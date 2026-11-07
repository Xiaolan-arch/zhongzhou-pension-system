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
      <el-form-item label="入住状态" prop="applyStatus">
        <el-select v-model="queryParams.applyStatus" placeholder="请选择入住状态" clearable style="width: 200px">
          <el-option
            v-for="dict in biz_checkin_status"
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
          v-hasPermi="['business:checkin:edit']"
        >新增入住申请</el-button>
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
      <el-table-column label="状态" align="center" prop="applyStatus" width="130">
        <template #default="scope">
          <div style="display: flex; flex-direction: column; align-items: center; gap: 2px;">
            <dict-tag :options="biz_checkin_status" :value="scope.row.applyStatus" />
            <template v-if="scope.row.applyStatus === '9' && scope.row.auditOpinion">
              <el-tooltip :content="'驳回意见：' + scope.row.auditOpinion" placement="bottom" effect="dark">
                <el-tag size="small" type="danger" effect="plain" style="max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">已驳回</el-tag>
              </el-tooltip>
            </template>
            <el-tag v-if="scope.row.applyStatus === '2'" size="small" type="warning" effect="plain">待审批</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="审核信息" align="center" width="180">
        <template #default="scope">
          <template v-if="scope.row.applyStatus === '2'">
            <span class="text-warning">等待机构管理员审批</span>
          </template>
          <template v-else-if="scope.row.auditor">
            <span style="color: #606266;">{{ scope.row.auditor }}</span>
            <div style="color: #909399; font-size: 12px;">{{ parseTime(scope.row.auditTime, '{m}-{d} {h}:{i}') }}</div>
          </template>
          <template v-else>-</template>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleWizard(scope.row)"
            :disabled="scope.row.applyStatus === '6'"
            v-hasPermi="['business:checkin:edit']"
          >{{ scope.row.applyStatus === '6' ? '查看' : scope.row.applyStatus === '2' ? '继续办理' : '办理' }}</el-button>
          <el-button
            v-if="scope.row.applyStatus === '2'"
            link
            type="warning"
            icon="InfoFilled"
            @click="showAuditTip"
          >审批后继续</el-button>
          <el-button
            v-if="scope.row.applyStatus === '9'"
            link
            type="success"
            icon="Refresh"
            :loading="scope.row._resubmitting"
            @click="handleResubmit(scope.row)"
            v-hasPermi="['business:checkin:edit']"
          >重新提交</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            :disabled="deleteLoading"
            @click="handleDelete(scope.row)"
            v-hasPermi="['business:checkin:remove']"
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

    <!-- 入住七步向导 -->
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
        :title="form.applyStatus === '9' ? '该申请已驳回，无法继续办理' : '该入住申请已完成，仅可查看'"
      />

      <el-form v-if="stepIndex < 6" ref="wizardRef" :model="form" :rules="wizardRules" label-width="130px" :disabled="readOnly">
        <!-- 第一步：申请入住 -->
        <template v-if="stepIndex === 0">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="老人姓名" prop="elderName">
                <el-input v-model="form.elderName" placeholder="请输入老人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender">
                  <el-radio label="0">男</el-radio>
                  <el-radio label="1">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="form.idCard" placeholder="请输入身份证号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出生日期" prop="birthday">
                <el-date-picker
                  v-model="form.birthday"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择出生日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="form.age" :min="0" :max="150" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="户籍地址" prop="address">
                <el-input v-model="form.address" placeholder="请输入户籍地址" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="家属姓名" prop="familyName">
                <el-input v-model="form.familyName" placeholder="请输入家属姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="与老人关系" prop="familyRelation">
                <el-input v-model="form.familyRelation" placeholder="请输入关系" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="家属电话" prop="familyPhone">
                <el-input v-model="form.familyPhone" placeholder="请输入家属电话" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="一寸照片" prop="avatar">
                <image-upload v-model="form.avatar" :limit="1" :file-size="5" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="身份证人像面" prop="idCardFront">
                <image-upload v-model="form.idCardFront" :limit="1" :file-size="5" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="身份证国徽面" prop="idCardBack">
                <image-upload v-model="form.idCardBack" :limit="1" :file-size="5" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第二步：入住评估 -->
        <template v-if="stepIndex === 1">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="自理能力" prop="selfCare">
                <el-input v-model="form.selfCare" placeholder="请输入自理能力评估" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="精神状态" prop="mentalState">
                <el-input v-model="form.mentalState" placeholder="请输入精神状态评估" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="疾病情况" prop="disease">
                <el-input v-model="form.disease" type="textarea" :rows="3" placeholder="请输入疾病情况" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="评估结论" prop="assessmentResult">
                <el-input v-model="form.assessmentResult" type="textarea" :rows="3" placeholder="请输入评估结论" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="建议护理等级" prop="suggestCareLevel">
                <el-select v-model="form.suggestCareLevel" placeholder="请选择护理等级" style="width: 100%">
                  <el-option v-for="dict in biz_care_level" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="评估人" prop="assessor">
                <el-input v-model="form.assessor" placeholder="请输入评估人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="评估时间" prop="assessTime">
                <el-date-picker
                  v-model="form.assessTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择评估时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第三步：入住审核 -->
        <template v-if="stepIndex === 2">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="审核结果" prop="auditResult">
                <el-radio-group v-model="form.auditResult">
                  <el-radio label="0">通过</el-radio>
                  <el-radio label="1">驳回</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="审核人" prop="auditor">
                <el-input v-model="form.auditor" placeholder="请输入审核人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="审核时间" prop="auditTime">
                <el-date-picker
                  v-model="form.auditTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择审核时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="审核意见" prop="auditOpinion">
                <el-input v-model="form.auditOpinion" type="textarea" :rows="3" placeholder="请输入审核意见" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第四步：入住配置 -->
        <template v-if="stepIndex === 3">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="房间" prop="roomId">
                <el-select v-model="form.roomId" placeholder="请选择房间" style="width: 100%" @change="handleRoomChange">
                  <el-option
                    v-for="room in roomOptions"
                    :key="room.roomId"
                    :label="roomLabel(room)"
                    :value="room.roomId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="床位" prop="bedId">
                <el-select
                  v-model="form.bedId"
                  placeholder="请选择床位"
                  style="width: 100%"
                  :disabled="!form.roomId"
                  @change="handleBedChange"
                >
                  <el-option
                    v-for="bed in roomBeds"
                    :key="bed.bedId"
                    :label="bed.bedNo + '床'"
                    :value="bed.bedId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="护理等级" prop="careLevel">
                <el-select v-model="form.careLevel" placeholder="请选择护理等级" style="width: 100%">
                  <el-option v-for="dict in biz_care_level" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="护理员" prop="nurseIds">
                <el-select v-model="nurseIdsArray" multiple placeholder="请选择护理员" style="width: 100%" @change="handleNurseChange">
                  <el-option
                    v-for="nurse in nurseOptions"
                    :key="nurse.nurseId"
                    :label="nurse.nurseName"
                    :value="nurse.nurseId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="费用标准（元/月）" prop="feeStandard">
                <el-input-number v-model="form.feeStandard" :min="0" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第五步：签约办理 -->
        <template v-if="stepIndex === 4">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="合同编号" prop="contractNo">
                <el-input v-model="form.contractNo" placeholder="请输入合同编号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="签约日期" prop="signDate">
                <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择签约日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合同开始日期" prop="contractStart">
                <el-date-picker v-model="form.contractStart" type="date" value-format="YYYY-MM-DD" placeholder="请选择合同开始日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合同结束日期" prop="contractEnd">
                <el-date-picker v-model="form.contractEnd" type="date" value-format="YYYY-MM-DD" placeholder="请选择合同结束日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合同文件" prop="contractFile">
                <file-upload v-model="form.contractFile" :limit="1" :file-size="20" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 第六步：首期缴费 -->
        <template v-if="stepIndex === 5">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="费用标准（元）">
                <el-input :model-value="money(form.feeStandard)" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="押金（元）">
                <el-input model-value="1000.00" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="首期合计（元）">
                <el-input :model-value="money(firstTotal)" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="支付方式" prop="payMethod">
                <el-radio-group v-model="form.payMethod">
                  <el-radio v-for="dict in biz_pay_method" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="支付凭证" prop="payVoucher">
                <image-upload v-model="form.payVoucher" :limit="1" :file-size="5" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="保存并下一步将生成首期账单并标记已支付。"
          />
        </template>
      </el-form>

      <!-- 第七步：完成入住 -->
      <template v-if="stepIndex === 6">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人姓名">{{ form.elderName }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ form.idCard }}</el-descriptions-item>
          <el-descriptions-item label="床位">{{ bedLabel }}</el-descriptions-item>
          <el-descriptions-item label="护理等级">{{ dictLabel(biz_care_level, form.careLevel) }}</el-descriptions-item>
          <el-descriptions-item label="合同编号">{{ form.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="合同期限">{{ contractPeriod }}</el-descriptions-item>
          <el-descriptions-item label="费用标准">{{ money(form.feeStandard) }}</el-descriptions-item>
          <el-descriptions-item label="首期费用">{{ money(firstTotal) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ dictLabel(biz_pay_method, form.payMethod) }}</el-descriptions-item>
          <el-descriptions-item label="缴费状态">{{ form.payStatus === '1' ? '已支付' : '待支付' }}</el-descriptions-item>
        </el-descriptions>
        <el-alert
          class="mt20"
          type="info"
          :closable="false"
          show-icon
          title="核对信息无误后点击“完成入住”，系统将占用床位并同步老人档案。"
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
            v-hasPermi="['business:checkin:complete']"
          >完成入住</el-button>
          <el-button @click="wizardOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessCheckin">
import { listCheckin, getCheckin, addCheckin, stepCheckin, completeCheckin, delCheckin, resubmitCheckin } from "@/api/business/checkin";
import { listRoom } from "@/api/business/room";
import { getAvailableBeds, getRoomDetail } from "@/api/business/residence";
import { listNurse, getTaskOverview } from "@/api/business/service";

const { proxy } = getCurrentInstance();
const { biz_checkin_status, biz_care_level, biz_room_type, biz_pay_method } = proxy.useDict(
  "biz_checkin_status",
  "biz_care_level",
  "biz_room_type",
  "biz_pay_method"
);

const stepTitles = ["申请入住", "入住评估", "入住审核", "入住配置", "签约办理", "首期缴费", "完成入住"];

const checkinList = ref([]);
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

const wizardOpen = ref(false);
const wizardTitle = ref("入住办理");
const stepIndex = ref(0);
const checkinId = ref(null);
const submitLoading = ref(false);
const readOnly = ref(false);
const isCompleted = ref(false);

const roomOptions = ref([]);
const bedOptions = ref([]);
const roomBeds = ref([]);
const nurseOptions = ref([]);
const nurseIdsArray = ref([]);
const nurseBedCounts = ref({});
const currentBedNurseIds = ref([]);
const form = ref(createEmptyForm());

// 护理员负责床位数上限（前端提示阈值，仅用于提前拦截）
const NURSE_MAX_BEDS = 10;

// 各步骤必填校验规则
const wizardRules = {
  elderName: [{ required: true, message: "请输入老人姓名", trigger: "blur" }],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
  idCard: [
    { required: true, message: "请输入身份证号", trigger: "blur" },
    { validator: validateIdCard, trigger: "blur" }
  ],
  birthday: [{ required: true, message: "请选择出生日期", trigger: "change" }],
  age: [{ required: true, message: "请输入年龄", trigger: "change" }],
  phone: [
    { required: true, message: "请输入联系电话", trigger: "blur" },
    { validator: validatePhone, trigger: "blur" }
  ],
  selfCare: [{ required: true, message: "请输入自理能力评估", trigger: "blur" }],
  mentalState: [{ required: true, message: "请输入精神状态评估", trigger: "blur" }],
  assessmentResult: [{ required: true, message: "请输入评估结论", trigger: "blur" }],
  suggestCareLevel: [{ required: true, message: "请选择建议护理等级", trigger: "change" }],
  assessor: [{ required: true, message: "请输入评估人", trigger: "blur" }],
  assessTime: [{ required: true, message: "请选择评估时间", trigger: "change" }],
  auditResult: [{ required: true, message: "请选择审核结果", trigger: "change" }],
  auditor: [{ required: true, message: "请输入审核人", trigger: "blur" }],
  auditTime: [{ required: true, message: "请选择审核时间", trigger: "change" }],
  auditOpinion: [{ validator: validateAuditOpinion, trigger: "blur" }],
  roomId: [{ required: true, message: "请选择房间", trigger: "change" }],
  bedId: [{ required: true, message: "请选择床位", trigger: "change" }],
  careLevel: [{ required: true, message: "请选择护理等级", trigger: "change" }],
  nurseIds: [{ validator: validateNurseIds, trigger: "change" }],
  feeStandard: [
    { required: true, message: "请输入费用标准", trigger: "change" },
    { validator: validateFeeStandard, trigger: "change" }
  ],
  contractNo: [{ required: true, message: "请输入合同编号", trigger: "blur" }],
  signDate: [{ required: true, message: "请选择签约日期", trigger: "change" }],
  contractStart: [{ required: true, message: "请选择合同开始日期", trigger: "change" }],
  contractEnd: [
    { required: true, message: "请选择合同结束日期", trigger: "change" },
    { validator: validateContractEnd, trigger: "change" }
  ],
  payMethod: [{ required: true, message: "请选择支付方式", trigger: "change" }]
};

function validateIdCard(rule, value, callback) {
  if (!value || /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(value)) {
    callback();
    return;
  }
  callback(new Error("身份证号格式不正确"));
}

function validatePhone(rule, value, callback) {
  if (!value || /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/.test(value)) {
    callback();
    return;
  }
  callback(new Error("联系电话格式不正确"));
}

function validateAuditOpinion(rule, value, callback) {
  if (form.value.auditResult === "1" && !value) {
    callback(new Error("驳回时审核意见必填"));
    return;
  }
  callback();
}

function validateNurseIds(rule, value, callback) {
  if (!nurseIdsArray.value.length) {
    callback(new Error("请至少选择一名护理员"));
    return;
  }
  callback();
}

function validateFeeStandard(rule, value, callback) {
  if (value === undefined || value === null || value === "") {
    callback();
    return;
  }
  if (Number(value) <= 0) {
    callback(new Error("费用标准必须大于 0"));
    return;
  }
  callback();
}

function validateContractEnd(rule, value, callback) {
  if (!value || !form.value.contractStart) {
    callback();
    return;
  }
  if (value < form.value.contractStart) {
    callback(new Error("合同结束日期不能早于开始日期"));
    return;
  }
  callback();
}

const firstTotal = computed(() => (Number(form.value.feeStandard) || 0) + 1000);

const bedLabel = computed(() => {
  const bed = bedOptions.value.find(item => item.bedId === form.value.bedId);
  if (bed) {
    return bed.floorNo + "楼 " + bed.roomNo + " " + bed.bedNo + "床";
  }
  const room = roomOptions.value.find(item => item.roomId === form.value.roomId);
  if (room) {
    return room.floorNo + "楼 " + room.roomNo;
  }
  return "-";
});

const contractPeriod = computed(() => {
  if (!form.value.contractStart && !form.value.contractEnd) {
    return "-";
  }
  return (form.value.contractStart || "-") + " 至 " + (form.value.contractEnd || "-");
});

function createEmptyForm() {
  return {
    checkinId: undefined,
    applyNo: undefined,
    elderName: undefined,
    gender: "0",
    idCard: undefined,
    birthday: undefined,
    age: undefined,
    phone: undefined,
    address: undefined,
    avatar: undefined,
    idCardFront: undefined,
    idCardBack: undefined,
    familyName: undefined,
    familyRelation: undefined,
    familyPhone: undefined,
    applyStatus: undefined,
    selfCare: undefined,
    mentalState: undefined,
    disease: undefined,
    assessmentResult: undefined,
    suggestCareLevel: undefined,
    assessor: undefined,
    assessTime: undefined,
    auditResult: undefined,
    auditOpinion: undefined,
    auditor: undefined,
    auditTime: undefined,
    roomId: undefined,
    bedId: undefined,
    careLevel: undefined,
    nurseIds: undefined,
    feeStandard: undefined,
    contractNo: undefined,
    contractStart: undefined,
    contractEnd: undefined,
    contractFile: undefined,
    signDate: undefined,
    payStatus: undefined,
    payMethod: undefined,
    payVoucher: undefined,
    checkinDate: undefined
  };
}

/** 查询入住申请列表 */
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

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.elderName = undefined;
  queryParams.value.applyStatus = undefined;
  handleQuery();
}

function isFinished(row) {
  return row.applyStatus === "6" || row.applyStatus === "9";
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

function roomLabel(room) {
  return room.floorNo + "楼 " + room.roomNo + " " + dictLabel(biz_room_type, room.roomType);
}

/** 加载房/床/护理员基础数据 */
function loadMasterData() {
  listRoom({ pageNum: 1, pageSize: 100 }).then(res => {
    roomOptions.value = res.rows || [];
  }).catch(() => {});
  getAvailableBeds().then(res => {
    bedOptions.value = res.data || [];
  }).catch(() => {});
  listNurse({ pageNum: 1, pageSize: 100 }).then(res => {
    nurseOptions.value = res.rows || [];
  }).catch(() => {});
  loadNurseWorkload();
}

/** 加载护理员已负责床位数量（用于提前提示超上限） */
function loadNurseWorkload() {
  nurseBedCounts.value = {};
  currentBedNurseIds.value = [];
  getTaskOverview().then(res => {
    const counts = {};
    (res.data || []).forEach(floor => {
      (floor.rooms || []).forEach(room => {
        (room.beds || []).forEach(bed => {
          if (bed.bedId === form.value.bedId) {
            currentBedNurseIds.value = (bed.nurses || []).map(nurse => nurse.nurseId);
          }
          (bed.nurses || []).forEach(nurse => {
            counts[nurse.nurseId] = (counts[nurse.nurseId] || 0) + 1;
          });
        });
      });
    });
    nurseBedCounts.value = counts;
  }).catch(() => {});
}

/** 按房间实时加载可用床位（只保留空闲且未绑定老人的床位） */
function loadRoomBeds(roomId) {
  if (!roomId) {
    roomBeds.value = [];
    return Promise.resolve();
  }
  return getRoomDetail(roomId).then(res => {
    const beds = ((res.data || {}).beds || []).filter(bed => bed.status === "0" && !bed.elderId);
    // 补充楼层、房间号等展示字段
    roomBeds.value = beds.map(bed => {
      const origin = bedOptions.value.find(item => item.bedId === bed.bedId);
      return origin ? Object.assign({}, origin, bed) : bed;
    });
    if (form.value.bedId && !roomBeds.value.some(bed => bed.bedId === form.value.bedId)) {
      form.value.bedId = undefined;
    }
  }).catch(() => {
    roomBeds.value = [];
  });
}

/** 选择房间后自动刷新该房间床位列表 */
function handleRoomChange(roomId) {
  form.value.bedId = undefined;
  roomBeds.value = [];
  loadRoomBeds(roomId);
}

/** 选择床位后立即校验是否已被占用 */
function handleBedChange(bedId) {
  if (!bedId || !form.value.roomId) {
    return;
  }
  getRoomDetail(form.value.roomId).then(res => {
    const beds = (res.data || {}).beds || [];
    const bed = beds.find(item => item.bedId === bedId);
    if (!bed || bed.status !== "0" || bed.elderId) {
      proxy.$modal.msgWarning("该床位已被占用，请重新选择");
      form.value.bedId = undefined;
    }
    roomBeds.value = beds
      .filter(item => item.status === "0" && !item.elderId)
      .map(item => {
        const origin = bedOptions.value.find(b => b.bedId === item.bedId);
        return origin ? Object.assign({}, origin, item) : item;
      });
  }).catch(() => {});
}

/** 判断护理员是否已超出负责床位上限 */
function isNurseOverLimit(nurseId) {
  let count = nurseBedCounts.value[nurseId] || 0;
  // 当前床位若已绑定该护理员则不重复计新增
  if (!currentBedNurseIds.value.includes(nurseId)) {
    count += 1;
  }
  return count > NURSE_MAX_BEDS;
}

/** 护理员分配联动校验：超出上限时立即提示并取消选择 */
function handleNurseChange(ids) {
  const overflow = (ids || []).filter(isNurseOverLimit);
  if (!overflow.length) {
    return;
  }
  const names = overflow.map(id => {
    const nurse = nurseOptions.value.find(item => item.nurseId === id);
    return nurse ? nurse.nurseName : String(id);
  }).join("、");
  nurseIdsArray.value = (ids || []).filter(id => !overflow.includes(id));
  proxy.$modal.msgWarning(names + " 已负责 " + NURSE_MAX_BEDS + " 张床位，已达上限，请更换护理员");
}

/** 重置向导 */
function resetWizard() {
  form.value = createEmptyForm();
  stepIndex.value = 0;
  checkinId.value = null;
  submitLoading.value = false;
  readOnly.value = false;
  isCompleted.value = false;
  nurseIdsArray.value = [];
  roomBeds.value = [];
  nurseBedCounts.value = {};
  currentBedNurseIds.value = [];
}

/** 新增入住申请 */
function handleAdd() {
  resetWizard();
  wizardTitle.value = "新增入住申请";
  loadMasterData();
  wizardOpen.value = true;
}

/** 查看/办理入住申请 */
function handleWizard(row) {
  resetWizard();
  wizardTitle.value = "入住办理 - " + (row.applyNo || "");
  loadMasterData();
  wizardOpen.value = true;
  getCheckin(row.checkinId).then(response => {
    const data = response.data || {};
    form.value = Object.assign(createEmptyForm(), data);
    nurseIdsArray.value = data.nurseIds
      ? String(data.nurseIds).split(",").filter(item => item).map(item => Number(item))
      : [];
    // 回显时同步刷新床位数与护理员工作量
    if (data.roomId) {
      loadRoomBeds(data.roomId);
    }
    loadNurseWorkload();
    const status = String(data.applyStatus || "0");
    checkinId.value = data.checkinId;
    readOnly.value = status === "9" || status === "6";
    isCompleted.value = status === "6";
    stepIndex.value = status === "9" ? 0 : Math.min(Number(status), 6);
    // 审批状态提示：状态 2 待审核时向导只读保存 + 提醒
    if (status === "2") {
      proxy.$modal.msgWarning("当前处于【待审核】状态，向导仅可修改保存不能提交后续步骤，请先通过【入住审核】审批");
    }
  }).catch(() => {});
}

/** 驳回后重新提交 */
function handleResubmit(row) {
  proxy.$modal.confirm('是否将该已驳回的申请重新提交？重新提交后状态将变为【待审核】，可再次走向导修改并提交审批。').then(() => {
    return resubmitCheckin(row.checkinId);
  }).then(() => {
    proxy.$modal.msgSuccess("已重新提交，等待审批");
    getList();
  }).catch(() => {});
}

/** 审批后继续办理提示 */
function showAuditTip() {
  proxy.$modal.msgWarning("申请处于【待审核】状态，需由机构管理员在【入住审核】页审批通过后，才能继续后续床位/签约/缴费步骤");
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

/** 入住配置步骤联动校验：床位实时占用、护理员床位上限 */
async function validateConfigStep() {
  try {
    const res = await getRoomDetail(form.value.roomId);
    const beds = (res.data || {}).beds || [];
    const bed = beds.find(item => item.bedId === form.value.bedId);
    if (!bed || bed.status !== "0" || bed.elderId) {
      proxy.$modal.msgWarning("所选床位已被占用，请重新选择可用床位");
      form.value.bedId = undefined;
      await loadRoomBeds(form.value.roomId);
      return false;
    }
  } catch (e) {
    // 接口异常时不阻断，由提交接口做最终校验
  }
  const overflowId = nurseIdsArray.value.find(isNurseOverLimit);
  if (overflowId !== undefined) {
    const nurse = nurseOptions.value.find(item => item.nurseId === overflowId);
    proxy.$modal.msgWarning((nurse ? nurse.nurseName : String(overflowId)) + " 已负责 " + NURSE_MAX_BEDS + " 张床位，已达上限");
    return false;
  }
  return true;
}

/** 下一步前置校验：表单必填 + 关键联动 */
async function ensureStepValid() {
  const valid = await validateCurrentForm();
  if (!valid) {
    proxy.$modal.msgWarning("请先完善当前步骤的必填信息");
    return false;
  }
  if (stepIndex.value === 3) {
    return await validateConfigStep();
  }
  return true;
}

/** 保存当前步骤（无ID时先创建草稿） */
async function saveStep(step) {
  form.value.nurseIds = nurseIdsArray.value.join(",");
  let id = checkinId.value;
  if (!id) {
    await addCheckin(form.value);
    const res = await listCheckin({ pageNum: 1, pageSize: 1, elderName: form.value.elderName || undefined });
    const row = (res.rows || [])[0];
    if (!row) {
      throw new Error("入住申请创建失败");
    }
    id = row.checkinId;
    checkinId.value = id;
    form.value.applyNo = row.applyNo;
  }
  await stepCheckin(id, step, form.value);
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
    if (step === 6) {
      form.value.payStatus = "1";
      form.value.applyStatus = "5";
      proxy.$modal.msgSuccess("首期账单已生成并标记已支付");
    } else {
      form.value.applyStatus = String(step);
    }
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
    proxy.$modal.msgWarning("请点击“完成入住”完成流程");
    return;
  }
  if (!(await ensureStepValid())) {
    return;
  }
  submitLoading.value = true;
  try {
    await saveStep(stepIndex.value + 1);
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

/** 完成入住 */
function handleComplete() {
  if (!checkinId.value || submitLoading.value) {
    return;
  }
  submitLoading.value = true;
  completeCheckin(checkinId.value).then(() => {
    proxy.$modal.msgSuccess("入住办理完成");
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

/** 删除入住申请 */
function handleDelete(row) {
  if (deleteLoading.value) {
    return;
  }
  proxy.$modal.confirm('是否确认删除入住申请"' + (row.applyNo || row.checkinId) + '"？').then(() => {
    deleteLoading.value = true;
    return delCheckin(row.checkinId);
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
.mt20 {
  margin-top: 20px;
}
</style>
