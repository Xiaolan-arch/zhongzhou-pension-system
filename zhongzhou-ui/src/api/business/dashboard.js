import request from '@/utils/request'

// 工作台核心指标
export function getDashboardSummary() {
  return request({
    url: '/business/dashboard/summary',
    method: 'get'
  })
}

// 工作台趋势
export function getDashboardTrend(months = 6) {
  return request({
    url: '/business/dashboard/trend',
    method: 'get',
    params: { months }
  })
}

// 工作台分布
export function getDashboardDistribution() {
  return request({
    url: '/business/dashboard/distribution',
    method: 'get'
  })
}

// 工作台待办
export function getDashboardTodo() {
  return request({
    url: '/business/dashboard/todo',
    method: 'get'
  })
}

// 工作台布局配置
export function getDashboardLayout() {
  return request({
    url: '/business/dashboard/layout',
    method: 'get'
  })
}

// 保存工作台布局配置
export function saveDashboardLayout(layout) {
  return request({
    url: '/business/dashboard/layout',
    method: 'put',
    data: { layout }
  })
}
