package com.ruoyi.workflow.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.math.BigDecimal;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程业务扩展对象 wf_my_business
 *
 * @author nbacheng
 * @date 2023-10-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_my_business")
public class WfMyBusiness extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 流程定义key 一个key会有多个版本的id
     */
    private String processDefinitionKey;
    /**
     * 流程定义id 一个流程定义唯一
     */
    private String processDefinitionId;
    /**
     * 流程业务实例id 一个流程业务唯一，本表中也唯一
     */
    private String processInstanceId;
    /**
     * 流程业务简要描述
     */
    private String title;
    /**
     * 业务表id，理论唯一
     */
    private String dataId;
    /**
     * 业务类名，用来获取spring容器里的服务对象
     */
    private String serviceImplName;
    /**
     * 申请人
     */
    private String proposer;
    /**
     * 流程状态说明，有：启动  撤回  驳回  审批中  审批通过  审批异常
     */
    private String actStatus;
    /**
     * 当前的节点定义上的Id,
     */
    private String taskId;
    /**
     * 当前的节点
     */
    private String taskName;
    /**
     * 当前的节点实例上的Id
     */
    private String taskNameId;
    /**
     * 当前的节点可以处理的用户名
     */
    private String todoUsers;
    /**
     * 处理过的人
     */
    private String doneUsers;
    /**
     * 当前任务节点的优先级 流程定义的时候所填
     */
    private String priority;
    /**
     * 前端页面显示的路由地址
     */
    private String routeName;
    
    /**流程变量*/
	@TableField(exist = false)
    private Map<String,Object> values;
	
    /**
     * 流程实例主键
     */
    private String deployId;

}
