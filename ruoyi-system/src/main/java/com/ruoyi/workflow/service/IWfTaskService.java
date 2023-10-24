package com.ruoyi.workflow.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.flowable.core.domain.dto.FlowNextDto;
import com.ruoyi.workflow.domain.bo.WfTaskBo;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.runtime.ProcessInstance;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author KonBAI
 * @createTime 2022/3/10 00:12
 */
public interface IWfTaskService {

    /**
     * 审批任务
     *
     * @param task 请求实体参数
     */
    void complete(WfTaskBo task);

    /**
     * 拒绝任务
     *
     * @param taskBo
     */
    void taskReject(WfTaskBo taskBo);


    /**
     * 退回任务
     *
     * @param bo 请求实体参数
     */
    void taskReturn(WfTaskBo bo);

    /**
     * 获取所有可回退的节点
     *
     * @param bo
     * @return
     */
    List<FlowElement> findReturnTaskList(WfTaskBo bo);

    /**
     * 删除任务
     *
     * @param bo 请求实体参数
     */
    void deleteTask(WfTaskBo bo);

    /**
     * 认领/签收任务
     *
     * @param bo 请求实体参数
     */
    void claim(WfTaskBo bo);

    /**
     * 取消认领/签收任务
     *
     * @param bo 请求实体参数
     */
    void unClaim(WfTaskBo bo);

    /**
     * 委派任务
     *
     * @param bo 请求实体参数
     */
    void delegateTask(WfTaskBo bo);


    /**
     * 转办任务
     *
     * @param bo 请求实体参数
     */
    void transferTask(WfTaskBo bo);

    /**
     * 取消申请
     * @param bo
     * @return
     */
    void stopProcess(WfTaskBo bo);

    /**
     * 撤回流程
     * @param bo
     * @return
     */
    void revokeProcess(WfTaskBo bo);

    /**
     * 获取流程过程图
     * @param processId
     * @return
     */
    InputStream diagram(String processId);

    /**
     * 获取流程变量
     * @param taskId 任务ID
     * @return 流程变量
     */
    Map<String, Object> getProcessVariables(String taskId);

    /**
     * 启动第一个任务
     * @param processInstance 流程实例
     * @param variables 流程参数
     */
    void startFirstTask(ProcessInstance processInstance, Map<String, Object> variables);
    
    /**
     * 获取下一节点
     * @param WfTaskBo 任务
     * @return
     */
    public R getNextFlowNode(WfTaskBo flowTaskVo);
    /**
     * 获取下一节点
     * @param String taskId, Map<String, Object> values
     * @return
     */
    public FlowNextDto getNextFlowNode(String taskId, Map<String, Object> values);

    /**
     * 自定义业务使用
     * 判断是否是第一个发起人节点，目前只针对退回，驳回情况进行处理
     * @param dataId 流程业务数据id, variables 变量集合,json对象
     * @return
     */
	boolean isFirstInitiator(String processInstanceId, String actStatusType);

	/**
     * 自定义业务使用
     *  删除自定义业务任务关联表与流程历史表，以便可以重新发起流程。
     * @param dataId 流程业务数据id, variables 变量集合,json对象
     * @return
     */
	boolean deleteActivityAndJoin(String dataId, String processInstanceId, String actStatusType);
}
