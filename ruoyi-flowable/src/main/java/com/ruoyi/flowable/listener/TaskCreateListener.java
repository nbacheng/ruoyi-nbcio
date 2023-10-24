package com.ruoyi.flowable.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

import com.ruoyi.common.core.service.CommonService;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;

import javax.annotation.Resource;

/**
 * 全局监听-工作流待办消息提醒
 *
 * @author nbacheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskCreateListener implements FlowableEventListener {
	
    private final TaskService taskService;
    
    @Resource
    private CommonService commonService;
    
    @Resource
	protected RepositoryService repositoryService;
	
	@Resource
    protected HistoryService historyService;
	   

    @Override
    public void onEvent(FlowableEvent flowableEvent) {
    	FlowableEventType type = flowableEvent.getType();
    	if (type == FlowableEngineEventType.TASK_ASSIGNED) { 
    		if(flowableEvent instanceof org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl ) {
    			TaskEntity taskEntity = (TaskEntity) ((org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl) flowableEvent).getEntity();
    			String taskId = taskEntity.getId();
    	        String procInsId = taskEntity.getProcessInstanceId();
    	        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
    	                .processInstanceId(procInsId)
    	                .singleResult();
    			String businessKey =  historicProcessInstance.getBusinessKey();
    			String deployId = historicProcessInstance.getDeploymentId();
    			String startUserId = historicProcessInstance.getStartUserId();
    	        //获取任务接收人
    			String receiver = taskEntity.getAssignee();
    	        if (StringUtils.isNotEmpty(receiver)) {
    	            //发送提醒消息
    	        	String category = "";
    	        	if(taskService.getVariables(taskId).get("category") != null) {
    	        		category = taskService.getVariables(taskId).get("category").toString();
    	        	}
    	      
    	        	LoginUser loginUser = commonService.getLoginUser();
    	        	String taskMessageUrl;
    	        	if(StringUtils.isNotBlank(businessKey)) {
    	    			taskMessageUrl = "<a href=" + commonService.getBaseUrl() + procInsId + "?taskId="
    	    					          + taskId + "&businessKey=" + businessKey + "&category=" 
    	    					          + category + "&processed=true"  + ">点击这个进行处理</a>" ;
    	    		}
    	    		else {
    	    			taskMessageUrl = "<a href=" + commonService.getBaseUrl() + procInsId + "?taskId="
    	    					         + taskId + "&businessKey" + "&category=" 
    	    					         + category + "&processed=true" + ">点击这个进行处理</a>" ;
    	    		}
    	        	String msgContent = "流程待办通知" + taskMessageUrl;
    	      		if(!StringUtils.equals((loginUser.getUserId()).toString(),receiver)) {//发起人或登录人自己不发送
    	      			log.info("流程待办通知给:" + receiver);
    	      			commonService.sendSysNotice(loginUser.getUserId().toString(), receiver, "流程待办通知", msgContent, Constants.MSG_CATEGORY_3);//setMsgCategory=3是待办
    	        	}
    	        }
    		}
    	}	
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}

