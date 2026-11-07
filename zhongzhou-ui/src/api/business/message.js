import request from '@/utils/request'

// 查询消息通知（微信订阅消息发送记录）列表
export function listMessage(query) {
  return request({
    url: '/business/message/list',
    method: 'get',
    params: query
  })
}
