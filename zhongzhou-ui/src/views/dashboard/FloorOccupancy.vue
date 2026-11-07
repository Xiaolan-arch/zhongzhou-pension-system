<template>
  <el-card shadow="never" class="panel">
    <template #header>
      <div class="panel-header">
        <span class="panel-title">楼层床位占用</span>
        <span class="panel-tip">共 {{ totalBeds }} 张床位 · 空余 {{ totalFree }} 张</span>
      </div>
    </template>
    <div v-if="!rows.length" class="empty">暂无床位数据</div>
    <div v-for="row in rows" :key="row.name" class="floor">
      <div class="floor-top">
        <span class="floor-name">{{ row.name }}</span>
        <span class="floor-num">{{ row.occupied }} / {{ row.total }} 已住</span>
      </div>
      <div class="bar">
        <div class="bar-fill" :class="{ warn: row.rate >= 90 }" :style="{ width: `${Math.min(row.rate, 100)}%` }" />
      </div>
      <div class="floor-foot">
        <span>使用率 {{ row.rate }}%</span>
        <span>空余 {{ row.free }} 张</span>
      </div>
    </div>
  </el-card>
</template>

<script setup name="FloorOccupancy">
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const rows = computed(() => props.data || [])
const totalBeds = computed(() => rows.value.reduce((sum, row) => sum + Number(row.total || 0), 0))
const totalFree = computed(() => rows.value.reduce((sum, row) => sum + Number(row.free || 0), 0))
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

.floor {
  padding: 10px 0;
  border-top: 1px solid #f2f5f3;

  &:first-of-type {
    border-top: none;
  }
}

.floor-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.floor-name {
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
}

.floor-num {
  font-size: 12px;
  color: #6b7785;
}

.bar {
  height: 8px;
  margin: 8px 0 6px;
  background: #f2f5f3;
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #5bc08d, #2fa36b);
  transition: width 0.4s ease;

  &.warn {
    background: linear-gradient(90deg, #f2c079, #f2a65a);
  }
}

.floor-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: #a3acb9;
}
</style>
