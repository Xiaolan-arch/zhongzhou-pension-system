import request from '@/utils/request'

// 查询护理任务列表
export function listServiceTask(query) {
  return request({
    url: '/business/serviceTask/list',
    method: 'get',
    params: query
  })
}

// 更新护理任务状态（0待执行 1已执行 2已完成 3已取消 4已逾期），完成时可填写执行反馈
export function updateServiceTaskStatus(taskId, taskStatus, feedback) {
  return request({
    url: '/business/serviceTask/status/' + taskId,
    method: 'put',
    params: { taskStatus: taskStatus, feedback: feedback }
  })
}
