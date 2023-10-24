package com.ruoyi.flowable.core.domain.dto;

import lombok.Data;
import org.flowable.bpmn.model.UserTask;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;

import java.io.Serializable;
import java.util.List;

/**
 * 人员、组
 */
@Data
public class FlowNextDto implements Serializable {
	

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String type;

    private String vars;
	
    /**
     * 节点对象
     */
    private UserTask userTask;
    /**
     * 待办人员
     */
    private List<SysUser> userList;
    
    private List<SysRole> roleList;
    
    //会签是否结束标志
    private boolean bmutiInstanceFinish=false;
    
    //是否串行会签
    private boolean bisSequential;

}
