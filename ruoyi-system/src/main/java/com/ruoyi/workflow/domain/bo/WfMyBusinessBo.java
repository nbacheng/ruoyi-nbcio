package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程业务扩展业务对象 wf_my_business
 *
 * @author nbacheng
 * @date 2023-10-11
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class WfMyBusinessBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 流程定义key 一个key会有多个版本的id
     */
    @NotBlank(message = "流程定义key 一个key会有多个版本的id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processDefinitionKey;

    /**
     * 流程定义id 一个流程定义唯一
     */
    @NotBlank(message = "流程定义id 一个流程定义唯一不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processDefinitionId;

    /**
     * 流程业务实例id 一个流程业务唯一，本表中也唯一
     */
    @NotBlank(message = "流程业务实例id 一个流程业务唯一，本表中也唯一不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processInstanceId;

    /**
     * 流程业务简要描述
     */
    @NotBlank(message = "流程业务简要描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 业务表id，理论唯一
     */
    @NotBlank(message = "业务表id，理论唯一不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dataId;

    /**
     * 业务类名，用来获取spring容器里的服务对象
     */
    @NotBlank(message = "业务类名，用来获取spring容器里的服务对象不能为空", groups = { AddGroup.class, EditGroup.class })
    private String serviceImplName;

    /**
     * 申请人
     */
    @NotBlank(message = "申请人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String proposer;

    /**
     * 流程状态说明，有：启动  撤回  驳回  审批中  审批通过  审批异常
     */
    @NotBlank(message = "流程状态说明，有：启动  撤回  驳回  审批中  审批通过  审批异常不能为空", groups = { AddGroup.class, EditGroup.class })
    private String actStatus;

    /**
     * 当前的节点定义上的Id,
     */
    @NotBlank(message = "当前的节点定义上的Id,不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskId;

    /**
     * 当前的节点
     */
    @NotBlank(message = "当前的节点不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskName;

    /**
     * 当前的节点实例上的Id
     */
    @NotBlank(message = "当前的节点实例上的Id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskNameId;

    /**
     * 当前的节点可以处理的用户名
     */
    @NotBlank(message = "当前的节点可以处理的用户名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String todoUsers;

    /**
     * 处理过的人
     */
    @NotBlank(message = "处理过的人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String doneUsers;

    /**
     * 当前任务节点的优先级 流程定义的时候所填
     */
    @NotBlank(message = "当前任务节点的优先级 流程定义的时候所填不能为空", groups = { AddGroup.class, EditGroup.class })
    private String priority;

    /**
     * 前端页面显示的路由地址
     */
    @NotBlank(message = "前端页面显示的路由地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String routeName;

    /**
     * 流程实例主键
     */
    @NotBlank(message = "流程实例主键不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deployId;


}
