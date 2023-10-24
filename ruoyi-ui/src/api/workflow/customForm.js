import request from '@/utils/request'

// 查询流程业务单列表
export function listCustomForm(query) {
  return request({
    url: '/workflow/customForm/list',
    method: 'get',
    params: query
  })
}

// 查询流程业务单详细
export function getCustomForm(id) {
  return request({
    url: '/workflow/customForm/' + id,
    method: 'get'
  })
}

// 新增流程业务单
export function addCustomForm(data) {
  return request({
    url: '/workflow/customForm',
    method: 'post',
    data: data
  })
}

// 修改流程业务单
export function updateCustomForm(data) {
  return request({
    url: '/workflow/customForm',
    method: 'put',
    data: data
  })
}

// 根据选择关联的流程定义，更新自定义流程表单列表
export function updateCustom(data) {
  return request({
    url: '/workflow/customForm/updateCustom',
    method: 'post',
    data: data
  })
}

// 删除流程业务单
export function delCustomForm(id) {
  return request({
    url: '/workflow/customForm/' + id,
    method: 'delete'
  })
}
