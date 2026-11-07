<template>
  <div class="kpi-card" :class="`tone-${tone}`" @click="$emit('click')">
    <div class="kpi-icon">
      <el-icon><component :is="iconComp" /></el-icon>
    </div>
    <div class="kpi-body">
      <div class="kpi-label">{{ label }}</div>
      <div class="kpi-value">
        {{ value }}<span v-if="unit" class="kpi-unit">{{ unit }}</span>
      </div>
      <div class="kpi-desc">{{ desc }}</div>
    </div>
  </div>
</template>

<script setup name="KpiCard">
import { computed } from 'vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const props = defineProps({
  label: { type: String, default: '' },
  value: { type: [String, Number], default: 0 },
  unit: { type: String, default: '' },
  desc: { type: String, default: '' },
  icon: { type: String, default: 'DataLine' },
  tone: { type: String, default: 'green' },
  path: { type: String, default: '' }
})

const iconComp = computed(() => ElementPlusIcons[props.icon] || ElementPlusIcons.DataLine)
</script>

<style scoped lang="scss">
.kpi-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 16px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef1f0;
  box-shadow: 0 4px 16px rgba(31, 45, 61, 0.04);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  min-height: 108px;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(47, 163, 107, 0.35);
    box-shadow: 0 10px 26px rgba(31, 45, 61, 0.1);
  }
}

.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
  flex-shrink: 0;
}

.kpi-body {
  min-width: 0;
  flex: 1;
}

.kpi-label {
  font-size: 13px;
  color: #6b7785;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.kpi-value {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
  color: #1f2d3d;
  font-family: 'DIN Alternate', 'Helvetica Neue', Arial, sans-serif;
}

.kpi-unit {
  margin-left: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #8a94a6;
}

.kpi-desc {
  margin-top: 8px;
  font-size: 12px;
  color: #8a94a6;
  line-height: 1.5;
}

.tone-green {
  .kpi-icon { background: #e8f6ee; color: #2fa36b; }
}
.tone-blue {
  .kpi-icon { background: #eaf2fe; color: #4a8ef0; }
}
.tone-orange {
  .kpi-icon { background: #fdf3e7; color: #f2a65a; }
}
.tone-red {
  .kpi-icon { background: #fdeceb; color: #f05a5a; }
}
.tone-purple {
  .kpi-icon { background: #f0edfb; color: #7a6ad8; }
}
.tone-teal {
  .kpi-icon { background: #e6f5f5; color: #1f9c94; }
}
</style>
