<template>
  <div class="app-container">
    <div class="toolbar-row mb8">
      <el-button
        type="primary"
        plain
        icon="Plus"
        @click="handleAddRoom"
        v-hasPermi="['business:room:add']"
      >新增房间</el-button>
      <div class="legend">
        <span class="legend-item"><i class="legend-dot dot-free"></i>空闲</span>
        <span class="legend-item"><i class="legend-dot dot-occupied"></i>已入住</span>
        <span class="legend-item"><i class="legend-dot dot-out"></i>外出中</span>
      </div>
    </div>

    <!-- 待完善档案：家属端自助绑定创建的待入住档案，需完善信息并分配床位 -->
    <el-card v-if="pendingElders.length" shadow="never" class="pending-card">
      <template #header>
        <div class="pending-header">
          <span class="pending-title">待完善档案（{{ pendingElders.length }}）</span>
          <span class="pending-tip">家属端自助创建或待入住，请完善信息并分配床位</span>
        </div>
      </template>
      <el-table :data="pendingElders" size="small" border>
        <el-table-column label="姓名" prop="elderName" width="100" />
        <el-table-column label="联系电话" prop="phone" width="130" />
        <el-table-column label="家庭联系人" prop="familyName" width="120" />
        <el-table-column label="与老人关系" prop="familyRelation" width="100" />
        <el-table-column label="状态" width="90">
          <template #default>待入住</template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="190">
          <template #default="scope">
            <el-button link type="primary" @click="goArchive(scope.row)">查看档案</el-button>
            <el-button link type="primary" @click="goCheckinApply">分配床位</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
                    <el-button link type="primary" @click="handleRoomDetail(room)">详情</el-button>
                    <el-button
                      link
                      type="primary"
                      @click="handleUpdateRoom(room)"
                      v-hasPermi="['business:room:edit']"
                    >修改</el-button>
                    <el-button
                      link
                      type="danger"
                      @click="handleDeleteRoom(room)"
                      v-hasPermi="['business:room:remove']"
                    >删除</el-button>
                  </div>
                </div>
              </template>
              <el-row :gutter="10">
                <el-col :span="12" v-for="bed in room.beds" :key="bed.bedId">
                  <div
                    class="bed-card"
                    :class="['bed-status-' + bed.status, { 'bed-clickable': bed.status === '1' }]"
                    @click="handleBedClick(bed)"
                  >
                    <div class="bed-no">{{ bed.bedNo }} 号床</div>
                    <template v-if="bed.status === '1'">
                      <div class="bed-elder">{{ bed.elderName || "-" }}</div>
                      <div class="bed-care" v-if="bed.careLevel">
                        <dict-tag :options="biz_care_level" :value="bed.careLevel" />
                      </div>
                    </template>
                    <div v-else class="bed-empty">{{ bed.status === "2" ? "外出中" : "空闲" }}</div>
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

    <!-- 新增或修改房间对话框 -->
    <el-dialog :title="roomTitle" v-model="roomOpen" width="500px" append-to-body>
      <el-form ref="roomRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="楼层" prop="floorNo">
          <el-input-number v-model="form.floorNo" :min="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="房间号" prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="房型" prop="roomType">
          <el-select v-model="form.roomType" placeholder="请选择房型" clearable style="width: 100%">
            <el-option
              v-for="dict in biz_room_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="床位数" prop="bedCount">
          <el-input-number v-model="form.bedCount" :min="1" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitRoom">确 定</el-button>
          <el-button @click="cancelRoom">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 房间详情对话框 -->
    <el-dialog title="房间详情" v-model="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="房间号">{{ roomDetail.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">
          <dict-tag :options="biz_room_type" :value="roomDetail.roomType" />
        </el-descriptions-item>
        <el-descriptions-item label="床位数">{{ roomDetail.bedCount }}</el-descriptions-item>
        <el-descriptions-item label="入住率">{{ roomDetail.occupancyRate != null ? roomDetail.occupancyRate + "%" : "-" }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="roomDetail.beds || []" class="mt20">
        <el-table-column label="床位号" align="center" prop="bedNo" width="90" />
        <el-table-column label="老人姓名" align="center" prop="elderName">
          <template #default="scope">{{ scope.row.elderName || "-" }}</template>
        </el-table-column>
        <el-table-column label="性别" align="center" prop="gender" width="70">
          <template #default="scope">{{ genderText(scope.row.gender) }}</template>
        </el-table-column>
        <el-table-column label="年龄" align="center" prop="age" width="70">
          <template #default="scope">{{ scope.row.age != null ? scope.row.age : "-" }}</template>
        </el-table-column>
        <el-table-column label="护理等级" align="center" prop="careLevel" width="110">
          <template #default="scope">
            <dict-tag v-if="scope.row.careLevel" :options="biz_care_level" :value="scope.row.careLevel" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="入住日期" align="center" prop="checkinDate" width="110">
          <template #default="scope">
            <span>{{ parseTime(scope.row.checkinDate, '{y}-{m}-{d}') || "-" }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template #default="scope">
            <el-button v-if="scope.row.elderId" link type="primary" @click="handleBedClick(scope.row)">老人信息</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 老人信息对话框 -->
    <el-dialog title="老人信息" v-model="elderOpen" width="500px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ currentBed.elderName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderText(currentBed.gender) }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentBed.age != null ? currentBed.age : "-" }}</el-descriptions-item>
        <el-descriptions-item label="护理等级">
          <dict-tag v-if="currentBed.careLevel" :options="biz_care_level" :value="currentBed.careLevel" />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="入住日期" :span="2">
          {{ parseTime(currentBed.checkinDate, '{y}-{m}-{d}') || "-" }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            @click="handleArchive"
            v-hasPermi="['business:residence:list']"
          >查看档案</el-button>
          <el-button
            type="danger"
            @click="handleCheckout"
            v-hasPermi="['business:checkout:edit']"
          >退住申请</el-button>
          <el-button @click="elderOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessResidence">
import { getOverview, getRoomDetail } from "@/api/business/residence";
import { getRoom, addRoom, updateRoom, delRoom } from "@/api/business/room";
import { listElder } from "@/api/business/elder";
import { addCheckout } from "@/api/business/checkout";
import { useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();
const { biz_room_type, biz_care_level } = proxy.useDict("biz_room_type", "biz_care_level");

const floors = ref([]);
const activeFloor = ref(null);
const loading = ref(true);
const roomOpen = ref(false);
const roomTitle = ref("");
const detailOpen = ref(false);
const roomDetail = ref({});
const elderOpen = ref(false);
const currentBed = ref({});
const pendingElders = ref([]);

const data = reactive({
  form: {},
  rules: {
    floorNo: [{ required: true, message: "楼层不能为空", trigger: "blur" }],
    roomNo: [{ required: true, message: "房间号不能为空", trigger: "blur" }],
    roomType: [{ required: true, message: "房型不能为空", trigger: "change" }],
    bedCount: [{ required: true, message: "床位数不能为空", trigger: "blur" }]
  }
});

const { form, rules } = toRefs(data);

/** 加载楼层房间床位聚合数据 */
function loadData() {
  loading.value = true;
  getOverview().then(response => {
    floors.value = response.data || [];
    if (!floors.value.some(item => item.floorNo === activeFloor.value)) {
      activeFloor.value = floors.value.length ? floors.value[0].floorNo : null;
    }
  }).finally(() => {
    loading.value = false;
  });
}

/** 加载待入住 / 待完善档案（家属端自助创建） */
function loadPendingElders() {
  listElder({ pageNum: 1, pageSize: 100, status: "2" }).then(response => {
    pendingElders.value = response.rows || [];
  }).catch(() => {});
}

/** 查看待完善老人档案 */
function goArchive(row) {
  router.push(`/residence/elderArchive/${row.elderId}`);
}

/** 前往入住办理分配床位 */
function goCheckinApply() {
  router.push("/checkin/apply");
}

/** 性别文本 */
function genderText(gender) {
  if (gender === "0") return "男";
  if (gender === "1") return "女";
  return "-";
}

/** 新增房间 */
function handleAddRoom() {
  resetRoom();
  form.value.floorNo = activeFloor.value || 1;
  roomOpen.value = true;
  roomTitle.value = "添加房间";
}

/** 修改房间 */
function handleUpdateRoom(row) {
  resetRoom();
  getRoom(row.roomId).then(response => {
    form.value = response.data;
    roomOpen.value = true;
    roomTitle.value = "修改房间";
  });
}

/** 房间表单重置 */
function resetRoom() {
  form.value = {
    roomId: undefined,
    floorNo: activeFloor.value || 1,
    roomNo: undefined,
    roomType: undefined,
    bedCount: 1,
    status: "0"
  };
  proxy.resetForm("roomRef");
}

/** 取消房间编辑 */
function cancelRoom() {
  roomOpen.value = false;
  resetRoom();
}

/** 提交房间 */
function submitRoom() {
  proxy.$refs["roomRef"].validate(valid => {
    if (valid) {
      if (form.value.roomId != undefined) {
        updateRoom(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          roomOpen.value = false;
          loadData();
        });
      } else {
        addRoom(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          roomOpen.value = false;
          loadData();
        });
      }
    }
  });
}

/** 删除房间 */
function handleDeleteRoom(row) {
  proxy.$modal.confirm('是否确认删除房间号为"' + row.roomNo + '"的数据项？').then(function() {
    return delRoom(row.roomId);
  }).then(() => {
    loadData();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 房间详情 */
function handleRoomDetail(row) {
  getRoomDetail(row.roomId).then(response => {
    roomDetail.value = response.data || {};
    detailOpen.value = true;
  });
}

/** 点击已入住床位查看老人信息 */
function handleBedClick(bed) {
  if (bed.status !== "1") {
    return;
  }
  currentBed.value = bed;
  elderOpen.value = true;
}

/** 查看老人档案 */
function handleArchive() {
  if (!currentBed.value.elderId) {
    proxy.$modal.msgWarning("该床位暂无入住老人");
    return;
  }
  elderOpen.value = false;
  router.push(`/residence/elderArchive/${currentBed.value.elderId}`);
}

/** 退住申请 */
function handleCheckout() {
  proxy.$modal.confirm('是否确认为老人"' + currentBed.value.elderName + '"提交退住申请？').then(function() {
    return addCheckout({
      elderId: currentBed.value.elderId,
      checkoutDate: proxy.parseTime(new Date(), "{y}-{m}-{d}"),
      reason: "家属申请退住"
    });
  }).then(() => {
    proxy.$modal.msgSuccess("退住申请提交成功");
    elderOpen.value = false;
    loadData();
  }).catch(() => {});
}

loadData();
loadPendingElders();
</script>

<style scoped lang="scss">
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.legend {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #606266;
  font-size: 14px;
}

.legend-item {
  display: flex;
  align-items: center;
}

.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  margin-right: 5px;
  border-radius: 3px;
}

.dot-free {
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
}

.dot-occupied {
  background-color: #f0f9eb;
  border: 1px solid #b2e19a;
}

.dot-out {
  background-color: #fdf6ec;
  border: 1px solid #f5dab1;
}

.room-card {
  margin-bottom: 16px;
}

.pending-card {
  margin-bottom: 16px;
}

.pending-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pending-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.pending-tip {
  color: #e6a23c;
  font-size: 13px;
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

.bed-card {
  margin-bottom: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  min-height: 66px;
  border: 1px solid transparent;
}

.bed-status-0 {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.bed-status-1 {
  background-color: #f0f9eb;
  border-color: #b2e19a;
}

.bed-status-2 {
  background-color: #fdf6ec;
  border-color: #f5dab1;
}

.bed-clickable {
  cursor: pointer;
}

.bed-no {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.bed-elder {
  margin-top: 4px;
  color: #67c23a;
  font-size: 14px;
}

.bed-care {
  margin-top: 4px;
}

.bed-empty {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}

.empty-beds {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 10px 0;
}

.mt20 {
  margin-top: 20px;
}
</style>
