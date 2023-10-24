package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.service.CommonService;
import com.ruoyi.common.core.service.UserService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.JsonUtils;
import com.ruoyi.common.utils.SpringContextUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.common.constant.ProcessConstants;
import com.ruoyi.flowable.common.constant.TaskConstants;
import com.ruoyi.flowable.common.enums.FlowComment;
import com.ruoyi.flowable.common.enums.ProcessStatus;
import com.ruoyi.flowable.core.FormConf;
import com.ruoyi.flowable.core.domain.ActStatus;
import com.ruoyi.flowable.core.domain.ProcessQuery;
import com.ruoyi.flowable.core.domain.dto.FlowNextDto;
import com.ruoyi.flowable.factory.FlowServiceFactory;
import com.ruoyi.flowable.flow.FindNextNodeUtil;
import com.ruoyi.flowable.flow.FlowableUtils;
import com.ruoyi.flowable.utils.ModelUtils;
import com.ruoyi.flowable.utils.ProcessFormUtils;
import com.ruoyi.flowable.utils.ProcessUtils;
import com.ruoyi.flowable.utils.TaskUtils;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.workflow.domain.WfCustomForm;
import com.ruoyi.workflow.domain.WfDeployForm;
import com.ruoyi.workflow.domain.WfMyBusiness;
import com.ruoyi.workflow.domain.vo.*;
import com.ruoyi.workflow.mapper.WfCategoryMapper;
import com.ruoyi.workflow.mapper.WfDeployFormMapper;
import com.ruoyi.workflow.service.IWfCustomFormService;
import com.ruoyi.workflow.service.IWfMyBusinessService;
import com.ruoyi.workflow.service.IWfProcessService;
import com.ruoyi.workflow.service.IWfTaskService;
import com.ruoyi.workflow.service.WfCommonService;
import com.ruoyi.workflow.service.WfCallBackServiceI;
import com.ruoyi.common.core.domain.model.LoginUser;

import lombok.RequiredArgsConstructor;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.ObjectUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * @author nbacheng
 * @createTime 2023-09-25
 */
@RequiredArgsConstructor
@Service
public class WfProcessServiceImpl extends FlowServiceFactory implements IWfProcessService {

    private final IWfTaskService wfTaskService;
    private final UserService userService;
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;
    private final WfDeployFormMapper deployFormMapper;
    private final CommonService commonService;
    private final ISysUserService sysUserService;
    private final IWfCustomFormService wfCustomFormService;
    private final IWfMyBusinessService wfMyBusinessService;
    private final WfCommonService wfCommonService;
    private final WfCategoryMapper categoryMapper;
    private final WfMyBusinessServiceImpl wfMyBusinessServiceImpl;

    /**
     * 流程定义列表
     *
     * @param pageQuery 分页参数
     * @return 流程定义分页列表数据
     */
    @Override
    public TableDataInfo<WfDefinitionVo> selectPageStartProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfDefinitionVo> page = new Page<>();
        // 流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .active()
            .orderByProcessDefinitionKey()
            .asc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);
        long pageTotal = processDefinitionQuery.count();
        if (pageTotal <= 0) {
            return TableDataInfo.build();
        }
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<ProcessDefinition> definitionList = processDefinitionQuery.listPage(offset, pageQuery.getPageSize());

        List<WfDefinitionVo> definitionVoList = new ArrayList<>();
        for (ProcessDefinition processDefinition : definitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
           // 检查流程是否是OA流程
            WfAppTypeVo appTypeVo = categoryMapper.selectAppTypeVoByCode(deployment.getCategory());
            String appType = "";
            if(ObjectUtil.isNotEmpty(appTypeVo)) {
            	appType =  appTypeVo.getId();
            }
            if(StrUtil.equalsAnyIgnoreCase(appType, "OA")) {//OA流程取出，其它流程到其它相应地方发起流程
	            WfDefinitionVo vo = new WfDefinitionVo();
	            vo.setDefinitionId(processDefinition.getId());
	            vo.setProcessKey(processDefinition.getKey());
	            vo.setProcessName(processDefinition.getName());
	            vo.setVersion(processDefinition.getVersion());
	            vo.setDeploymentId(processDefinition.getDeploymentId());
	            vo.setSuspended(processDefinition.isSuspended());
	            // 流程定义时间
	            vo.setCategory(deployment.getCategory());
	            vo.setDeploymentTime(deployment.getDeploymentTime());
	            definitionVoList.add(vo);
            }
            
        }
        page.setRecords(definitionVoList);
        page.setTotal(pageTotal);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfDefinitionVo> selectStartProcessList(ProcessQuery processQuery) {
        // 流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .active()
                .orderByProcessDefinitionKey()
                .asc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);

        List<ProcessDefinition> definitionList = processDefinitionQuery.list();

        List<WfDefinitionVo> definitionVoList = new ArrayList<>();
        for (ProcessDefinition processDefinition : definitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            WfDefinitionVo vo = new WfDefinitionVo();
            vo.setDefinitionId(processDefinition.getId());
            vo.setProcessKey(processDefinition.getKey());
            vo.setProcessName(processDefinition.getName());
            vo.setVersion(processDefinition.getVersion());
            vo.setDeploymentId(processDefinition.getDeploymentId());
            vo.setSuspended(processDefinition.isSuspended());
            // 流程定义时间
            vo.setCategory(deployment.getCategory());
            vo.setDeploymentTime(deployment.getDeploymentTime());
            definitionVoList.add(vo);
        }
        return definitionVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageOwnProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
            .startedBy(TaskUtils.getUserName())
            .orderByProcessInstanceStartTime()
            .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(historicProcessInstanceQuery, processQuery);
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery
            .listPage(offset, pageQuery.getPageSize());
        page.setTotal(historicProcessInstanceQuery.count());
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            WfTaskVo taskVo = new WfTaskVo();
            // 获取流程状态
            HistoricVariableInstance processStatusVariable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(hisIns.getId())
                .variableName(ProcessConstants.PROCESS_STATUS_KEY)
                .singleResult();
            String processStatus = null;
            if (ObjectUtil.isNotNull(processStatusVariable)) {
                processStatus = Convert.toStr(processStatusVariable.getValue());
            }
            // 兼容旧流程
            if (processStatus == null) {
                processStatus = ObjectUtil.isNull(hisIns.getEndTime()) ? ProcessStatus.RUNNING.getStatus() : ProcessStatus.COMPLETED.getStatus();
            }
            taskVo.setProcessStatus(processStatus);
            taskVo.setCreateTime(hisIns.getStartTime());
            taskVo.setFinishTime(hisIns.getEndTime());
            taskVo.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                taskVo.setDuration(DateUtils.getDatePoor(hisIns.getEndTime(), hisIns.getStartTime()));
            } else {
                taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), hisIns.getStartTime()));
            }
            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(hisIns.getDeploymentId()).singleResult();
            taskVo.setDeployId(hisIns.getDeploymentId());
            taskVo.setProcDefId(hisIns.getProcessDefinitionId());
            taskVo.setProcDefName(hisIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(hisIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).includeIdentityLinks().list();
            // 任务列表
            if (CollUtil.isNotEmpty(taskList)) {
            	taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            	taskVo.setTaskId(taskList.get(0).getId());
            } else {
                List<HistoricTaskInstance> historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(hisIns.getId()).orderByHistoricTaskInstanceEndTime().desc().list();
                taskVo.setTaskId(historicTaskInstance.get(0).getId());
            }
            taskVoList.add(taskVo);
        }
        page.setRecords(taskVoList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectOwnProcessList(ProcessQuery processQuery) {
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(TaskUtils.getUserName())
                .orderByProcessInstanceStartTime()
                .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(historicProcessInstanceQuery, processQuery);
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery.list();
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            WfTaskVo taskVo = new WfTaskVo();
            taskVo.setCreateTime(hisIns.getStartTime());
            taskVo.setFinishTime(hisIns.getEndTime());
            taskVo.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                taskVo.setDuration(DateUtils.getDatePoor(hisIns.getEndTime(), hisIns.getStartTime()));
            } else {
                taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), hisIns.getStartTime()));
            }
            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(hisIns.getDeploymentId()).singleResult();
            taskVo.setDeployId(hisIns.getDeploymentId());
            taskVo.setProcDefId(hisIns.getProcessDefinitionId());
            taskVo.setProcDefName(hisIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(hisIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).includeIdentityLinks().list();
            if (CollUtil.isNotEmpty(taskList)) {
                taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            }
            taskVoList.add(taskVo);
        }
        return taskVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageTodoProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
    	Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
            .active()
            .includeProcessVariables()
            .taskCandidateOrAssigned(TaskUtils.getUserName())
            .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
            .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        page.setTotal(taskQuery.count());
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Task> taskList = taskQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
            String userId = historicProcessInstance.getStartUserId();
            String nickName = sysUserService.selectUserByUserName(userId).getNickName();
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            // 流程变量
            flowTask.setProcVars(task.getProcessVariables());

            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectTodoProcessList(ProcessQuery processQuery) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .taskCandidateOrAssigned(TaskUtils.getUserName())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> taskList = taskQuery.list();
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo taskVo = new WfTaskVo();
            // 当前流程信息
            taskVo.setTaskId(task.getId());
            taskVo.setTaskDefKey(task.getTaskDefinitionKey());
            taskVo.setCreateTime(task.getCreateTime());
            taskVo.setProcDefId(task.getProcessDefinitionId());
            taskVo.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            taskVo.setDeployId(pd.getDeploymentId());
            taskVo.setProcDefName(pd.getName());
            taskVo.setProcDefVersion(pd.getVersion());
            taskVo.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            String userId = historicProcessInstance.getStartUserId();
            String nickName = sysUserService.selectUserByUserName(userId).getNickName();
            taskVo.setStartUserId(userId);
            taskVo.setStartUserName(nickName);

            taskVoList.add(taskVo);
        }
        return taskVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageClaimProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
            .active()
            .includeProcessVariables()
            .taskCandidateUser(TaskUtils.getUserName())
            .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
            .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        page.setTotal(taskQuery.count());
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Task> taskList = taskQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
            String userId = historicProcessInstance.getStartUserId();
            String nickName = sysUserService.selectUserByUserName(userId).getNickName();
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectClaimProcessList(ProcessQuery processQuery) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .taskCandidateUser(TaskUtils.getUserName())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> taskList = taskQuery.list();
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            String userId = historicProcessInstance.getStartUserId();
            String nickName = sysUserService.selectUserByUserName(userId).getNickName();
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            flowList.add(flowTask);
        }
        return flowList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageFinishedProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
            .includeProcessVariables()
            .finished()
            .taskAssignee(TaskUtils.getUserName())
            .orderByHistoricTaskInstanceEndTime()
            .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskInstanceQuery, processQuery);
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(DateUtil.formatBetween(histTask.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(histTask.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(histTask.getProcessInstanceId())
                .singleResult();
            String userId = historicProcessInstance.getStartUserId();
            String nickName = sysUserService.selectUserByUserName(userId).getNickName();
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            // 流程变量
            flowTask.setProcVars(histTask.getProcessVariables());

            hisTaskList.add(flowTask);
        }
        page.setTotal(taskInstanceQuery.count());
        page.setRecords(hisTaskList);
//        Map<String, Object> result = new HashMap<>();
//        result.put("result",page);
//        result.put("finished",true);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectFinishedProcessList(ProcessQuery processQuery) {
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .finished()
                .taskAssignee(TaskUtils.getUserName())
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskInstanceQuery, processQuery);
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.list();
        List<WfTaskVo> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(DateUtil.formatBetween(histTask.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(histTask.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(histTask.getProcessInstanceId())
                    .singleResult();
            String userId = historicProcessInstance.getStartUserId();
            String nickName = sysUserService.selectUserByUserName(userId).getNickName();
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            // 流程变量
            flowTask.setProcVars(histTask.getProcessVariables());

            hisTaskList.add(flowTask);
        }
        return hisTaskList;
    }

    @Override
    public FormConf selectFormContent(String definitionId, String deployId, String procInsId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionId);
        if (ObjectUtil.isNull(bpmnModel)) {
            throw new RuntimeException("获取流程设计失败！");
        }
        StartEvent startEvent = ModelUtils.getStartEvent(bpmnModel);
        WfDeployForm deployForm = deployFormMapper.selectOne(new LambdaQueryWrapper<WfDeployForm>()
            .eq(WfDeployForm::getDeployId, deployId)
            .eq(WfDeployForm::getFormKey, startEvent.getFormKey())
            .eq(WfDeployForm::getNodeKey, startEvent.getId()));
        FormConf formConf = JsonUtils.parseObject(deployForm.getContent(), FormConf.class);
        if (ObjectUtil.isNull(formConf)) {
            throw new RuntimeException("获取流程表单失败！");
        }
        if (ObjectUtil.isNotEmpty(procInsId)) {
            // 获取流程实例
            HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .includeProcessVariables()
                .singleResult();
            // 填充表单信息
            ProcessFormUtils.fillFormData(formConf, historicProcIns.getProcessVariables());
        }
        return formConf;
    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId 流程定义Id
     * @param variables 流程变量
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> startProcessByDefId(String procDefId, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(procDefId).singleResult();
            return startProcess(processDefinition, variables);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("流程启动错误");
        }
    }

    /**
     * 通过DefinitionKey启动流程
     * @param procDefKey 流程定义Key
     * @param variables 扩展参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> startProcessByDefKey(String procDefKey, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(procDefKey).latestVersion().singleResult();
            return startProcess(processDefinition, variables);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("流程启动错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessByIds(String[] instanceIds) {
        List<String> ids = Arrays.asList(instanceIds);
        // 校验流程是否结束
        long activeInsCount = runtimeService.createProcessInstanceQuery()
            .processInstanceIds(new HashSet<>(ids)).active().count();
        if (activeInsCount > 0) {
            throw new ServiceException("不允许删除进行中的流程实例");
        }
        // 删除历史流程实例
        historyService.bulkDeleteHistoricProcessInstances(ids);
    }

    /**
     * 读取xml文件
     * @param processDefId 流程定义ID
     */
    @Override
    public String queryBpmnXmlById(String processDefId) {
        InputStream inputStream = repositoryService.getProcessModel(processDefId);
        try {
            return IoUtil.readUtf8(inputStream);
        } catch (IORuntimeException exception) {
            throw new RuntimeException("加载xml文件异常");
        }
    }

    /**
     * 流程详情信息
     *
     * @param procInsId 流程实例ID
     * @param taskId 任务ID
     * @return
     */
    @Override
    public WfDetailVo queryProcessDetail(String procInsId, String taskId, String dataId ) {
        WfDetailVo detailVo = new WfDetailVo();
        // 获取流程实例
        HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(procInsId)
            .includeProcessVariables()
            .singleResult();
        if (StringUtils.isNotBlank(taskId)) {
            HistoricTaskInstance taskIns = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .includeIdentityLinks()
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
            if (taskIns == null) {
                throw new ServiceException("没有可办理的任务！");
            }
            detailVo.setTaskFormData(currTaskFormData(historicProcIns.getDeploymentId(), taskIns));
        }
        // 获取Bpmn模型信息
        InputStream inputStream = repositoryService.getProcessModel(historicProcIns.getProcessDefinitionId());
        String bpmnXmlStr = StrUtil.utf8Str(IoUtil.readBytes(inputStream, false));
        BpmnModel bpmnModel = ModelUtils.getBpmnModel(bpmnXmlStr);
        detailVo.setBpmnXml(bpmnXmlStr);
        detailVo.setHistoryProcNodeList(historyProcNodeList(historicProcIns));
        detailVo.setProcessFormList(processFormList(bpmnModel, historicProcIns, dataId));
        detailVo.setFlowViewer(getFlowViewer(bpmnModel, procInsId));
        return detailVo;
    }
    
    /**
     * 流程详情信息
     *
     * @param dataId 业务数据ID
     * @return
     */
    @Override
    public WfDetailVo queryProcessDetailByDataId(String dataId ) {
        WfDetailVo detailVo = new WfDetailVo();
        WfMyBusiness business = wfMyBusinessServiceImpl.getByDataId(dataId);
        String procInsId = business.getProcessInstanceId();
        // 获取流程实例
        HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(procInsId)
            .includeProcessVariables()
            .singleResult();
        // 获取Bpmn模型信息
        InputStream inputStream = repositoryService.getProcessModel(historicProcIns.getProcessDefinitionId());
        String bpmnXmlStr = StrUtil.utf8Str(IoUtil.readBytes(inputStream, false));
        BpmnModel bpmnModel = ModelUtils.getBpmnModel(bpmnXmlStr);
        detailVo.setBpmnXml(bpmnXmlStr);
        detailVo.setHistoryProcNodeList(historyProcNodeList(historicProcIns));
        detailVo.setProcessFormList(processFormList(bpmnModel, historicProcIns, dataId));
        detailVo.setFlowViewer(getFlowViewer(bpmnModel, procInsId));
        return detailVo;
    }

    /**
     * 启动流程实例
     */
    private R startProcess(ProcessDefinition procDef, Map<String, Object> variables) {
        if (ObjectUtil.isNotNull(procDef) && procDef.isSuspended()) {
            throw new ServiceException("流程已被挂起，请先激活流程");
        }   
        // 设置流程发起人Id到流程中,包括变量
        String userStr = TaskUtils.getUserName();
        SysUser sysUsr = sysUserService.selectUserByUserName(userStr);
 		setFlowVariables(sysUsr,variables);	
 		
 		Map<String, Object> variablesnew = variables;
 		Map<String, Object> usermap = new HashMap<String, Object>();
        List<String> userlist = new ArrayList<String>();
        boolean bparallelGateway = false;
        boolean bapprovedEG = false;
        
        //业务数据id
        Object objdataId = variables.get("dataId");
        String dataId = "";
        if(ObjectUtils.isNotEmpty(objdataId)) {
        	dataId = objdataId.toString();
        }
        if(StringUtils.isNotEmpty(dataId)) {//自定义业务表单
        	//设置自定义表单dataid的数据 
            WfMyBusiness flowmybusiness = wfMyBusinessServiceImpl.getByDataId(variables.get("dataId").toString());
            String serviceImplName = flowmybusiness.getServiceImplName();
            WfCallBackServiceI flowCallBackService = (WfCallBackServiceI) SpringContextUtils.getBean(serviceImplName);
            if (flowCallBackService!=null){
              Object businessDataById = flowCallBackService.getBusinessDataById(variables.get("dataId").toString());
              variables.put("formData",businessDataById);
            }
        }
        
        //获取下个节点信息
        getNextFlowInfo(procDef, variablesnew, usermap, variables, userlist);
        
        //取出两个特殊的变量
        if(variablesnew.containsKey("bparallelGateway")) {//并行网关
        	bparallelGateway = (boolean) variablesnew.get("bparallelGateway");
        	variablesnew.remove("bparallelGateway");
        }
        if(variablesnew.containsKey("bapprovedEG")) {//通用拒绝同意排它网关
        	bapprovedEG = (boolean) variablesnew.get("bapprovedEG");
        	variablesnew.remove("bapprovedEG");
        }
      
        // 发起流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(procDef.getId(), variables);
        // 第一个用户任务为发起人，则自动完成任务
        //wfTaskService.startFirstTask(processInstance, variables);
        R<Void> result = setNextAssignee(processInstance, usermap, userlist, sysUsr, variables, bparallelGateway, bapprovedEG);	
        if(StringUtils.isNotEmpty(dataId)) {//自定义业务表单
        	// 流程发起后的自定义业务更新-需要考虑两种情况，第一个发起人审批或跳过
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().list();
            /*======================todo 启动之后  回调以及关键数据保存======================*/
            //如果保存数据前未调用必调的FlowCommonService.initActBusiness方法，就会有问题
            LoginUser sysUser = commonService.getLoginUser();
            if(tasks!=null) {
            	SysUser sysTaskUser = new SysUser();
            	List <String> listUser = new ArrayList<String>();
            	List <String> listId = new ArrayList<String>();
            	List <String> listName = new ArrayList<String>();
            	String taskUser = "";
            	String taskid = "";
            	String taskName = "";
            	int taskPriority = 0;
            	for(Task task : tasks) {
            		if(task.getAssignee() != null) {
                		sysTaskUser = commonService.getSysUserByUserName(task.getAssignee());
                		listUser.add(sysTaskUser.getNickName());
                	}
            		listId.add(task.getId());
            		listName.add(task.getName());
            	    taskPriority = task.getPriority();
            	}
            	taskUser = listUser.stream().map(String::valueOf).collect(Collectors.joining(","));
            	taskid = listId.stream().map(String::valueOf).collect(Collectors.joining(","));
            	taskName = listName.stream().map(String::valueOf).collect(Collectors.joining(","));
            	
            	WfMyBusiness business = wfMyBusinessServiceImpl.getByDataId(dataId);
    	        business.setProcessDefinitionId(procDef.getId());
    	        business.setProcessInstanceId(processInstance.getProcessInstanceId());
    	        business.setActStatus(ActStatus.doing);
    	        business.setProposer(sysUser.getUsername());
    	        business.setTaskId(taskid);
    	        business.setTaskName(taskName);
    	        business.setTaskNameId(taskid);
    	        business.setPriority(String.valueOf(taskPriority));
    	        business.setDoneUsers("");
    	        business.setTodoUsers(taskUser);
    	        wfMyBusinessService.updateById(business);
    	        //spring容器类名
    	        String serviceImplNameafter = business.getServiceImplName();
    	        WfCallBackServiceI flowCallBackServiceafter = (WfCallBackServiceI) SpringContextUtils.getBean(serviceImplNameafter);
    	        // 流程处理完后，进行回调业务层
    	        business.setValues(variables);
    	        if (flowCallBackServiceafter!=null)flowCallBackServiceafter.afterFlowHandle(business);
            }
            else {
            	WfMyBusiness business = wfMyBusinessServiceImpl.getByDataId(dataId);
    	        business.setProcessDefinitionId(procDef.getId());
    	        business.setProcessInstanceId(processInstance.getProcessInstanceId());
    	        business.setActStatus(ActStatus.pass);
    	        business.setProposer(sysUser.getUsername());
    	        business.setTaskId("");
    	        business.setTaskName("");
    	        business.setTaskNameId("");
    	        business.setDoneUsers("");
    	        business.setTodoUsers("");
    	        wfMyBusinessService.updateById(business);
    	        //spring容器类名
    	        String serviceImplNameafter = business.getServiceImplName();
    	        WfCallBackServiceI flowCallBackServiceafter = (WfCallBackServiceI) SpringContextUtils.getBean(serviceImplNameafter);
    	        // 流程处理完后，进行回调业务层
    	        business.setValues(variables);
    	        if (flowCallBackServiceafter!=null)flowCallBackServiceafter.afterFlowHandle(business);
            }
        }
        return result;	
    }
    
    /**
	 * 设置下个节点信息处理人员
	 *  add by nbacheng
	 *           
	 * @param variablesnew, usermap,
	 *		  userlist, sysUser, variables,  bparallelGateway
	 *            
	 * @return
	 */
	private R<Void> setNextAssignee(ProcessInstance processInstance, Map<String, Object> usermap,
			                       List<String> userlist, SysUser sysUser, Map<String, Object> variables,
			                       boolean bparallelGateway, boolean bapprovedEG) {
		// 给第一步申请人节点设置任务执行人和意见
		if((usermap.containsKey("isSequential")) && !(boolean)usermap.get("isSequential")) {//并发会签会出现2个以上需要特殊处理
			List<Task> nexttasklist = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().list();
			  int i=0;
			  for (Task nexttask : nexttasklist) {
				   String assignee = userlist.get(i).toString();	
				   taskService.addComment(nexttask.getId(), processInstance.getProcessInstanceId(),
							FlowComment.NORMAL.getType(), sysUser.getNickName() + "发起流程申请");
			       taskService.setAssignee(nexttask.getId(), assignee);
			       i++;
			  }
			  return R.ok("多实例会签流程启动成功.");
 	    }
		else {// 给第一步申请人节点设置任务执行人和意见
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active()
				.singleResult();
			if (Objects.nonNull(task)) {
				taskService.addComment(task.getId(), processInstance.getProcessInstanceId(),
						FlowComment.NORMAL.getType(), sysUser.getNickName() + "发起流程申请");
				taskService.setAssignee(task.getId(), sysUser.getUserName());
			}

			// 获取下一个节点数据及设置数据

			FlowNextDto	nextFlowNode = wfTaskService.getNextFlowNode(task.getId(), variables);
			if(Objects.nonNull(nextFlowNode)) {
				if (Objects.nonNull(task)) {
					Map<String, Object> nVariablesMap = taskService.getVariables(task.getId());
					if(nVariablesMap.containsKey("SetAssigneeTaskListener")) {//是否通过动态设置审批人的任务监听器
					  taskService.complete(task.getId(), variables);
					  Task nexttask = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().singleResult();
					  taskService.setAssignee(nexttask.getId(), nVariablesMap.get("SetAssigneeTaskListener").toString());
					  return R.ok("通过动态设置审批人的任务监听器流程启动成功.");
				    }
					if(nVariablesMap.containsKey("SetDeptHeadTaskListener")) {//是否通过动态设置发起人部门负责人的任务监听器
						  taskService.complete(task.getId(), variables);
						  Task nexttask = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().singleResult();
						  if(Objects.nonNull(nexttask)) {
							  if(Objects.nonNull((List<String>) nVariablesMap.get("SetDeptHeadTaskListener"))) {
								  if(((List<String>) nVariablesMap.get("SetDeptHeadTaskListener")).size() == 1) {//是否就一个人
									  taskService.setAssignee(nexttask.getId(), ((List<String>)nVariablesMap.get("SetDeptHeadTaskListener")).get(0).toString());
							          return R.ok("设置发起人部门负责人的任务监听器流程启动成功.");
								  }
								  else {
									  for (String username : ((List<String>) nVariablesMap.get("SetDeptHeadTaskListener"))) {
	        							  taskService.addCandidateUser(nexttask.getId(), username);
	        						  }
						             return R.ok("设置多个发起人部门负责人的任务监听器流程启动成功,目前用户可通过签收方式完成审批."); 
								  }
								  
							  }
							
						  }
						  
					}
				}
				if(Objects.nonNull(nextFlowNode.getUserList())) {
					if( nextFlowNode.getUserList().size() == 1 ) {
						if (nextFlowNode.getUserList().get(0) != null) {
							if(StringUtils.equalsAnyIgnoreCase(nextFlowNode.getUserList().get(0).getUserName(), "${initiator}")) {//对发起人做特殊处理
								taskService.complete(task.getId(), variables);
								return R.ok("流程启动成功给发起人.");
							}
							else if(nextFlowNode.getUserTask().getCandidateUsers().size()>0 && StringUtils.equalsAnyIgnoreCase(nextFlowNode.getUserTask().getCandidateUsers().get(0), "${DepManagerHandler.getUsers(execution)}")) {//对部门经理做特殊处理								
								//taskService.complete(task.getId(), variables);
								return R.ok("流程启动成功给部门经理,请到我的待办里进行流程的提交流转.");
							}
							else {
								taskService.complete(task.getId(), variables);
							    return R.ok("流程启动成功.");
							}
						}
						else {
							return R.fail("审批人不存在，流程启动失败!");
						}
						
					}
					else if(nextFlowNode.getType() == ProcessConstants.PROCESS_MULTI_INSTANCE ) {//对多实例会签做特殊处理或者以后在流程设计进行修改也可以
		                Map<String, Object> approvalmap = new HashMap<>();
		                List<String> sysuserlist = nextFlowNode.getUserList().stream().map(obj-> (String) obj.getUserName()).collect(Collectors.toList());
						approvalmap.put("approval", sysuserlist);
						taskService.complete(task.getId(), approvalmap);
						if(!nextFlowNode.isBisSequential()){//对并发会签进行assignee单独赋值
		  				  List<Task> nexttasklist = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().list();
		  				  int i=0;
		  				  for (Task nexttask : nexttasklist) {
		  					   String assignee = sysuserlist.get(i).toString();	
		      			       taskService.setAssignee(nexttask.getId(), assignee);
		      			       i++;
		  				  }
		  				 
		  				}
						return R.ok("多实例会签流程启动成功.");
					}
					else if(nextFlowNode.getUserList().size() > 1) {
						if (bparallelGateway) {//后一个节点是并行网关的话
							taskService.complete(task.getId(), variables);
							return R.ok("流程启动成功.");
						}
						else {
							return R.ok("流程启动成功,请到我的待办里进行流程的提交流转.");
						}
					}
					else {
						return R.ok("流程启动失败,请检查流程设置人员！");
					}
				}
				else {//对跳过流程做特殊处理
					List<UserTask> nextUserTask = FindNextNodeUtil.getNextUserTasks(repositoryService, task, variables);
		            if (CollectionUtils.isNotEmpty(nextUserTask)) {
		            	List<FlowableListener> listlistener = nextUserTask.get(0).getTaskListeners();
		            	if(CollectionUtils.isNotEmpty(listlistener)) {
		            		String tasklistener =  listlistener.get(0).getImplementation();
		            		if(StringUtils.contains(tasklistener, "AutoSkipTaskListener")) {
			            		taskService.complete(task.getId(), variables);
			    				return R.ok("流程启动成功.");
			            	}else {
				            	return R.ok("流程启动失败,请检查流程设置人员！");
				            }
		            	}else {
			            	return R.ok("流程启动失败,请检查流程设置人员！");
			            }
		            	
		            }
		            else {
		            	return R.ok("流程启动失败,请检查流程设置人员！");
		            }
				}
			}
			else {
				if(bapprovedEG) {
					return R.ok("通用拒绝同意流程启动成功,请到我的待办里进行流程的提交流转.");
				}
				taskService.complete(task.getId(), variables);
				return R.ok("流程启动成功.");
			}
		}
	}

	/**
	 * 设置发起人变量
	 *  add by nbacheng
	 *           
	 * @param variables
	 *            流程变量
	 * @return
	 */
	private void setFlowVariables(SysUser sysUser,Map<String, Object> variables) {
		 // 设置流程发起人Id到流程中
        identityService.setAuthenticatedUserId(sysUser.getUserName());
        variables.put(BpmnXMLConstants.ATTRIBUTE_EVENT_START_INITIATOR, sysUser.getUserName());
        // 设置流程状态为进行中
        variables.put(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.RUNNING.getStatus());
     // 设置流程状态为进行中
        variables.put(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.RUNNING.getStatus());
	}
    
    /**
	 * 获取下个节点信息,对并行与排它网关做处理
	 *  add by nbacheng
	 *           
	 * @param processDefinition, variablesnew, usermap,
			   variables, userlist, bparallelGateway
	 *           
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void getNextFlowInfo(ProcessDefinition processDefinition, Map<String, Object> variablesnew, Map<String, Object> usermap,
			                     Map<String, Object> variables, List<String> userlist) {
		String definitionld = processDefinition.getId();        //获取bpm（模型）对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionld);
        //传节点定义key获取当前节点
        List<org.flowable.bpmn.model.Process> processes =  bpmnModel.getProcesses();
        //只处理发起人后面排它网关再后面是会签的情况，其它目前不考虑
        //List<UserTask> userTasks = process.findFlowElementsOfType(UserTask.class);
        List<FlowNode> flowNodes = processes.get(0).findFlowElementsOfType(FlowNode.class);
        List<SequenceFlow> outgoingFlows = flowNodes.get(1).getOutgoingFlows();
        //遍历返回下一个节点信息
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            //类型自己判断（获取下个节点是网关还是节点）
            FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
            //下个是节点
           if(targetFlowElement instanceof ExclusiveGateway){// 下个出口是排它网关的话,后一个用户任务又是会签的情况下需要approval的赋值处理，否则报错
        	   usermap =  GetExclusiveGatewayUser(targetFlowElement,variables);//还是需要返回用户与是否并发，因为并发要做特殊处理
        	   if(usermap != null) {
        		 userlist = (ArrayList<String>) usermap.get("approval");
        	     variablesnew.put("approval", userlist);
        	   }
        	   if(FindNextNodeUtil.GetExclusiveGatewayExpression(targetFlowElement)) {//下个出口是通用拒绝同意排它网关
        		   variablesnew.put("bapprovedEG",true);
        	   }
        	   break;
            }
           if(targetFlowElement instanceof ParallelGateway){// 下个出口是并行网关的话,直接需要进行complete，否则报错
        	   variablesnew.put("bparallelGateway",true);
           }
        }
	}

	/**
     * 获取排他网关分支名称、分支表达式、下一级任务节点
     * @param flowElement
     * @param data
     * add by nbacheng
     */
    private Map<String, Object> GetExclusiveGatewayUser(FlowElement flowElement,Map<String, Object> variables){
    	// 获取所有网关分支
        List<SequenceFlow> targetFlows=((ExclusiveGateway)flowElement).getOutgoingFlows();
        // 循环每个网关分支
        for(SequenceFlow sequenceFlow : targetFlows){
            // 获取下一个网关和节点数据
            FlowElement targetFlowElement=sequenceFlow.getTargetFlowElement();
            // 网关数据不为空
            if (StringUtils.isNotBlank(sequenceFlow.getConditionExpression())) {
                // 获取网关判断条件
            	String expression = sequenceFlow.getConditionExpression();
                if (expression == null ||Boolean.parseBoolean(
                                String.valueOf(
                                		FindNextNodeUtil.result(variables, expression.substring(expression.lastIndexOf("{") + 1, expression.lastIndexOf("}")))))) {
                	// 网关出线的下个节点是用户节点
                    if(targetFlowElement instanceof UserTask){
                        // 判断是否是会签
                        UserTask userTask = (UserTask) targetFlowElement;
                        MultiInstanceLoopCharacteristics multiInstance = userTask.getLoopCharacteristics();
                    	if (Objects.nonNull(multiInstance)) {//下个节点是会签节点
                    		Map<String, Object> approvalmap = new HashMap<>();
                    		List<String> getuserlist =  getmultiInstanceUsers(multiInstance,userTask);
                    		approvalmap.put("approval", getuserlist);
                    		if(multiInstance.isSequential()) {
                    			approvalmap.put("isSequential", true);
                    		}
                    		else {
                    			approvalmap.put("isSequential", false);
                    		}
                    		return approvalmap;
                    	}
                    }
                }
            }
        }
		return null;
    }
    
    /**
     * 获取多实例会签用户信息
     * @param userTask
     * @param multiInstance
     *
     **/
    List<String> getmultiInstanceUsers(MultiInstanceLoopCharacteristics multiInstance,UserTask userTask) {
    	List<String> sysuserlist = new ArrayList<>();
    	List<String> rolelist = new ArrayList<>();
        rolelist = userTask.getCandidateGroups();
    	List<String> userlist = new ArrayList<>();
        userlist = userTask.getCandidateUsers();
        if(rolelist.size() > 0) {
        	List<SysUser> list = new ArrayList<SysUser>();
			for(String roleId : rolelist ){
        	  List<SysUser> templist = commonService.getUserListByRoleId(roleId);
        	  for(SysUser sysuser : templist) {
          		SysUser sysUserTemp = sysUserService.selectUserById(sysuser.getUserId());
          		list.add(sysUserTemp);
          	  }
        	}
			sysuserlist = list.stream().map(obj-> (String) obj.getUserName()).collect(Collectors.toList());
           
        }
        else if(userlist.size() > 0) {
        	List<SysUser> list = new ArrayList<SysUser>();
        	for(String username : userlist) {
        		SysUser sysUser =  sysUserService.selectUserByUserName(username);
        		list.add(sysUser);
        	}
        	sysuserlist = list.stream().map(obj-> (String) obj.getUserName()).collect(Collectors.toList());
        }    
    	return sysuserlist;
    }

    /**
     * 获取流程变量
     *
     * @param taskId 任务ID
     * @return 流程变量
     */
    private Map<String, Object> getProcessVariables(String taskId) {
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
     * 获取当前任务流程表单信息
     */
    private FormConf currTaskFormData(String deployId, HistoricTaskInstance taskIns) {
        WfDeployFormVo deployFormVo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
            .eq(WfDeployForm::getDeployId, deployId)
            .eq(WfDeployForm::getFormKey, taskIns.getFormKey())
            .eq(WfDeployForm::getNodeKey, taskIns.getTaskDefinitionKey()));
        if (ObjectUtil.isNotEmpty(deployFormVo)) {
            FormConf currTaskFormData = JsonUtils.parseObject(deployFormVo.getContent(), FormConf.class);
            if (null != currTaskFormData) {
                //currTaskFormData.setFormBtns(false);
                ProcessFormUtils.fillFormData(currTaskFormData, taskIns.getTaskLocalVariables());
                return currTaskFormData;
            }
        }
        return null;
    }

    /**
     * 获取历史流程表单信息
     */
    private List<FormConf> processFormList(BpmnModel bpmnModel, HistoricProcessInstance historicProcIns, String dataId) {
        List<FormConf> procFormList = new ArrayList<>();

        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(historicProcIns.getId()).finished()
            .activityTypes(CollUtil.newHashSet(BpmnXMLConstants.ELEMENT_EVENT_START, BpmnXMLConstants.ELEMENT_TASK_USER))
            .orderByHistoricActivityInstanceStartTime().asc()
            .list();
        List<String> processFormKeys = new ArrayList<>();
        for (HistoricActivityInstance activityInstance : activityInstanceList) {
            // 获取当前节点流程元素信息
            FlowElement flowElement = ModelUtils.getFlowElementById(bpmnModel, activityInstance.getActivityId());
            // 获取当前节点表单Key
            String formKey = ModelUtils.getFormKey(flowElement);
            if (formKey == null) {
                continue;
            }
            boolean localScope = Convert.toBool(ModelUtils.getElementAttributeValue(flowElement, ProcessConstants.PROCESS_FORM_LOCAL_SCOPE), false);
            Map<String, Object> variables;
            if (localScope) {
                // 查询任务节点参数，并转换成Map
                variables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicProcIns.getId())
                    .taskId(activityInstance.getTaskId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));
            } else {
                if (processFormKeys.contains(formKey)) {
                    continue;
                }
                variables = historicProcIns.getProcessVariables();
                processFormKeys.add(formKey);
            }  
           
            Map<String, Object> formvariables = new HashedMap<String, Object>();
            //遍历Map
            if(variables.containsKey("variables")) {
              formvariables = (Map<String, Object>)((Map<String, Object>) variables.get("variables")).get("formValue");
            }
 
            // 非节点表单此处查询结果可能有多条，只获取第一条信息
            List<WfDeployFormVo> formInfoList = deployFormMapper.selectVoList(new LambdaQueryWrapper<WfDeployForm>()
                .eq(WfDeployForm::getDeployId, historicProcIns.getDeploymentId())
                .eq(WfDeployForm::getFormKey, formKey)
                .eq(localScope, WfDeployForm::getNodeKey, flowElement.getId()));

            //@update by Brath：避免空集合导致的NULL空指针
            WfDeployFormVo formInfo = formInfoList.stream().findFirst().orElse(null);
         
            if (ObjectUtil.isNotNull(formInfo)) {
                // 旧数据 formInfo.getFormName() 为 null
                String formName = Optional.ofNullable(formInfo.getFormName()).orElse(StringUtils.EMPTY);
                String title = localScope ? formName.concat("(" + flowElement.getName() + ")") : formName;
                FormConf formConf = JsonUtils.parseObject(formInfo.getContent(), FormConf.class);
                if (null != formConf) {
                    //ProcessFormUtils.fillFormData(formConf, variables);
                	formConf.setTitle(title);
                	formConf.setFormValues(formvariables);
                    procFormList.add(formConf);
                }
            }
        }
        if(StringUtils.isNoneEmpty(dataId)) {
        	WfMyBusiness business = wfMyBusinessServiceImpl.getByDataId(dataId);
            String serviceImplName = business.getServiceImplName();
            WfCallBackServiceI flowCallBackService = (WfCallBackServiceI) SpringContextUtils.getBean(serviceImplName);
            // 流程处理完后，进行回调业务层
            if (flowCallBackService!=null){
            	Map<String, Object> customMap = new HashMap<String, Object>();
	            FormConf formConf = new FormConf();
	            Object businessDataById = flowCallBackService.getBusinessDataById(dataId);
	            customMap.put("formData",businessDataById);
	            customMap.put("routeName", business.getRouteName());
	            formConf.setFormValues(customMap);
	            procFormList.add(formConf);
            }
        }
        return procFormList;
    }

    @Deprecated
    private void buildStartFormData(HistoricProcessInstance historicProcIns, Process process, String deployId, List<FormConf> procFormList) {
        procFormList = procFormList == null ? new ArrayList<>() : procFormList;
        HistoricActivityInstance startInstance = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(historicProcIns.getId())
            .activityId(historicProcIns.getStartActivityId())
            .singleResult();
        StartEvent startEvent = (StartEvent) process.getFlowElement(startInstance.getActivityId());
        WfDeployFormVo startFormInfo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
            .eq(WfDeployForm::getDeployId, deployId)
            .eq(WfDeployForm::getFormKey, startEvent.getFormKey())
            .eq(WfDeployForm::getNodeKey, startEvent.getId()));
        if (ObjectUtil.isNotNull(startFormInfo)) {
            FormConf formConf = JsonUtils.parseObject(startFormInfo.getContent(), FormConf.class);
            if (null != formConf) {
                //formConf.setTitle(startEvent.getName());
                //formConf.setDisabled(true);
                //formConf.setFormBtns(false);
                ProcessFormUtils.fillFormData(formConf, historicProcIns.getProcessVariables());
                procFormList.add(formConf);
            }
        }
    }

    @Deprecated
    private void buildUserTaskFormData(String procInsId, String deployId, Process process, List<FormConf> procFormList) {
        procFormList = procFormList == null ? new ArrayList<>() : procFormList;
        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(procInsId).finished()
            .activityType(BpmnXMLConstants.ELEMENT_TASK_USER)
            .orderByHistoricActivityInstanceStartTime().asc()
            .list();
        for (HistoricActivityInstance instanceItem : activityInstanceList) {
            UserTask userTask = (UserTask) process.getFlowElement(instanceItem.getActivityId(), true);
            String formKey = userTask.getFormKey();
            if (formKey == null) {
                continue;
            }
            // 查询任务节点参数，并转换成Map
            Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(procInsId)
                .taskId(instanceItem.getTaskId())
                .list()
                .stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));
            WfDeployFormVo deployFormVo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
                .eq(WfDeployForm::getDeployId, deployId)
                .eq(WfDeployForm::getFormKey, formKey)
                .eq(WfDeployForm::getNodeKey, userTask.getId()));
            if (ObjectUtil.isNotNull(deployFormVo)) {
                FormConf formConf = JsonUtils.parseObject(deployFormVo.getContent(), FormConf.class);
                if (null != formConf) {
                    //formConf.setTitle(userTask.getName());
                    //formConf.setDisabled(true);
                    //formConf.setFormBtns(false);
                    ProcessFormUtils.fillFormData(formConf, variables);
                    procFormList.add(formConf);
                }
            }
        }
    }

    /**
     * 获取历史任务信息列表
     */
    private List<WfProcNodeVo> historyProcNodeList(HistoricProcessInstance historicProcIns) {
        String procInsId = historicProcIns.getId();
        List<HistoricActivityInstance> historicActivityInstanceList =  historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(procInsId)
            .activityTypes(CollUtil.newHashSet(BpmnXMLConstants.ELEMENT_EVENT_START, BpmnXMLConstants.ELEMENT_EVENT_END, BpmnXMLConstants.ELEMENT_TASK_USER))
            .orderByHistoricActivityInstanceStartTime().desc()
            .orderByHistoricActivityInstanceEndTime().desc()
            .list();

        List<Comment> commentList = taskService.getProcessInstanceComments(procInsId);

        List<WfProcNodeVo> elementVoList = new ArrayList<>();
        for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
            WfProcNodeVo elementVo = new WfProcNodeVo();
            elementVo.setProcDefId(activityInstance.getProcessDefinitionId());
            elementVo.setActivityId(activityInstance.getActivityId());
            elementVo.setActivityName(activityInstance.getActivityName());
            elementVo.setActivityType(activityInstance.getActivityType());
            elementVo.setCreateTime(activityInstance.getStartTime());
            elementVo.setEndTime(activityInstance.getEndTime());
            if (ObjectUtil.isNotNull(activityInstance.getDurationInMillis())) {
                elementVo.setDuration(DateUtil.formatBetween(activityInstance.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            }

            if (BpmnXMLConstants.ELEMENT_EVENT_START.equals(activityInstance.getActivityType())) {
                if (ObjectUtil.isNotNull(historicProcIns)) {
                    String userName = historicProcIns.getStartUserId();
                    String nickName = sysUserService.selectUserByUserName(userName).getNickName();
                    if (nickName != null) {
                        elementVo.setAssigneeId(userName);
                        elementVo.setAssigneeName(nickName);
                    }
                }
            } else if (BpmnXMLConstants.ELEMENT_TASK_USER.equals(activityInstance.getActivityType())) {
                if (StringUtils.isNotBlank(activityInstance.getAssignee())) {
                	String userName = activityInstance.getAssignee();
                    String nickName = sysUserService.selectUserByUserName(userName).getNickName();
                    elementVo.setAssigneeId(userName);
                    elementVo.setAssigneeName(nickName);
                }
                // 展示审批人员
                List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(activityInstance.getTaskId());
                StringBuilder stringBuilder = new StringBuilder();
                for (HistoricIdentityLink identityLink : linksForTask) {
                    if ("candidate".equals(identityLink.getType())) {
                        if (StringUtils.isNotBlank(identityLink.getUserId())) {
                            String userId = identityLink.getUserId();
                            String nickName = sysUserService.selectUserByUserName(userId).getNickName();
                            stringBuilder.append(nickName).append(",");
                        }
                        if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                            if (identityLink.getGroupId().startsWith(TaskConstants.ROLE_GROUP_PREFIX)) {
                                Long roleId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.ROLE_GROUP_PREFIX));
                                SysRole role = roleService.selectRoleById(roleId);
                                stringBuilder.append(role.getRoleName()).append(",");
                            } else if (identityLink.getGroupId().startsWith(TaskConstants.DEPT_GROUP_PREFIX)) {
                                Long deptId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.DEPT_GROUP_PREFIX));
                                SysDept dept = deptService.selectDeptById(deptId);
                                stringBuilder.append(dept.getDeptName()).append(",");
                            }
                        }
                    }
                }
                if (StringUtils.isNotBlank(stringBuilder)) {
                    elementVo.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
                }
                // 获取意见评论内容
                if (CollUtil.isNotEmpty(commentList)) {
                    List<Comment> comments = new ArrayList<>();
                    for (Comment comment : commentList) {

                        if (comment.getTaskId().equals(activityInstance.getTaskId())) {
                            comments.add(comment);
                        }
                    }
                    elementVo.setCommentList(comments);
                }
            }
            elementVoList.add(elementVo);
        }
        return elementVoList;
    }

    /**
     * 获取流程执行过程
     *
     * @param procInsId
     * @return
     */
    private WfViewerVo getFlowViewer(BpmnModel bpmnModel, String procInsId) {
        // 构建查询条件
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(procInsId);
        List<HistoricActivityInstance> allActivityInstanceList = query.list();
        if (CollUtil.isEmpty(allActivityInstanceList)) {
            return new WfViewerVo();
        }
        // 查询所有已完成的元素
        List<HistoricActivityInstance> finishedElementList = allActivityInstanceList.stream()
            .filter(item -> ObjectUtil.isNotNull(item.getEndTime())).collect(Collectors.toList());
        // 所有已完成的连线
        Set<String> finishedSequenceFlowSet = new HashSet<>();
        // 所有已完成的任务节点
        Set<String> finishedTaskSet = new HashSet<>();
        finishedElementList.forEach(item -> {
            if (BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW.equals(item.getActivityType())) {
                finishedSequenceFlowSet.add(item.getActivityId());
            } else {
                finishedTaskSet.add(item.getActivityId());
            }
        });
        // 查询所有未结束的节点
        Set<String> unfinishedTaskSet = allActivityInstanceList.stream()
            .filter(item -> ObjectUtil.isNull(item.getEndTime()))
            .map(HistoricActivityInstance::getActivityId)
            .collect(Collectors.toSet());
        // DFS 查询未通过的元素集合
        Set<String> rejectedSet = FlowableUtils.dfsFindRejects(bpmnModel, unfinishedTaskSet, finishedSequenceFlowSet, finishedTaskSet);
        return new WfViewerVo(finishedTaskSet, finishedSequenceFlowSet, unfinishedTaskSet, rejectedSet);
    }

    /**
	 * 获取流程是否结束
	 *  add by nbacheng          
	 * @param String procInsId       
	 * @return
	 */
	@Override
	public boolean processIscompleted(String procInsId) {
		
		// 获取流程状态
        HistoricVariableInstance processStatusVariable = historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(procInsId)
            .variableName(ProcessConstants.PROCESS_STATUS_KEY)
            .singleResult();
        if (ObjectUtil.isNotNull(processStatusVariable)) {
        	String processStatus = null;
            if (ObjectUtil.isNotNull(processStatusVariable)) {
                processStatus = Convert.toStr(processStatusVariable.getValue());
                if(StringUtils.equalsIgnoreCase(processStatus, "completed")) {
                	return true;
                }
            }
        }    
		return false;
	}

	/**
	 * 根据流程dataId,serviceName启动流程实例，主要是自定义业务表单发起流程使用
	 *  add by nbacheng
	 * @param dataId,serviceName
	 *           
	 * @param variables
	 *            流程变量
	 * @return
	 */
	@Override
	public R<Void> startProcessByDataId(String dataId, String serviceName, Map<String, Object> variables) {
//提交审批的时候进行流程实例关联初始化
    	
        if (serviceName==null){
             return R.fail("未找到serviceName："+serviceName);
        }
        WfCustomForm wfCustomForm = wfCustomFormService.selectSysCustomFormByServiceName(serviceName);
        if(wfCustomForm ==null){
        	 return R.fail("未找到sysCustomForm："+serviceName);
        }
        //优先考虑自定义业务表是否关联流程，再看通用的表单流程关联表
        ProcessDefinition processDefinition;
        String deployId = wfCustomForm.getDeployId();
        if(StringUtils.isEmpty(deployId)) {
        	WfDeployForm sysDeployForm  = deployFormMapper.selectSysDeployFormByFormId("key_"+String.valueOf(wfCustomForm.getId()));
            if(sysDeployForm ==null){          	
       	       return R.fail("自定义表单也没关联流程定义表,流程没定义关联自定义表单"+wfCustomForm.getId());
            }
            processDefinition = repositoryService.createProcessDefinitionQuery()
        		.parentDeploymentId(sysDeployForm.getDeployId()).latestVersion().singleResult();
        }
        else {
        	processDefinition = repositoryService.createProcessDefinitionQuery()
            		.parentDeploymentId(deployId).latestVersion().singleResult();
        }
        
        LambdaQueryWrapper<WfMyBusiness> wfMyBusinessLambdaQueryWrapper = new LambdaQueryWrapper<>();
        wfMyBusinessLambdaQueryWrapper.eq(WfMyBusiness::getDataId, dataId);
        WfMyBusiness business = wfMyBusinessService.getOne(wfMyBusinessLambdaQueryWrapper);
        if (business==null){
        	if(processDefinition==null) {
        		return R.fail("自定义表单也没关联流程定义表,流程没定义关联自定义表单"+wfCustomForm.getId());
        	}
        	boolean binit = wfCommonService.initActBusiness(wfCustomForm.getBusinessName(), dataId, serviceName, 
        	processDefinition.getKey(), processDefinition.getId(), wfCustomForm.getRouteName());
        	if(!binit) {
        		return R.fail("自定义表单也没关联流程定义表,流程没定义关联自定义表单"+wfCustomForm.getId());
        	}
        	WfMyBusiness businessnew = wfMyBusinessService.getOne(wfMyBusinessLambdaQueryWrapper);
           //流程实例关联初始化结束
            if (StrUtil.isNotBlank(businessnew.getProcessDefinitionId())){
              return this.startProcessByDefId(businessnew.getProcessDefinitionId(),variables);
            }
            return this.startProcessByDefKey(businessnew.getProcessDefinitionKey(),variables);
        }
        else {
        	 return R.fail("已经存在这个dataid实例，不要重复申请："+dataId);
        }
        
	}
}
