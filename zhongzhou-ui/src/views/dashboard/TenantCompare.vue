<template>
  <el-card shadow="never" class="panel">
    <template #header>
      <div class="panel-header">
        <span class="panel-title">机构对比（集团视图）</span>
        <span class="panel-tip">共 {{ rows.length }} 家机构</span>
      </div>
    </template>
    <div v-if="!rows.length" class="empty">暂无机构数据</div>
    <div v-for="row in rows" :key="row.tenantId" class="tenant">
      <div class="tenant-top">
        <span class="tenant-name">{{ row.tenantName }}</span>
        <span class="tenant-num">{{ row.elderCount }} 位在住</span>
      </div>
      <div class="bar">
        <div class="bar-fill" :style="{ width: `${barWidth(row.elderCount)}%` }" />
      </div>
    </div>
  </el-card>
</template>

<script setup name="TenantCompare">
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const rows = computed(() => props.data || [])
const maxCount = computed(() => Math.max(...rows.value.map((row) => Number(row.elderCount || 0)), 1))

function barWidth(count) {
  return Math.round((Number(count || 0) / maxCount.value) * 100)
}
</script>

<style scoped lang="scss">
.panel {
  border-radius: 12px;
  border: 1px solid #eef1f0;
  margin-bottom: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.panel-tip {
  font-size: 12px;
  color: #8a94a6;
}

.empty {
  padding: 32px 0;
  text-align: center;
  font-size: 13px;
  color: #a3acb9;
}

.tenant {
  padding: 10px 0;
  border-top: 1px solid #f2f5f3;

  &:first-of-type {
    border-top: none;
  }
}

.tenant-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tenant-name {
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
}

.tenant-num {
  font-size: 12px;
  color: #6b7785;
}

.bar {
  height: 8px;
  margin-top: 8px;
  background: #f2f5f3;
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #5bc08d, #2fa36b);
  transition: width 0.4s ease;
}
</style>
