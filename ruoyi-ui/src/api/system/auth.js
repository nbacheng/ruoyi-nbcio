import request from '@/utils/request'

// 绑定账号
export function authBinding(source) {
  return request({
    url: '/system/auth/binding/' + source,
    method: 'get'
  })
}

// 解绑账号
export function authUnlock(authId) {
  return request({
    url: '/system/auth/unlock/' + authId,
    method: 'delete'
  })
}
