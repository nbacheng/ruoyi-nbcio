package com.ruoyi.flowable.listener;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.task.api.Task;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.core.service.CommonService;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;

import lombok.RequiredArgsConstructor;

/**
 * 全局监听-工作流完成消息提醒
 *
 * @author nbacheng
 */

//必须要用 AbstractFlowableEngineEventListener 用FlowableEventListener这个会出现问题，应该是已经完成了
@Component
@RequiredArgsConstructor
public class ProcessCompleteListener extends AbstractFlowableEngineEventListener {

	private final TaskService taskService;
	
    @Resource
    private CommonService commonService;
    
    @Autowired
    protected HistoryService historyService;
    
    @Resource
	protected RepositoryService repositoryService;

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        System.out.println("进入流程结束监听器……");

        String procInsId = event.getProcessInstanceId();
        HistoricProcessInstance hi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .singleResult();
        
        List<Task> listtask = taskService.createTaskQuery().processInstanceId(procInsId).active().list();
        String taskId = "";
        if(listtask !=null) {
        	taskId = listtask.get(0).getId();
        }
        String startUserId = hi.getStartUserId();
		String businessKey =  hi.getBusinessKey();
		String deployId = hi.getDeploymentId();
		String category =  "";
        
        if (StringUtils.isNotEmpty(startUserId)) {
            // TODO:  发送提醒消息
        	if(((ExecutionEntityImpl)event.getEntity()).getVariableInstances().get("category") !=null) {
        		category = ((VariableInstance)((ExecutionEntityImpl)event.getEntity()).getVariableInstances().get("category")).getTextValue();
        	}
        	
        	LoginUser loginUser = commonService.getLoginUser();
        	String taskMessageUrl;
        	if(StringUtils.isNotBlank(businessKey)) {
    			taskMessageUrl = "<a href=" + commonService.getBaseUrl() + procInsId 
    				              + "?processed=false" + ">点击这个进行查看</a>" ;
    		}
    		else {
    			taskMessageUrl = "<a href=" + commonService.getBaseUrl() + procInsId 
			              + "?processed=false" + ">点击这个进行查看</a>" ;
    		}
        	String msgContent = "流程任务结束通知" + taskMessageUrl; 
        	commonService.sendSysNotice(loginUser.getUserId().toString(), startUserId, "流程任务结束通知", msgContent, Constants.MSG_CATEGORY_1);//setMsgCategory=1是通知
        }

        super.processCompleted(event);
    }

    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        System.out.println("进入taskCompleted监听器……");
        super.taskCompleted(event);
    }

    @Override
    public void onEvent(FlowableEvent flowableEvent) {
        System.out.println("进入taskCompleted监听器--onEvent……");
        super.onEvent(flowableEvent);
    }
}