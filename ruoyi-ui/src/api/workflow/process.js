import request from '@/utils/request'

// 查询流程列表
export function listProcess(query) {
  return request({
    url: '/workflow/process/list',
    method: 'get',
    params: query
  })
}

// 查询流程列表
export function getProcessForm(query) {
  return request({
    url: '/workflow/process/getProcessForm',
    method: 'get',
    params: query
  })
}

// 部署流程实例
export function startProcess(processDefId, data) {
  return request({
    url: '/workflow/process/start/' + processDefId,
    method: 'post',
    data: data
  })
}

// 部署自定义业务流程实例
export function startByDataId(dataId,serviceName, data) {
  return request({
    url: '/workflow/process/startByDataId/' + dataId + '/' + serviceName,
    method: 'post',
    data: data
  })
}

// 查询当前节点是否是属于退回或驳回的第一个发起人节点,业务数据dataid

export function isFirstInitiator(dataId,data) {
  return request({
    url: '/workflow/task/isFirstInitiator/' + dataId,
    method: 'post',
    data: data
  })
}

// 删除自定义业务任务关联表与流程历史表
//以便可以重新发起流程,业务数据dataid

export function deleteActivityAndJoin(dataId,data) {
  return request({
    url: '/workflow/task/deleteActivityAndJoin/' + dataId,
    method: 'post',
    data: data
  })
}

// 删除流程实例
export function delProcess(ids) {
  return request({
    url: '/workflow/process/instance/' + ids,
    method: 'delete'
  })
}

// 获取流程图
export function getBpmnXml(processDefId) {
  return request({
    url: '/workflow/process/bpmnXml/' + processDefId,
    method: 'get'
  })
}

export function detailProcess(query) {
  return request({
    url: '/workflow/process/detail',
    method: 'get',
    params: query
  })
}

export function detailProcessByDataId(query) {
  return request({
    url: '/workflow/process/detailbydataid',
    method: 'get',
    params: query
  })
}

//判断流程是否结束了,用在消息点击里判断
export function processIscompleted(query) {
  return request({
    url: '/workflow/process/iscompleted',
    method: 'get',
    params: query
  })
}

// 我的发起的流程
export function listOwnProcess(query) {
  return request({
    url: '/workflow/process/ownList',
    method: 'get',
    params: query
  })
}

// 我待办的流程
export function listTodoProcess(query) {
  return request({
    url: '/workflow/process/todoList',
    method: 'get',
    params: query
  })
}

// 我待签的流程
export function listClaimProcess(query) {
  return request({
    url: '/workflow/process/claimList',
    method: 'get',
    params: query
  })
}

// 我已办的流程
export function listFinishedProcess(query) {
  return request({
    url: '/workflow/process/finishedList',
    method: 'get',
    params: query
  })
}

// 查询流程抄送列表
export function listCopyProcess(query) {
  return request({
    url: '/workflow/process/copyList',
    method: 'get',
    params: query
  })
}

// 取消申请
export function stopProcess(data) {
  return request({
    url: '/workflow/task/stopProcess',
    method: 'post',
    data: data
  })
}
