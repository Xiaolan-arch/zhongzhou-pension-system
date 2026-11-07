import request from '@/utils/request'

// 查询押金记录列表
export function listDeposit(query) {
  return request({
    url: '/business/deposit/list',
    method: 'get',
    params: query
  })
}

// 查询老人押金可退余额
export function getDepositBalance(elderId) {
  return request({
    url: '/business/deposit/balance/' + elderId,
    method: 'get'
  })
}

// 收押金
export function collectDeposit(data) {
  return request({
    url: '/business/deposit/add',
    method: 'post',
    data: data
  })
}

// 退押金
export function refundDeposit(data) {
  return request({
    url: '/business/deposit/refund',
    method: 'post',
    data: data
  })
}
