package com.ruoyi.workflow.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.workflow.service.impl.WfInstanceServiceImpl;
import com.ruoyi.workflow.service.impl.WfMyBusinessServiceImpl;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.workflow.domain.WfMyBusiness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *业务模块调用API的集合
 *@author PanMeiCheng,nbacheng
 *@date 2022/11/02
 *@version 1.0
 */
@Service
public class WfCommonService {
    
	@Autowired
    WfMyBusinessServiceImpl wfMyBusinessService;
    
	@Autowired
	WfInstanceServiceImpl wfInstanceService;
    
    /**
     * 初始生成或修改业务与流程的关联信息<br/>
     * 当业务模块新增一条数据后调用，此时业务数据关联一个流程定义，以备后续流程使用
     * @return 是否成功
     * @param title 必填。流程业务简要描述。例：2021年11月26日xxxxx申请
     * @param dataId 必填。业务数据Id，如果是一对多业务关系，传入主表的数据Id
     * @param serviceImplName 必填。业务service注入spring容器的名称。
*                        例如：@Service("demoService")则传入 demoService
     * @param processDefinitionKey 必填。流程定义Key，传入此值，未来启动的会是该类流程的最新一个版本
     * @param processDefinitionId 选填。流程定义Id，传入此值，未来启动的为指定版本的流程
     */
    public boolean initActBusiness(String title,String dataId, String serviceImplName, String processDefinitionKey, String processDefinitionId,String routeName){
        boolean hasBlank = StrUtil.hasBlank(title,dataId, serviceImplName, processDefinitionKey);
        if (hasBlank) throw new ServiceException("流程关键参数未填完全！dataId, serviceImplName, processDefinitionKey");
        LambdaQueryWrapper<WfMyBusiness> flowMyBusinessLambdaQueryWrapper = new LambdaQueryWrapper<>();
        flowMyBusinessLambdaQueryWrapper.eq(WfMyBusiness::getDataId, dataId);
        WfMyBusiness wfMyBusiness = new WfMyBusiness();
        WfMyBusiness business = wfMyBusinessService.getOne(flowMyBusinessLambdaQueryWrapper);
        if (business!=null){
        	wfMyBusiness = business;
        } 
        if (processDefinitionId==null){
            // 以便更新流程
            processDefinitionId = "";
            R.fail("自定义表单也没关联流程定义表或流程定义里关联自定义表单");
            return false;
        }
        wfMyBusiness.setTitle(title);
        wfMyBusiness.setDataId(dataId);
        wfMyBusiness.setServiceImplName(serviceImplName);
        wfMyBusiness.setProcessDefinitionKey(processDefinitionKey);
        wfMyBusiness.setProcessDefinitionId(processDefinitionId);
        wfMyBusiness.setRouteName(routeName);
        if (business!=null){
            return wfMyBusinessService.updateById(wfMyBusiness);
        } else {
            return wfMyBusinessService.save(wfMyBusiness);
        }
    }

    /**
     * 删除流程
     * @param dataId
     * @return
     */
    public boolean delActBusiness(String dataId){
        boolean hasBlank = StrUtil.hasBlank(dataId);
        if (hasBlank) throw new ServiceException("流程关键参数未填完全！dataId");
        LambdaQueryWrapper<WfMyBusiness> flowMyBusinessQueryWrapper = new LambdaQueryWrapper<>();
        flowMyBusinessQueryWrapper.eq(WfMyBusiness::getDataId,dataId);
        WfMyBusiness one = wfMyBusinessService.getOne(flowMyBusinessQueryWrapper);
        if (one.getProcessInstanceId()!=null){
            try {
                wfInstanceService.delete(one.getProcessInstanceId(),"删除流程",dataId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return wfMyBusinessService.remove(flowMyBusinessQueryWrapper);
    }

}
