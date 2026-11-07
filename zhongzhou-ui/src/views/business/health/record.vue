<template>
  <div class="app-container">
    <!-- 1. 查询条件 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="老人姓名" prop="elderId">
        <el-select v-model="queryParams.elderId" placeholder="请选择老人" filterable clearable style="width: 200px;" @change="onQueryElderChange">
          <el-option v-for="e in elderOptions" :key="e.elderId" :label="e.elderName + ' (' + (e.age||'') + '岁)'" :value="Number(e.elderId)" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否异常" prop="isAbnormal">
        <el-select v-model="queryParams.isAbnormal" placeholder="全部" clearable style="width: 120px;" @change="getList">
          <el-option label="全部" value="" />
          <el-option label="正常" value="0" />
          <el-option label="异常" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="测量日期">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" @change="getList" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 2. 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:health:add']">录入体征</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="MagicStick" @click="handleBatchAdd" v-hasPermi="['business:health:add']">批量录入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Warning" @click="showOnlyAbnormal = !showOnlyAbnormal">
          {{ showOnlyAbnormal ? '显示全部' : '仅异常老人' }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 3. 列表 -->
    <el-table v-loading="loading" :data="pagedList" border stripe>
      <el-table-column label="日期" align="center" width="140">
        <template #default="scope">{{ scope.row.recordDate }} {{ scope.row.recordTime }}</template>
      </el-table-column>
      <el-table-column label="老人" align="center" prop="elderName" width="110" />
      <el-table-column label="血压(高压/低压)" align="center" width="170">
        <template #default="scope">
          <span :class="bpHighClass(scope.row)">{{ scope.row.bloodPressureHigh ?? '-' }}</span>
          <span class="muted">/</span>
          <span :class="bpLowClass(scope.row)">{{ scope.row.bloodPressureLow ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="心率" align="center" width="80">
        <template #default="scope"><span :class="hrClass(scope.row)">{{ scope.row.heartRate ?? '-' }}</span></template>
      </el-table-column>
      <el-table-column label="体温" align="center" width="80">
        <template #default="scope"><span :class="tempClass(scope.row)">{{ scope.row.bodyTemp ?? '-' }}</span></template>
      </el-table-column>
      <el-table-column label="血糖" align="center" width="90">
        <template #default="scope"><span :class="sugarClass(scope.row)">{{ scope.row.bloodSugar ?? '-' }}</span></template>
      </el-table-column>
      <el-table-column label="血氧" align="center" width="90">
        <template #default="scope"><span :class="oxygenClass(scope.row)">{{ scope.row.bloodOxygen ?? '-' }}</span></template>
      </el-table-column>
      <el-table-column label="体重/步数/睡眠" align="center" width="190">
        <template #default="scope">
          {{ scope.row.weight ?? '-' }}kg / {{ scope.row.steps ?? '-' }}步 / {{ scope.row.sleepHours ?? '-' }}h
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isAbnormal === '1'" type="danger" effect="dark">异常</el-tag>
          <el-tag v-else type="success" effect="plain">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="异常项" align="center" min-width="180">
        <template #default="scope">
          <el-tag v-for="(item, idx) in splitAbnormal(scope.row.abnormalItems)" :key="idx" size="small" type="danger" style="margin: 2px 4px;">{{ item }}</el-tag>
          <span v-if="!scope.row.abnormalItems" class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="130" show-overflow-tooltip />
      <el-table-column label="录入人" align="center" prop="createBy" width="90" />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:health:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:health:remove']">删除</el-button>
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

    <!-- 4. 单条录入弹窗 -->
    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="老人" prop="elderId">
              <el-select v-model="form.elderId" filterable clearable placeholder="选择老人（必填）" style="width: 100%;" @change="onFormElderChange">
                <el-option v-for="e in elderOptions" :key="e.elderId" :label="e.elderName + ' (' + (e.age||'') + '岁)'" :value="Number(e.elderId)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="测量日期" prop="recordDate">
              <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="测量时间">
              <el-time-picker v-model="form.recordTime" format="HH:mm" value-format="HH:mm" placeholder="HH:mm" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收缩压(mmHg)">
              <el-input-number v-model="form.bloodPressureHigh" :min="40" :max="260" :controls="false" placeholder="90-140" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="舒张压(mmHg)">
              <el-input-number v-model="form.bloodPressureLow" :min="20" :max="180" :controls="false" placeholder="60-90" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="心率(次/分)">
              <el-input-number v-model="form.heartRate" :min="30" :max="200" :controls="false" placeholder="60-100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="体温(℃)">
              <el-input-number v-model="form.bodyTemp" :min="34" :max="42" :precision="1" :step="0.1" :controls="false" placeholder="36-37.3" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="血糖(mmol/L)">
              <el-input-number v-model="form.bloodSugar" :min="3" :max="15" :precision="2" :step="0.1" :controls="false" placeholder="<=7.0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="血氧(%)">
              <el-input-number v-model="form.bloodOxygen" :min="85" :max="100" :precision="1" :step="0.1" :controls="false" placeholder=">=95" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="体重(kg)">
              <el-input-number v-model="form.weight" :min="30" :max="150" :precision="1" :step="0.1" :controls="false" placeholder="体重" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="步数">
              <el-input-number v-model="form.steps" :min="0" :max="50000" :controls="false" placeholder="步数" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="睡眠(h)">
              <el-input-number v-model="form.sleepHours" :min="0" :max="24" :precision="1" :step="0.5" :controls="false" placeholder="睡眠时长" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="1" maxlength="200" show-word-limit placeholder="可选备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listHealthRecord, addHealthRecord, updateHealthRecord, delHealthRecord } from '@/api/business/health'
import { listElder } from '@/api/business/elder'

const { proxy } = getCurrentInstance()
const { parseTime } = proxy

const healthOptions = [{ value: '0', label: '正常' }, { value: '1', label: '异常' }]

const open = ref(false)
const submitting = ref(false)
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const showOnlyAbnormal = ref(false)
const dateRange = ref([])
const elderOptions = ref([])
const records = ref([])
const formRef = ref()

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, elderId: null, isAbnormal: '' },
  rules: {
    elderId: [{ required: true, message: '请选择老人', trigger: 'change' }],
    recordDate: [{ required: true, message: '请选择测量日期', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = data

// ================= 分页 + 筛选 =================
const pagedList = computed(() => {
  let list = records.value
  if (showOnlyAbnormal.value) list = list.filter(r => r.isAbnormal === '1')
  if (dateRange.value && dateRange.value.length === 2) {
    const [a, b] = dateRange.value
    list = list.filter(r => r.recordDate >= a && r.recordDate <= b)
  }
  total.value = list.length
  const start = (queryParams.pageNum - 1) * queryParams.pageSize
  return list.slice(start, start + queryParams.pageSize)
})

function loadElderOptions() {
  listElder({ pageNum: 1, pageSize: 500 }).then(res => {
    elderOptions.value = res.rows || []
  })
}

/** 查询栏老人选择 */
function onQueryElderChange(val) {
  queryParams.value.elderId = val === '' || val == null ? null : Number(val)
  getList()
}

/** 表单老人选择（Element Plus filterable 可能把 number 转成 string，强制转回来） */
function onFormElderChange(val) {
  if (val === '' || val == null) {
    form.value.elderId = null
    return
  }
  form.value.elderId = Number(val)
}

function getList() {
  loading.value = true
  listHealthRecord(queryParams).then(res => {
    records.value = res.rows || []
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  proxy.resetForm('queryForm')
  dateRange.value = []
  getList()
}

// ================= 表单 =================
function reset() {
  form.value = {
    recordId: null, elderId: null,
    recordDate: new Date().toISOString().slice(0, 10),
    recordTime: new Date().toTimeString().slice(0, 5),
    bloodPressureHigh: null, bloodPressureLow: null,
    heartRate: null, bodyTemp: null, bloodSugar: null, bloodOxygen: null,
    weight: null, steps: null, sleepHours: null, remark: ''
  }
}

function handleAdd() {
  reset(); title.value = '录入体征'; open.value = true
}
function handleBatchAdd() { proxy.$modal.msgWarning('批量录入即将上线，支持多老人同日期一次性录入'); handleAdd() }

function handleUpdate(row) {
  reset()
  getHealthRecordLocal(row.recordId)
  title.value = '修改体征'
  open.value = true
  Object.assign(form.value, row)
}
function getHealthRecordLocal(id) { return id }

function submitForm() {
  proxy.$refs.formRef.validate((valid, invalidFields) => {
    if (!valid) {
      console.warn('form invalid:', invalidFields);
      proxy.$modal.msgWarning('请检查必填项（老人 + 日期）');
      return
    }
    // Element Plus filterable 下拉可能把 number elderId 变成 string — 提交前强制转 Number
    if (form.value.elderId != null && form.value.elderId !== '') {
      form.value.elderId = Number(form.value.elderId)
    }
    submitting.value = true
    const p = form.value.recordId
      ? updateHealthRecord(form.value)
      : addHealthRecord(form.value)
    p.then(() => {
      proxy.$modal.msgSuccess(form.value.recordId ? '修改成功' : '录入成功')
      open.value = false; getList()
    }).finally(() => { submitting.value = false })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该体征记录？').then(() => {
    return delHealthRecord(row.recordId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList()
  }).catch(() => {})
}

function cancel() { open.value = false; reset() }

// ================= 异常颜色 =================
function bpHighClass(r) {
  if (r.bloodPressureHigh == null) return ''
  if (r.bloodPressureHigh > 140 || r.bloodPressureHigh < 90) return 'abnormal'
  return 'normal'
}
function bpLowClass(r) {
  if (r.bloodPressureLow == null) return ''
  if (r.bloodPressureLow > 90 || r.bloodPressureLow < 60) return 'abnormal'
  return 'normal'
}
function hrClass(r) {
  if (r.heartRate == null) return ''
  if (r.heartRate < 60 || r.heartRate > 100) return 'abnormal'
  return 'normal'
}
function tempClass(r) {
  if (r.bodyTemp == null) return ''
  if (r.bodyTemp < 36.0 || r.bodyTemp > 37.3) return 'abnormal'
  return 'normal'
}
function sugarClass(r) {
  if (r.bloodSugar == null) return ''
  if (r.bloodSugar > 7.0) return 'abnormal'
  return 'normal'
}
function oxygenClass(r) {
  if (r.bloodOxygen == null) return ''
  if (r.bloodOxygen < 95.0) return 'abnormal'
  return 'normal'
}
function splitAbnormal(s) {
  if (!s) return []
  return s.split(',').filter(x => x.trim())
}

onMounted(() => { loadElderOptions(); getList() })
</script>

<style>
.abnormal { color: #f56c6c !important; font-weight: 600; }
.normal { color: #67c23a !important; }
.muted { color: #c0c4cc; }
</style>
