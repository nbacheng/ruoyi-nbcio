import request from '@/utils/request'

// 查询公告列表
export function listNotice(query) {
  return request({
    url: '/system/notice/list',
    method: 'get',
    params: query
  })
}

// 获取系统消息
export function listByUser(query) {
  return request({
    url: '/system/notice/listByUser',
    method: 'get',
    params: query
  })
}

// 获取我的系统消息
export function getMyNoticeSend(data) {
  return request({
    url: '/system/noticeSend/getMyNoticeSend',
    method: 'post',
    data: data
  })
}


// 查询公告详细
export function getNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'get'
  })
}

// 新增公告
export function addNotice(data) {
  return request({
    url: '/system/notice',
    method: 'post',
    data: data
  })
}

// 修改公告
export function updateNotice(data) {
  return request({
    url: '/system/notice',
    method: 'put',
    data: data
  })
}

// 更新用户阅读公告状态
export function updateUserIdAndNotice(data) {
  return request({
    url: '/system/noticeSend/updateUserIdAndNotice',
    method: 'put',
    data: data
  })
}

// 删除公告
export function delNotice(noticeId) {
  return request({
    url: '/system/notice/' + noticeId,
    method: 'delete'
  })
}
