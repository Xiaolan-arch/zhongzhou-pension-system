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
      <el-form-item label="房型" prop="roomType">
        <el-select v-model="queryParams.roomType" placeholder="请选择房型" clearable style="width: 200px">
          <el-option
            v-for="dict in biz_room_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option
            v-for="dict in sys_normal_disable"
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
          v-hasPermi="['business:room:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="roomList">
      <el-table-column label="房间ID" align="center" prop="roomId" width="90" />
      <el-table-column label="楼层" align="center" prop="floorNo" width="80" />
      <el-table-column label="房间号" align="center" prop="roomNo" min-width="100" />
      <el-table-column label="房型" align="center" prop="roomType" width="100">
        <template #default="scope">
          <dict-tag :options="biz_room_type" :value="scope.row.roomType" />
        </template>
      </el-table-column>
      <el-table-column label="床位数" align="center" prop="bedCount" width="90" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['business:room:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['business:room:remove']"
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

    <!-- 添加或修改房间对话框 -->
    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="roomRef" :model="form" :rules="rules" label-width="90px">
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
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessRoom">
import { listRoom, getRoom, addRoom, updateRoom, delRoom } from "@/api/business/room";

const { proxy } = getCurrentInstance();
const { biz_room_type, sys_normal_disable } = proxy.useDict("biz_room_type", "sys_normal_disable");

const roomList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    floorNo: undefined,
    roomNo: undefined,
    roomType: undefined,
    status: undefined
  },
  rules: {
    floorNo: [{ required: true, message: "楼层不能为空", trigger: "blur" }],
    roomNo: [{ required: true, message: "房间号不能为空", trigger: "blur" }],
    roomType: [{ required: true, message: "请选择房型", trigger: "change" }],
    bedCount: [{ required: true, message: "床位数不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询房间列表 */
function getList() {
  loading.value = true;
  listRoom(queryParams.value).then(response => {
    roomList.value = response.rows;
    total.value = response.total;
  }).finally(() => {
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    roomId: undefined,
    floorNo: 1,
    roomNo: undefined,
    roomType: undefined,
    bedCount: 1,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("roomRef");
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

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加房间";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  getRoom(row.roomId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改房间";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["roomRef"].validate(valid => {
    if (valid) {
      if (form.value.roomId != undefined) {
        updateRoom(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addRoom(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除房间号为"' + row.roomNo + '"的数据项？').then(function() {
    return delRoom(row.roomId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
