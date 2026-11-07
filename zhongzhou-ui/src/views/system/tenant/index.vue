<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="机构名称" prop="tenantName">
            <el-input
               v-model="queryParams.tenantName"
               placeholder="请输入机构名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="机构状态" clearable style="width: 200px">
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
               v-hasPermi="['system:tenant:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['system:tenant:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['system:tenant:remove']"
            >删除</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="tenantList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="机构编号" align="center" prop="tenantId" width="100" />
         <el-table-column label="机构名称" align="center" prop="tenantName" />
         <el-table-column label="机构编码" align="center" prop="tenantCode" />
         <el-table-column label="联系人" align="center" prop="contactName" />
         <el-table-column label="联系电话" align="center" prop="contactPhone" width="130" />
         <el-table-column label="到期时间" align="center" prop="expireTime" width="170">
            <template #default="scope">
               <span>{{ parseTime(scope.row.expireTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="90">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="170">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="160" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:tenant:edit']">修改</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:tenant:remove']">删除</el-button>
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

      <!-- 添加或修改机构对话框 -->
      <el-dialog :title="title" v-model="open" width="560px" append-to-body>
         <el-form ref="tenantRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="机构名称" prop="tenantName">
               <el-input v-model="form.tenantName" placeholder="请输入机构名称" />
            </el-form-item>
            <el-form-item label="机构编码" prop="tenantCode">
               <el-input v-model="form.tenantCode" placeholder="请输入机构编码" />
            </el-form-item>
            <el-form-item label="联系人" prop="contactName">
               <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
               <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="机构地址" prop="address">
               <el-input v-model="form.address" placeholder="请输入机构地址" />
            </el-form-item>
            <el-form-item label="到期时间" prop="expireTime">
               <el-date-picker
                  v-model="form.expireTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择到期时间"
                  style="width: 100%"
               />
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

<script setup name="Tenant">
import { listTenant, addTenant, delTenant, getTenant, updateTenant } from "@/api/system/tenant";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const tenantList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    tenantName: undefined,
    status: undefined
  },
  rules: {
    tenantName: [{ required: true, message: "机构名称不能为空", trigger: "blur" }],
    contactPhone: [{ pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询机构列表 */
function getList() {
  loading.value = true;
  listTenant(queryParams.value).then(response => {
    tenantList.value = response.rows;
    total.value = response.total;
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
    tenantId: undefined,
    tenantName: undefined,
    tenantCode: undefined,
    contactName: undefined,
    contactPhone: undefined,
    address: undefined,
    expireTime: undefined,
    status: "0",
    remark: undefined
  };
  proxy.resetForm("tenantRef");
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
/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.tenantId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加机构";
}
/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const tenantId = row.tenantId || ids.value;
  getTenant(tenantId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改机构";
  });
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs["tenantRef"].validate(valid => {
    if (valid) {
      if (form.value.tenantId != undefined) {
        updateTenant(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addTenant(form.value).then(() => {
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
  const tenantIds = row.tenantId || ids.value;
  proxy.$modal.confirm('是否确认删除机构编号为"' + tenantIds + '"的数据项？').then(function() {
    return delTenant(tenantIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
