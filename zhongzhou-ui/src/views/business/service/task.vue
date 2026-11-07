<template>
  <div class="app-container">
    <el-tabs v-model="activeFloor" v-loading="loading" class="floor-tabs">
      <el-tab-pane
        v-for="floor in floors"
        :key="floor.floorNo"
        :label="'楼层 ' + floor.floorNo"
        :name="floor.floorNo"
      >
        <el-row :gutter="16">
          <el-col
            v-for="room in floor.rooms"
            :key="room.roomId"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <el-card shadow="hover" class="room-card">
              <template #header>
                <div class="room-header">
                  <span class="room-title">{{ room.roomNo }}</span>
                  <dict-tag :options="biz_room_type" :value="room.roomType" />
                  <span class="room-bedcount">{{ room.bedCount }} 床</span>
                  <div class="room-actions">
                    <el-button
                      link
                      type="primary"
                      icon="Setting"
                      @click="handleBatchAssign(floor.floorNo)"
                      v-hasPermi="['business:service:assign']"
                    >批量设置护理员</el-button>
                  </div>
                </div>
              </template>
              <el-row :gutter="10">
                <el-col :span="12" v-for="bed in room.beds" :key="bed.bedId">
                  <div class="task-bed">
                    <div class="task-bed-head">
                      <span class="bed-no">{{ bed.bedNo }} 号床</span>
                      <el-button
                        link
                        type="primary"
                        icon="Setting"
                        title="设置护理员"
                        @click.stop="handleAssign(bed)"
                        v-hasPermi="['business:service:assign']"
                      />
                    </div>
                    <div v-if="bed.status === '1'" class="elder-name">{{ bed.elderName || "-" }}</div>
                    <div v-else class="empty-tip">{{ bed.status === "2" ? "外出中" : "空闲" }}</div>
                    <div class="nurse-tags">
                      <el-tag
                        v-for="(nurse, index) in bed.nurses"
                        :key="nurse.nurseId"
                        :type="nurseTagType(index)"
                        size="small"
                        class="nurse-tag"
                      >{{ nurse.nurseName }}</el-tag>
                      <span v-if="!bed.nurses || !bed.nurses.length" class="no-nurse">未分配护理员</span>
                    </div>
                  </div>
                </el-col>
              </el-row>
              <div v-if="!room.beds || !room.beds.length" class="empty-beds">暂无床位</div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!floor.rooms || !floor.rooms.length" description="暂无房间" />
      </el-tab-pane>
    </el-tabs>

    <!-- 单床设置护理员 -->
    <el-dialog :title="assignTitle" v-model="assignOpen" width="500px" append-to-body>
      <el-checkbox-group v-model="selectedNurseIds" class="nurse-group">
        <el-checkbox
          v-for="nurse in nurses"
          :key="nurse.nurseId"
          :label="nurse.nurseId"
        >{{ nurse.nurseName }}</el-checkbox>
      </el-checkbox-group>
      <el-empty v-if="!nurses.length" description="暂无护理员数据" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAssign">确 定</el-button>
          <el-button @click="assignOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 整层批量设置护理员 -->
    <el-dialog :title="'楼层 ' + batchFloorNo + ' 批量设置护理员'" v-model="batchOpen" width="500px" append-to-body>
      <el-checkbox-group v-model="batchNurseIds" class="nurse-group">
        <el-checkbox
          v-for="nurse in nurses"
          :key="nurse.nurseId"
          :label="nurse.nurseId"
        >{{ nurse.nurseName }}</el-checkbox>
      </el-checkbox-group>
      <el-empty v-if="!nurses.length" description="暂无护理员数据" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitBatchAssign">确 定</el-button>
          <el-button @click="batchOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessServiceTask">
import {
  getTaskOverview,
  listNurse,
  assignNurse,
  batchAssignNurse,
  getBedNurses
} from "@/api/business/service";

const { proxy } = getCurrentInstance();
const { biz_room_type } = proxy.useDict("biz_room_type");

const floors = ref([]);
const activeFloor = ref(null);
const loading = ref(true);
const nurses = ref([]);
const assignOpen = ref(false);
const assignTitle = ref("");
const currentBed = ref({});
const selectedNurseIds = ref([]);
const batchOpen = ref(false);
const batchFloorNo = ref(null);
const batchNurseIds = ref([]);

/** 护理员标签类型 */
function nurseTagType(index) {
  return ["", "success", "info", "warning", "danger"][index % 5];
}

/** 加载护理任务聚合数据 */
function loadData() {
  loading.value = true;
  getTaskOverview().then(response => {
    floors.value = response.data || [];
    if (!floors.value.some(item => item.floorNo === activeFloor.value)) {
      activeFloor.value = floors.value.length ? floors.value[0].floorNo : null;
    }
  }).finally(() => {
    loading.value = false;
  });
}

/** 加载护理员下拉数据 */
function loadNurses() {
  listNurse({ pageNum: 1, pageSize: 100 }).then(response => {
    nurses.value = response.rows || [];
  });
}

/** 打开单床护理员设置 */
function handleAssign(bed) {
  currentBed.value = bed;
  selectedNurseIds.value = (bed.nurses || []).map(nurse => nurse.nurseId);
  assignTitle.value = "设置护理员 - " + bed.bedNo + " 号床";
  assignOpen.value = true;
  getBedNurses(bed.bedId).then(response => {
    if (currentBed.value.bedId === bed.bedId) {
      selectedNurseIds.value = (response.data || []).map(nurse => nurse.nurseId);
    }
  }).catch(() => {});
}

/** 提交单床护理员设置 */
function submitAssign() {
  assignNurse({
    bedId: currentBed.value.bedId,
    nurseIds: selectedNurseIds.value
  }).then(() => {
    proxy.$modal.msgSuccess("设置成功");
    assignOpen.value = false;
    loadData();
  });
}

/** 打开整层批量设置 */
function handleBatchAssign(floorNo) {
  batchFloorNo.value = floorNo;
  batchNurseIds.value = [];
  batchOpen.value = true;
}

/** 提交整层批量设置 */
function submitBatchAssign() {
  batchAssignNurse({
    floorNo: batchFloorNo.value,
    nurseIds: batchNurseIds.value
  }).then(() => {
    proxy.$modal.msgSuccess("批量设置成功");
    batchOpen.value = false;
    loadData();
  });
}

loadData();
loadNurses();
</script>

<style scoped lang="scss">
.room-card {
  margin-bottom: 16px;
}

.room-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.room-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.room-bedcount {
  color: #909399;
  font-size: 13px;
}

.room-actions {
  margin-left: auto;
  white-space: nowrap;
}

.task-bed {
  margin-bottom: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  background-color: #fafafa;
  min-height: 76px;
}

.task-bed-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bed-no {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.elder-name {
  margin-top: 4px;
  color: #67c23a;
  font-size: 14px;
}

.empty-tip {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.nurse-tags {
  margin-top: 4px;
  min-height: 24px;
}

.nurse-tag {
  margin: 0 5px 4px 0;
}

.no-nurse {
  color: #c0c4cc;
  font-size: 12px;
}

.empty-beds {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 10px 0;
}

.nurse-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  min-height: 40px;
}
</style>
