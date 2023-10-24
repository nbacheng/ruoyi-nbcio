import request from '@/utils/request'

// 查询流程业务扩展列表
export function listMyBusiness(query) {
  return request({
    url: '/workflow/myBusiness/list',
    method: 'get',
    params: query
  })
}

// 查询流程业务扩展详细
export function getMyBusiness(id) {
  return request({
    url: '/workflow/myBusiness/' + id,
    method: 'get'
  })
}

// 新增流程业务扩展
export function addMyBusiness(data) {
  return request({
    url: '/workflow/myBusiness',
    method: 'post',
    data: data
  })
}

// 修改流程业务扩展
export function updateMyBusiness(data) {
  return request({
    url: '/workflow/myBusiness',
    method: 'put',
    data: data
  })
}

// 删除流程业务扩展
export function delMyBusiness(id) {
  return request({
    url: '/workflow/myBusiness/' + id,
    method: 'delete'
  })
}
