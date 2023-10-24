package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.service.CommonService;
import com.ruoyi.common.core.service.UserService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.SpringContextUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.redis.RedisUtils;
import com.ruoyi.flowable.common.constant.ProcessConstants;
import com.ruoyi.flowable.common.constant.TaskConstants;
import com.ruoyi.flowable.common.enums.FlowComment;
import com.ruoyi.flowable.common.enums.ProcessStatus;
import com.ruoyi.flowable.core.domain.ActStatus;
import com.ruoyi.flowable.core.domain.dto.FlowNextDto;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.flow.CustomProcessDiagramGenerator;
import com.ruoyi.flowable.flow.FindNextNodeUtil;
import com.ruoyi.flowable.flow.FlowableUtils;
import com.ruoyi.flowable.utils.ModelUtils;
import com.ruoyi.flowable.utils.TaskUtils;
import com.ruoyi.flowable.utils.flowExp;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.workflow.domain.WfMyBusiness;
import com.ruoyi.workflow.domain.bo.WfTaskBo;
import com.ruoyi.workflow.mapper.FlowTaskMapper;
import com.ruoyi.workflow.service.IWfCopyService;
import com.ruoyi.workflow.service.IWfTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author KonBAI
 * @createTime 2022/3/10 00:12
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class WfTaskServiceImpl extends FlowServiceFactory implements IWfTaskService {

    private final UserService userService;

    private final IWfCopyService copyService;
    
    private final CommonService commonService;
    
    private final ISysUserService sysUserService;
    
    private final WfMyBusinessServiceImpl wfMyBusinessService;
    
    private final FlowTaskMapper flowTaskMapper;

    /**
     * 完成任务
     *
     * @param taskBo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void complete(WfTaskBo taskBo) {
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new ServiceException("任务不存在");
        }
        // 获取 bpmn 模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            taskService.addComment(taskBo.getTaskId(), taskBo.getProcInsId(), FlowComment.DELEGATE.getType(), taskBo.getComment());
            taskService.resolveTask(taskBo.getTaskId());
        } else {
            taskService.addComment(taskBo.getTaskId(), taskBo.getProcInsId(), FlowComment.NORMAL.getType(), taskBo.getComment());
            taskService.setAssignee(taskBo.getTaskId(), TaskUtils.getUserName());
            if (ObjectUtil.isNotEmpty(taskBo.getVariables())) {
                // 获取模型信息
                String localScopeValue = ModelUtils.getUserTaskAttributeValue(bpmnModel, task.getTaskDefinitionKey(), ProcessConstants.PROCESS_FORM_LOCAL_SCOPE);
                boolean localScope = Convert.toBool(localScopeValue, false);
                taskService.complete(taskBo.getTaskId(), taskBo.getVariables(), localScope);
            } else {
                taskService.complete(taskBo.getTaskId());
            }
        }
        // 设置任务节点名称
        taskBo.setTaskName(task.getName());
        // 处理下一级审批人
        if (StringUtils.isNotBlank(taskBo.getNextUserIds())) {
            this.assignNextUsers(bpmnModel, taskBo.getProcInsId(), taskBo.getNextUserIds());
        }
        // 处理抄送用户
        if (!copyService.makeCopy(taskBo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }

    /**
     * 拒绝任务
     *
     * @param taskBo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void taskReject(WfTaskBo taskBo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (ObjectUtil.isNull(task)) {
            throw new RuntimeException("获取任务信息异常！");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        // 获取流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(taskBo.getProcInsId())
            .singleResult();
        if (processInstance == null) {
            throw new RuntimeException("流程实例不存在，请确认！");
        }
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(task.getProcessDefinitionId())
            .singleResult();

        // 添加审批意见
        taskService.addComment(taskBo.getTaskId(), taskBo.getProcInsId(), FlowComment.REJECT.getType(), taskBo.getComment());
        // 设置流程状态为已终结
        runtimeService.setVariable(processInstance.getId(), ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.TERMINATED.getStatus());
        // 获取所有节点信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        EndEvent endEvent = ModelUtils.getEndEvent(bpmnModel);
        // 终止流程
        List<Execution> executions = runtimeService.createExecutionQuery().parentId(task.getProcessInstanceId()).list();
        List<String> executionIds = executions.stream().map(Execution::getId).collect(Collectors.toList());
        runtimeService.createChangeActivityStateBuilder()
            .processInstanceId(task.getProcessInstanceId())
            .moveExecutionsToSingleActivityId(executionIds, endEvent.getId())
            .changeState();
        // 处理抄送用户
        if (!copyService.makeCopy(taskBo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }

    /**
     * 退回任务
     *
     * @param bo 请求实体参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void taskReturn(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        if (ObjectUtil.isNull(task)) {
            throw new RuntimeException("获取任务信息异常！");
        }
        if (task.isSuspended()) {
            throw new RuntimeException("任务处于挂起状态");
        }
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取流程模型信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        // 获取当前任务节点元素
        FlowElement source = ModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        // 获取跳转的节点元素
        FlowElement target = ModelUtils.getFlowElementById(bpmnModel, bo.getTargetKey());
        // 从当前节点向前扫描，判断当前节点与目标节点是否属于串行，若目标节点是在并行网关上或非同一路线上，不可跳转
        boolean isSequential = ModelUtils.isSequentialReachable(source, target, new HashSet<>());
        if (!isSequential) {
            throw new RuntimeException("当前节点相对于目标节点，不属于串行关系，无法回退");
        }

        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需退回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = FlowableUtils.iteratorFindChildUserTasks(target, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        List<String> currentTaskIds = new ArrayList<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // 设置回退意见
        for (String currentTaskId : currentTaskIds) {
            taskService.addComment(currentTaskId, task.getProcessInstanceId(), FlowComment.REBACK.getType(), bo.getComment());
        }

        try {
            // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetKey 跳转到的节点(1)
            runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdsToSingleActivityId(currentIds, bo.getTargetKey()).changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new RuntimeException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new RuntimeException("无法取消或开始活动");
        }
        // 设置任务节点名称
        bo.setTaskName(task.getName());
        // 处理抄送用户
        if (!copyService.makeCopy(bo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }


    /**
     * 获取所有可回退的节点
     *
     * @param bo
     * @return
     */
    @Override
    public List<FlowElement> findReturnTaskList(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 获取流程模型信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        // 查询历史节点实例
        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(task.getProcessInstanceId())
            .activityType(BpmnXMLConstants.ELEMENT_TASK_USER)
            .finished()
            .orderByHistoricActivityInstanceEndTime().asc()
            .list();
        List<String> activityIdList = activityInstanceList.stream()
            .map(HistoricActivityInstance::getActivityId)
            .filter(activityId -> !StringUtils.equals(activityId, task.getTaskDefinitionKey()))
            .distinct()
            .collect(Collectors.toList());
        // 获取当前任务节点元素
        FlowElement source = ModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        List<FlowElement> elementList = new ArrayList<>();
        for (String activityId : activityIdList) {
            FlowElement target = ModelUtils.getFlowElementById(bpmnModel, activityId);
            boolean isSequential = ModelUtils.isSequentialReachable(source, target, new HashSet<>());
            if (isSequential) {
                elementList.add(target);
            }
        }
        return elementList;
    }

    /**
     * 删除任务
     *
     * @param bo 请求实体参数
     */
    @Override
    public void deleteTask(WfTaskBo bo) {
        // todo 待确认删除任务是物理删除任务 还是逻辑删除，让这个任务直接通过？
        taskService.deleteTask(bo.getTaskId(), bo.getComment());
    }

    /**
     * 认领/签收任务
     *
     * @param taskBo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(WfTaskBo taskBo) {
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (Objects.isNull(task)) {
            throw new ServiceException("任务不存在");
        }
        taskService.claim(taskBo.getTaskId(), TaskUtils.getUserName());
    }

    /**
     * 取消认领/签收任务
     *
     * @param bo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unClaim(WfTaskBo bo) {
        taskService.unclaim(bo.getTaskId());
    }

    /**
     * 委派任务
     *
     * @param bo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            throw new ServiceException("获取任务失败！");
        }
        StringBuilder commentBuilder = new StringBuilder(LoginHelper.getNickName())
            .append("->");
        String nickName = userService.selectNickNameById(Long.parseLong(bo.getUserId()));
        if (StringUtils.isNotBlank(nickName)) {
            commentBuilder.append(nickName);
        } else {
            commentBuilder.append(bo.getUserId());
        }
        if (StringUtils.isNotBlank(bo.getComment())) {
            commentBuilder.append(": ").append(bo.getComment());
        }
        // 添加审批意见
        taskService.addComment(bo.getTaskId(), task.getProcessInstanceId(), FlowComment.DELEGATE.getType(), commentBuilder.toString());
        // 设置办理人为当前登录人
        taskService.setOwner(bo.getTaskId(), TaskUtils.getUserName());
        // 执行委派
        taskService.delegateTask(bo.getTaskId(), bo.getUserId());
        // 设置任务节点名称
        bo.setTaskName(task.getName());
        // 处理抄送用户
        if (!copyService.makeCopy(bo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }


    /**
     * 转办任务
     *
     * @param bo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(WfTaskBo bo) {
        // 当前任务 task
        Task task = taskService.createTaskQuery().taskId(bo.getTaskId()).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            throw new ServiceException("获取任务失败！");
        }
        StringBuilder commentBuilder = new StringBuilder(LoginHelper.getNickName())
            .append("->");
        String nickName = userService.selectNickNameById(Long.parseLong(bo.getUserId()));
        if (StringUtils.isNotBlank(nickName)) {
            commentBuilder.append(nickName);
        } else {
            commentBuilder.append(bo.getUserId());
        }
        if (StringUtils.isNotBlank(bo.getComment())) {
            commentBuilder.append(": ").append(bo.getComment());
        }
        // 添加审批意见
        taskService.addComment(bo.getTaskId(), task.getProcessInstanceId(), FlowComment.TRANSFER.getType(), commentBuilder.toString());
        // 设置拥有者为当前登录人
        taskService.setOwner(bo.getTaskId(), TaskUtils.getUserName());
        // 转办任务
        taskService.setAssignee(bo.getTaskId(), bo.getUserId());
        // 设置任务节点名称
        bo.setTaskName(task.getName());
        // 处理抄送用户
        if (!copyService.makeCopy(bo)) {
            throw new RuntimeException("抄送任务失败");
        }
    }

    /**
     * 取消申请
     *
     * @param bo
     * @return
     */
    @Override
    public void stopProcess(WfTaskBo bo) {
        List<Task> task = taskService.createTaskQuery().processInstanceId(bo.getProcInsId()).list();
        if (CollectionUtils.isEmpty(task)) {
            throw new RuntimeException("流程未启动或已执行完成，取消申请失败");
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(bo.getProcInsId()).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        if (Objects.nonNull(bpmnModel)) {
            Process process = bpmnModel.getMainProcess();
            List<EndEvent> endNodes = process.findFlowElementsOfType(EndEvent.class, false);
            if (CollectionUtils.isNotEmpty(endNodes)) {
                Authentication.setAuthenticatedUserId(TaskUtils.getUserName());
//                taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.STOP.getType(),
//                        StringUtils.isBlank(flowTaskVo.getComment()) ? "取消申请" : flowTaskVo.getComment());
                // 获取当前流程最后一个节点
                String endId = endNodes.get(0).getId();
                List<Execution> executions = runtimeService.createExecutionQuery()
                    .parentId(processInstance.getProcessInstanceId()).list();
                List<String> executionIds = new ArrayList<>();
                executions.forEach(execution -> executionIds.add(execution.getId()));
                // 变更流程为已结束状态
                runtimeService.createChangeActivityStateBuilder()
                    .moveExecutionsToSingleActivityId(executionIds, endId).changeState();
            }
        }
    }

    /**
     * 撤回流程
     *
     * @param taskBo 请求实体参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeProcess(WfTaskBo taskBo) {
        String procInsId = taskBo.getProcInsId();
        String taskId = taskBo.getTaskId();
        // 校验流程是否结束
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(procInsId)
            .active()
            .singleResult();
        if(ObjectUtil.isNull(processInstance)) {
            throw new RuntimeException("流程已结束或已挂起，无法执行撤回操作");
        }
        // 获取待撤回任务实例
        HistoricTaskInstance currTaskIns = historyService.createHistoricTaskInstanceQuery()
            .taskId(taskId)
            .taskAssignee(TaskUtils.getUserName())
            .singleResult();
        if (ObjectUtil.isNull(currTaskIns)) {
            throw new RuntimeException("当前任务不存在，无法执行撤回操作");
        }
        // 获取 bpmn 模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(currTaskIns.getProcessDefinitionId());
        UserTask currUserTask = ModelUtils.getUserTaskByKey(bpmnModel, currTaskIns.getTaskDefinitionKey());
        // 查找下一级用户任务列表
        List<UserTask> nextUserTaskList = ModelUtils.findNextUserTasks(currUserTask);
        List<String> nextUserTaskKeys = nextUserTaskList.stream().map(UserTask::getId).collect(Collectors.toList());

        // 获取当前节点之后已完成的流程历史节点
        List<HistoricTaskInstance> finishedTaskInsList = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(procInsId)
            .taskCreatedAfter(currTaskIns.getEndTime())
            .finished()
            .list();
        for (HistoricTaskInstance finishedTaskInstance : finishedTaskInsList) {
            // 检查已完成流程历史节点是否存在下一级中
            if (CollUtil.contains(nextUserTaskKeys, finishedTaskInstance.getTaskDefinitionKey())) {
                throw new RuntimeException("下一流程已处理，无法执行撤回操作");
            }
        }
        // 获取所有激活的任务节点，找到需要撤回的任务
        List<Task> activateTaskList = taskService.createTaskQuery().processInstanceId(procInsId).list();
        List<String> revokeExecutionIds = new ArrayList<>();
        for (Task task : activateTaskList) {
            // 检查激活的任务节点是否存在下一级中，如果存在，则加入到需要撤回的节点
            if (CollUtil.contains(nextUserTaskKeys, task.getTaskDefinitionKey())) {
                // 添加撤回审批信息
                taskService.setAssignee(task.getId(), TaskUtils.getUserName());
                taskService.addComment(task.getId(), task.getProcessInstanceId(), FlowComment.REVOKE.getType(), LoginHelper.getNickName() + "撤回流程审批");
                revokeExecutionIds.add(task.getExecutionId());
            }
        }
        try {
            runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(procInsId)
                .moveExecutionsToSingleActivityId(revokeExecutionIds, currTaskIns.getTaskDefinitionKey()).changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new RuntimeException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new RuntimeException("执行撤回操作失败");
        }
    }

    /**
     * 获取流程过程图
     *
     * @param processId
     * @return
     */
    @Override
    public InputStream diagram(String processId) {
        String processDefinitionId;
        // 获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        // 如果流程已经结束，则得到结束节点
        if (Objects.isNull(processInstance)) {
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }

        // 获得活动的节点
        List<HistoricActivityInstance> highLightedFlowList = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> highLightedFlows = new ArrayList<>();
        List<String> highLightedNodes = new ArrayList<>();
        //高亮线
        for (HistoricActivityInstance tempActivity : highLightedFlowList) {
            if ("sequenceFlow".equals(tempActivity.getActivityType())) {
                //高亮线
                highLightedFlows.add(tempActivity.getActivityId());
            } else {
                //高亮节点
                highLightedNodes.add(tempActivity.getActivityId());
            }
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
        //获取自定义图片生成器
        ProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
        return diagramGenerator.generateDiagram(bpmnModel, "png", highLightedNodes, highLightedFlows, configuration.getActivityFontName(),
            configuration.getLabelFontName(), configuration.getAnnotationFontName(), configuration.getClassLoader(), 1.0, true);

    }

    /**
     * 获取流程变量
     *
     * @param taskId 任务ID
     * @return 流程变量
     */
    @Override
    public Map<String, Object> getProcessVariables(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
            .includeProcessVariables()
            .finished()
            .taskId(taskId)
            .singleResult();
        if (Objects.nonNull(historicTaskInstance)) {
            return historicTaskInstance.getProcessVariables();
        }
        return taskService.getVariables(taskId);
    }

    /**
     * 启动第一个任务
     * @param processInstance 流程实例
     * @param variables 流程参数
     */
    @Override
    public void startFirstTask(ProcessInstance processInstance, Map<String, Object> variables) {
        // 若第一个用户任务为发起人，则自动完成任务
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
        if (CollUtil.isNotEmpty(tasks)) {
            String userIdStr = (String) variables.get(TaskConstants.PROCESS_INITIATOR);
            for (Task task : tasks) {
                if (StrUtil.equals(task.getAssignee(), userIdStr)) {
                    taskService.addComment(task.getId(), processInstance.getProcessInstanceId(), FlowComment.NORMAL.getType(), LoginHelper.getNickName() + "发起流程申请");
                    taskService.complete(task.getId(), variables);
                }
            }
        }
    }

    /**
     * 指派下一任务审批人
     * @param bpmnModel bpmn模型
     * @param processInsId 流程实例id
     * @param userIds 用户ids
     */
    private void assignNextUsers(BpmnModel bpmnModel, String processInsId, String userIds) {
        // 获取所有节点信息
        List<Task> list = taskService.createTaskQuery()
            .processInstanceId(processInsId)
            .list();
        if (list.size() == 0) {
            return;
        }
        Queue<String> assignIds = CollUtil.newLinkedList(userIds.split(","));
        if (list.size() == assignIds.size()) {
            for (Task task : list) {
                taskService.setAssignee(task.getId(), assignIds.poll());
            }
            return;
        }
        // 优先处理非多实例任务
        Iterator<Task> iterator = list.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (!ModelUtils.isMultiInstance(bpmnModel, task.getTaskDefinitionKey())) {
                if (!assignIds.isEmpty()) {
                    taskService.setAssignee(task.getId(), assignIds.poll());
                }
                iterator.remove();
            }
        }
        // 若存在多实例任务，则进行动态加减签
        if (CollUtil.isNotEmpty(list)) {
            if (assignIds.isEmpty()) {
                // 动态减签
                for (Task task : list) {
                    runtimeService.deleteMultiInstanceExecution(task.getExecutionId(), true);
                }
            } else {
                // 动态加签
                for (String assignId : assignIds) {
                    Map<String, Object> assignVariables = Collections.singletonMap(BpmnXMLConstants.ATTRIBUTE_TASK_USER_ASSIGNEE, assignId);
                    runtimeService.addMultiInstanceExecution(list.get(0).getTaskDefinitionKey(), list.get(0).getProcessInstanceId(), assignVariables);
                }
            }
        }
    }
    
    /**
     * 获取下一节点
     *
     * @param flowTaskVo 任务
     * @return
     */
    @Override
    public R getNextFlowNode(WfTaskBo flowTaskVo) {
        // todo 目前只支持部分功能
        FlowNextDto flowNextDto = this.getNextFlowNode(flowTaskVo.getTaskId(), flowTaskVo.getVariables());
        if (flowNextDto==null) {
            return R.ok(null);
        }
        return R.ok(flowNextDto);

    }
    
    /**  modify by nbacheng
     * 获取下一个节点信息,流程定义上的节点信息
     * @param taskId 当前节点id
     * @param values 流程变量
     * @return 如果返回null，表示没有下一个节点，流程结束
     */

    public FlowNextDto getNextFlowNode(String taskId, Map<String, Object> values) {
    	//当前节点
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        FlowNextDto flowNextDto = new FlowNextDto();

    	if (Objects.nonNull(task)) {
        	// 下个任务节点
    		if (DelegationState.PENDING.equals(task.getDelegationState())) { //对于委派的处理
	        	List<UserTask> nextUserTask = FindNextNodeUtil.getNextUserTasks(repositoryService, task, values);
	            if (CollectionUtils.isNotEmpty(nextUserTask)) {
	            	flowNextDto.setType(ProcessConstants.FIXED);//委派是按原来流程执行，所以直接赋值返回
	            	return flowNextDto;
	            }
	            else {
	            	return null;
	            }

             }
            List<UserTask> nextUserTask = FindNextNodeUtil.getNextUserTasks(repositoryService, task, values);
            List<SysUser> list = new ArrayList<SysUser>();
            if (CollectionUtils.isNotEmpty(nextUserTask)) {
                for (UserTask userTask : nextUserTask) {
                    MultiInstanceLoopCharacteristics multiInstance = userTask.getLoopCharacteristics();
                    // 会签节点
                    if (Objects.nonNull(multiInstance)) {
                    	List<String> rolelist = new ArrayList<>();
                        rolelist = userTask.getCandidateGroups();
                    	List<String> userlist = new ArrayList<>();
                        userlist = userTask.getCandidateUsers();
                        UserTask newUserTask = userTask;
                        if(rolelist.size() != 0 && StringUtils.contains(rolelist.get(0), "${flowExp.getDynamic")) {//对表达式多个动态角色做特殊处理
                        	String methodname = StringUtils.substringBetween(rolelist.get(0), ".", "(");
                        	Object[] argsPara=new Object[]{};
                        	setMultiFlowExp(flowNextDto,newUserTask,multiInstance,methodname,argsPara);
                        }
                        else if(userlist.size() != 0 && StringUtils.contains(userlist.get(0), "${flowExp.getDynamic")) {//对表达式多个动态用户做特殊处理
                        	String methodname = StringUtils.substringBetween(userlist.get(0), ".", "(");
                        	Object[] argsPara=new Object[]{};
                        	setMultiFlowExp(flowNextDto,newUserTask,multiInstance,methodname,argsPara);
                        }
                        else if(userlist.size() != 0 && StringUtils.contains(userlist.get(0), "DepManagerHandler")) {//对部门经理做特殊处理
                        	String methodname = "getInitiatorDepManagers";
                        	// 获取流程发起人
	                   		ProcessInstance processInstance = runtimeService
	                                   .createProcessInstanceQuery()
	                                   .processInstanceId(task.getProcessInstanceId())
	                                   .singleResult();
	                        String startUserId = processInstance.getStartUserId();
	                        Object[] argsPara=new Object[]{};
	                        argsPara=new Object[]{startUserId};
                        	setMultiFlowExp(flowNextDto,newUserTask,multiInstance,methodname,argsPara);
                        }
                        else if(rolelist.size() > 0) {
							for(String roleId : rolelist ){
                        	  List<SysUser> templist = commonService.getUserListByRoleId(roleId);
                        	  for(SysUser sysuser : templist) {
                          		SysUser sysUserTemp = sysUserService.selectUserById(sysuser.getUserId());
                          		list.add(sysUserTemp);
                          	  }
                        	}
							setMultiFlowNetDto(flowNextDto,list,userTask,multiInstance);
                        }
                        else if(userlist.size() > 0) {
                        	for(String username : userlist) {
                        		SysUser sysUser =  sysUserService.selectUserByUserName(username);
                        		list.add(sysUser);
                        	}
                        	setMultiFlowNetDto(flowNextDto,list,userTask,multiInstance);
                        }
                        else {
                        	flowNextDto.setType(ProcessConstants.FIXED);
                        }
                  
                    } else {

                        // 读取自定义节点属性 判断是否是否需要动态指定任务接收人员、组,目前只支持用户角色或多用户，还不支持子流程和变量
                        //String dataType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_DATA_TYPE);
                        //String userType = userTask.getAttributeValue(ProcessConstants.NAMASPASE, ProcessConstants.PROCESS_CUSTOM_USER_TYPE);

                        List<String> rolelist = new ArrayList<>();
                        rolelist = userTask.getCandidateGroups();
                        List<String> userlist = new ArrayList<>();
                        userlist = userTask.getCandidateUsers();
                        String assignee = userTask.getAssignee();
                        // 处理加载动态指定下一节点接收人员信息
                        if(assignee !=null) {
                        	if(StringUtils.equalsAnyIgnoreCase(assignee, "${initiator}")) {//对发起人做特殊处理
                        		SysUser sysUser = new SysUser();
                        		sysUser.setUserName("${initiator}");
                        		list.add(sysUser);
                        		setAssigneeFlowNetDto(flowNextDto,list,userTask);
                        	} else if(StringUtils.equalsAnyIgnoreCase(assignee, "${DepManagerHandler.getUser(execution)}")) {//对部门经理做特殊处理
                        		SysUser sysUser = new SysUser();
                        		sysUser.setUserName("${DepManagerHandler.getUser(execution)}");
                        		list.add(sysUser);
                        		setAssigneeFlowNetDto(flowNextDto,list,userTask);
                        	}
                        	else if(StringUtils.contains(assignee, "${flowExp.getDynamicAssignee")) {//对表达式单个动态用户做特殊处理
                        		String methodname = StringUtils.substringBetween(assignee, ".", "(");
                        		SysUser sysUser = new SysUser();
                        		flowExp flowexp = SpringContextUtils.getBean(flowExp.class);
                        		Object[] argsPara=new Object[]{};
                        		String username = null;
                        		try {
									username = (String) flowexp.invokeMethod(flowexp, methodname,argsPara);
								} catch (Exception e) {
									e.printStackTrace();
								}
                        		sysUser.setUserName(username);
                        		list.add(sysUser);
                        		setAssigneeFlowNetDto(flowNextDto,list,userTask);
                        	}
                        	else if(StringUtils.contains(assignee, "${flowExp.getDynamicList")) {//对表达式多个动态用户做特殊处理
                        		String methodname = StringUtils.substringBetween(assignee, ".", "(");
                        		flowExp flowexp = SpringContextUtils.getBean(flowExp.class);
                        		Object[] argsPara=new Object[]{};
                        		try {
                        			list = (List<SysUser>) flowexp.invokeMethod(flowexp, methodname,argsPara);
								} catch (Exception e) {
									e.printStackTrace();
								}
                        		setUsersFlowNetDto(flowNextDto,list,userTask);
                        	   
                        	}
                        	/*else if(StringUtils.contains(assignee, "${DepManagerHandler")) {//对部门经理多用户做特殊处理
                        		String methodname = "getInitiatorDepManagers";
                        		// 获取流程发起人
    	                   		ProcessInstance processInstance = runtimeService
    	                                   .createProcessInstanceQuery()
    	                                   .processInstanceId(task.getProcessInstanceId())
    	                                   .singleResult();
    	                        String startUserId = processInstance.getStartUserId();
                        		flowExp flowexp = SpringContextUtils.getBean(flowExp.class);
                        		Object[] argsPara=new Object[]{};
                        		argsPara[0] = startUserId;
                        		try {
                        			list = (List<SysUser>) flowexp.invokeMethod(flowexp, methodname,argsPara);
								} catch (Exception e) {
									e.printStackTrace();
								}
                        		setUsersFlowNetDto(flowNextDto,list,userTask);
                        	   
                        	}*/
                        	else {
                        	    SysUser sysUser =  sysUserService.selectUserByUserName(assignee);
                    		    
                    		    list.add(sysUser);
                    		    setAssigneeFlowNetDto(flowNextDto,list,userTask);
                        	}
                        	
                        }
                        else if(userlist.size()>0 && StringUtils.equalsAnyIgnoreCase(userlist.get(0), "${DepManagerHandler.getUsers(execution)}")) {//对部门经理做特殊处理
	                   		// 获取流程发起人
	                   		ProcessInstance processInstance = runtimeService
	                                   .createProcessInstanceQuery()
	                                   .processInstanceId(task.getProcessInstanceId())
	                                   .singleResult();
	                           String startUserId = processInstance.getStartUserId();
	                        flowExp flowexp = SpringContextUtils.getBean(flowExp.class);
	                   		String manager =  flowexp.getDynamicManager(startUserId);
	                   		SysUser sysUser =  sysUserService.selectUserByUserName(manager);
	                   		list.add(sysUser);
	                   		setUsersFlowNetDto(flowNextDto,list,userTask);
                        }
                        else if(userlist.size() > 0) {
                        	for(String username : userlist) {
                        		SysUser sysUser =  sysUserService.selectUserByUserName(username);
                        		
                        		list.add(sysUser);
                        	}
                        	setUsersFlowNetDto(flowNextDto,list,userTask);
                        	setMultiFinishFlag(task,flowNextDto,list);
                        	
                        }
                        else if(rolelist.size() > 0) {
							for(String roleId : rolelist ){
                        	  List<SysUser> templist =  commonService.getUserListByRoleId(roleId);
                        	  for(SysUser sysuser : templist) {
                          		SysUser sysUserTemp = sysUserService.selectUserByUserName(sysuser.getUserName());
                          		list.add(sysUserTemp);
                          	  }
                        	}
							setUsersFlowNetDto(flowNextDto,list,userTask);
							setMultiFinishFlag(task,flowNextDto,list);
                        }
                        else {
                        	flowNextDto.setType(ProcessConstants.FIXED);
                        }
                    }
                }
                return flowNextDto;
            } else {
                return null;
          }
       }
       return null;

    }
    
    //设置单用户下一节点流程数据
    private void setAssigneeFlowNetDto(FlowNextDto flowNextDto,List<SysUser> list,UserTask userTask) {
    	flowNextDto.setVars(ProcessConstants.PROCESS_APPROVAL);
	    flowNextDto.setType(ProcessConstants.USER_TYPE_ASSIGNEE);
	    flowNextDto.setUserList(list);
	    flowNextDto.setUserTask(userTask);
    }
    
    //设置多用户下一节点流程数据
    private void setUsersFlowNetDto(FlowNextDto flowNextDto,List<SysUser> list,UserTask userTask) {
    	flowNextDto.setVars(ProcessConstants.PROCESS_APPROVAL);
        flowNextDto.setType(ProcessConstants.USER_TYPE_USERS);
        flowNextDto.setUserList(list);
        flowNextDto.setUserTask(userTask);
    }
    
    //设置多实例结束标志
    private void setMultiFinishFlag(Task task,FlowNextDto flowNextDto,List<SysUser> list) {
    	String definitionld = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult().getProcessDefinitionId();        //获取bpm（模型）对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionld);
        //通过节点定义key获取当前节点
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        if(flowNode instanceof UserTask ){
        	UserTask curuserTask = (UserTask) flowNode;
        	MultiInstanceLoopCharacteristics curmultiInstance = curuserTask.getLoopCharacteristics();
        	if (Objects.nonNull(curmultiInstance)) {
        		if(list.size()>1) {//多人选择的时候,从redis获取之前监听器写入的会签结束信息
        		   String smutinstance_next_finish = Constants.MUTIINSTANCE_NEXT_FINISH + task.getProcessInstanceId(); 	
        	       if(Objects.nonNull(RedisUtils.getCacheObject(smutinstance_next_finish))) {
        		      flowNextDto.setBmutiInstanceFinish(true);
        	       }
                }
        	}
        }
    }
    
    //设置多实例流程表达式
    private void setMultiFlowExp(FlowNextDto flowNextDto,UserTask newUserTask,MultiInstanceLoopCharacteristics multiInstance,String methodname,Object[] argsPara) {
    	List<SysUser> list = new ArrayList<SysUser>();
		flowExp flowexp = SpringContextUtils.getBean(flowExp.class);
		//Object[] argsPara=new Object[]{};
		List<String> templist = new ArrayList<String>();
		try {
			templist = (List<String>) flowexp.invokeMethod(flowexp, methodname,argsPara);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String sysuser : templist) {
      		SysUser sysUserTemp = sysUserService.selectUserByUserName(sysuser);
      		list.add(sysUserTemp);
      	}
		newUserTask.setAssignee("${assignee}");
		newUserTask.setCandidateUsers(templist);
		setMultiFlowNetDto(flowNextDto,list,newUserTask,multiInstance);
    }
    
    //设置多实例流程数据
    private void setMultiFlowNetDto(FlowNextDto flowNextDto,List<SysUser> list,UserTask userTask,MultiInstanceLoopCharacteristics multiInstance) {
    	flowNextDto.setVars(ProcessConstants.PROCESS_MULTI_INSTANCE_USER);
        flowNextDto.setType(ProcessConstants.PROCESS_MULTI_INSTANCE);
        flowNextDto.setUserList(list);
        flowNextDto.setUserTask(userTask);
        if(multiInstance.isSequential()) {
        	flowNextDto.setBisSequential(true);
        }
        else {
        	flowNextDto.setBisSequential(false);
        }
    }

	@Override
	public boolean isFirstInitiator(String processInstanceId, String actStatusType) {
		if(StringUtils.equalsAnyIgnoreCase(actStatusType, ActStatus.reject) ||
 	    	   StringUtils.equalsAnyIgnoreCase(actStatusType, ActStatus.recall) ||
 	    	   StringUtils.equalsAnyIgnoreCase(actStatusType, ActStatus.retrun) ) {
 		if(StringUtils.isNotEmpty(processInstanceId)) {
 		    //  获取当前任务
             Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
	    		BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
		        //  获取当前活动节点
		        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
		        // 输入连线
		        List<SequenceFlow> inFlows = currentFlowNode.getIncomingFlows();
		        for (SequenceFlow sequenceFlow : inFlows) {
		        	FlowElement sourceFlowElement = sequenceFlow.getSourceFlowElement();
		        	// 如果上个节点为开始节点
		            if (sourceFlowElement instanceof StartEvent) {
		            	log.info("当前节点为发起人节点,上个节点为开始节点：id=" + sourceFlowElement.getId() + ",name=" + sourceFlowElement.getName());
		                return true;
		            }
		        }
 		}
 	}
		return false;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteActivityAndJoin(String dataId, String processInstanceId, String actStatusType) {
		if (dataId==null) return false;
        WfMyBusiness business = wfMyBusinessService.getByDataId(dataId);
        if(StringUtils.equalsAnyIgnoreCase(actStatusType, ActStatus.reject) ||
 	    	   StringUtils.equalsAnyIgnoreCase(actStatusType, ActStatus.recall) ||
 	    	   StringUtils.equalsAnyIgnoreCase(actStatusType, ActStatus.retrun) ) {
            //  重新查询当前任务
            Task currentTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
            //删除自定义业务任务关联表与流程历史表，以便可以重新发起流程。
            if (business != null) {
            	wfMyBusinessService.removeById(business);
            	// 对自定义业务，删除运行和历史的节点信息 
                this.deleteActivity(currentTask.getTaskDefinitionKey(), currentTask.getProcessInstanceId(), dataId);
                return true;
            }
        }
		return false;
	}
	
	/**
     * 删除跳转的历史节点信息
     *
     * @param disActivityId     跳转的节点id
     * @param processInstanceId 流程实例id
     * @param dataId   自定义业务id
     */
    protected void deleteActivity(String disActivityId, String processInstanceId, String dataId) {
        List<ActivityInstance> disActivities = flowTaskMapper
                .queryActivityInstance(disActivityId, processInstanceId, null);

        //删除运行时和历史节点信息
        if (CollectionUtils.isNotEmpty(disActivities)) {
            ActivityInstance activityInstance = disActivities.get(0);
            List<ActivityInstance> datas = flowTaskMapper
                    .queryActivityInstance(disActivityId, processInstanceId, activityInstance.getEndTime());

            //datas.remove(0); //保留流程发起节点信息
            List<String> runActivityIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(datas)) {
                datas.forEach(ai -> runActivityIds.add(ai.getId()));
                flowTaskMapper.deleteRunActinstsByIds(runActivityIds);
                flowTaskMapper.deleteHisActinstsByIds(runActivityIds);
            }
            if(dataId != null) {//对于自定义业务, 删除所有相关流程信息
            	//flowTaskMapper.deleteAllHisAndRun(processInstanceId);
                //根据流程实例id 删除去ACT_RU_*与ACT_HI_*流程实例数据
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                if (null != processInstance) {
                    runtimeService.deleteProcessInstance(processInstanceId, "流程实例删除");
                    historyService.deleteHistoricProcessInstance(processInstanceId);
                }
            }
        }
    }
}
