<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="楼层" prop="floorNo">
        <el-input-number
          v-model="queryParams.floorNo"
          :min="1"
          controls-position="right"
          placeholder="楼层"
          style="width: 140px"
        />
      </el-form-item>
      <el-form-item label="房间号" prop="roomNo">
        <el-input
          v-model="queryParams.roomNo"
          placeholder="请输入房间号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="床位号" prop="bedNo">
        <el-input
          v-model="queryParams.bedNo"
          placeholder="请输入床位号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="床位状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="空闲" value="0" />
          <el-option label="已入住" value="1" />
          <el-option label="外出中" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="bedList">
      <el-table-column label="床位ID" align="center" prop="bedId" width="90" />
      <el-table-column label="楼层" align="center" prop="floorNo" width="80">
        <template #default="scope">{{ scope.row.floorNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="房间号" align="center" prop="roomNo" min-width="100">
        <template #default="scope">{{ scope.row.roomNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="床位号" align="center" prop="bedNo" width="90" />
      <el-table-column label="床位状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === '0'" type="info">空闲</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success">已入住</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="warning">外出中</el-tag>
          <el-tag v-else type="info">{{ scope.row.status || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前老人" align="center" prop="elderName" min-width="100">
        <template #default="scope">{{ scope.row.elderName || '-' }}</template>
      </el-table-column>
      <el-table-column label="护理等级" align="center" prop="careLevel" width="110">
        <template #default="scope">
          <dict-tag v-if="scope.row.careLevel" :options="biz_care_level" :value="scope.row.careLevel" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="入住日期" align="center" prop="checkinDate" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.checkinDate, '{y}-{m}-{d}') || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分配护理员" align="center" prop="nurses" min-width="160">
        <template #default="scope">
          <template v-if="scope.row.nurses && scope.row.nurses.length">
            <el-tag
              v-for="nurse in scope.row.nurses"
              :key="nurse.nurseId"
              class="mr5 mb5"
              type="primary"
              effect="plain"
            >{{ nurse.nurseName }}</el-tag>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="UserFilled"
            @click="handleAssignNurse(scope.row)"
            v-hasPermi="['business:service:assign']"
          >分配</el-button>
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

    <!-- 分配护理员对话框 -->
    <el-dialog :title="'分配护理员 - ' + (currentBed.roomNo || '') + ' ' + (currentBed.bedNo || '') + '号床'" v-model="assignOpen" width="520px" append-to-body>
      <el-form ref="assignRef" :model="assignForm" label-width="100px">
        <el-form-item label="可选护理员">
          <el-select
            v-model="assignForm.nurseIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择要分配的护理员"
            style="width: 100%"
          >
            <el-option
              v-for="nurse in nurseOptions"
              :key="nurse.nurseId"
              :label="nurse.nurseName + ' (' + nurse.phone + ')'"
              :value="nurse.nurseId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAssign">确 定</el-button>
          <el-button @click="assignOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessBed">
import { listBed } from "@/api/business/bed";
import { listNurse, assignNurse } from "@/api/business/nurse";

const { proxy } = getCurrentInstance();
const { biz_care_level } = proxy.useDict("biz_care_level");

const bedList = ref([]);
const nurseOptions = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const assignOpen = ref(false);
const currentBed = ref({});

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    floorNo: undefined,
    roomNo: undefined,
    bedNo: undefined,
    status: undefined
  },
  assignForm: {
    nurseIds: []
  }
});

const { queryParams, assignForm } = toRefs(data);

/** 查询床位列表 */
function getList() {
  loading.value = true;
  listBed(queryParams.value).then(response => {
    bedList.value = response.rows;
    total.value = response.total;
  }).finally(() => {
    loading.value = false;
  });
}

/** 查询护理员下拉 */
function getNurseOptions() {
  listNurse({ pageNum: 1, pageSize: 50, status: "0" }).then(response => {
    nurseOptions.value = response.rows || [];
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
  handleQuery();
}

/** 分配护理员 */
function handleAssignNurse(row) {
  currentBed.value = row;
  assignForm.value.nurseIds = (row.nurses || []).map(n => n.nurseId);
  assignOpen.value = true;
}

/** 提交分配 */
function submitAssign() {
  assignNurse({
    bedId: currentBed.value.bedId,
    nurseIds: assignForm.value.nurseIds
  }).then(() => {
    proxy.$modal.msgSuccess("分配成功");
    assignOpen.value = false;
    getList();
  });
}

getList();
getNurseOptions();
</script>
