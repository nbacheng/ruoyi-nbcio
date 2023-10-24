package com.ruoyi.workflow.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @Description: 流程业务扩展表
 * @Author: nbacheng
 * @Date:   2021-11-25
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class wfMyBusinessDto implements Serializable {
    private static final long serialVersionUID = 1L;

	/**流程定义key 一个key会有多个版本的id*/
	@TableField(exist = false)
    private String processDefinitionKey;
	/**流程定义id 一个流程定义唯一*/
    @TableField(exist = false)
    private String processDefinitionId;
	/**流程业务实例id 一个流程业务唯一，本表中也唯一*/
    @TableField(exist = false)
    private String processInstanceId;
	/**流程业务简要描述*/
    @TableField(exist = false)
    private String title;
	/**业务表id，理论唯一*/
    @TableField(exist = false)
    private String dataId;
	/**业务类名，用来获取spring容器里的服务对象*/
    @TableField(exist = false)
    private String serviceImplName;
	/**申请人*/
    @TableField(exist = false)
    private String proposer;
	/**流程状态说明，有：启动  撤回  驳回  审批中  审批通过  审批异常*/
    @TableField(exist = false)
    private String actStatus;
	/**当前的节点实例上的Id*/
    @TableField(exist = false)
    private String taskId;
	/**当前的节点*/
    @TableField(exist = false)
    private String taskName;
	/**当前的节点定义上的Id*/
    @TableField(exist = false)
    private String taskNameId;
	/**当前的节点可以处理的用户名，为username的集合json字符串*/
    @TableField(exist = false)
    private String todoUsers;
	/**处理过的人,为username的集合json字符串*/
    @TableField(exist = false)
    private String doneUsers;
	/**当前任务节点的优先级 流程定义的时候所填*/
    @TableField(exist = false)
    private String priority;
	/**流程变量*/
	@TableField(exist = false)
    private Map<String,Object> values;
	/**前端页面显示的路由地址，理论唯一*/
	@TableField(exist = false)
    private String routeName;

    @TableField(exist = false)
    private String deployId;
}
