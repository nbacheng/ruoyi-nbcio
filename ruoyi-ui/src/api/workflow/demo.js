import request from '@/utils/request'

// 查询DEMO列表
export function listDemo(query) {
  return request({
    url: '/workflow/demo/list',
    method: 'get',
    params: query
  })
}

// 查询DEMO详细
export function getDemo(demoId) {
  return request({
    url: '/workflow/demo/' + demoId,
    method: 'get'
  })
}

// 新增DEMO
export function addDemo(data) {
  return request({
    url: '/workflow/demo',
    method: 'post',
    data: data
  })
}

// 修改DEMO
export function updateDemo(data) {
  return request({
    url: '/workflow/demo',
    method: 'put',
    data: data
  })
}

// 删除DEMO
export function delDemo(demoId) {
  return request({
    url: '/workflow/demo/' + demoId,
    method: 'delete'
  })
}
