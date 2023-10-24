package com.ruoyi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.annotation.ExcelDictFormat;
import com.ruoyi.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 流程业务扩展视图对象 wf_my_business
 *
 * @author nbacheng
 * @date 2023-10-11
 */
@Data
@ExcelIgnoreUnannotated
public class WfMyBusinessVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 流程定义key 一个key会有多个版本的id
     */
    @ExcelProperty(value = "流程定义key 一个key会有多个版本的id")
    private String processDefinitionKey;

    /**
     * 流程定义id 一个流程定义唯一
     */
    @ExcelProperty(value = "流程定义id 一个流程定义唯一")
    private String processDefinitionId;

    /**
     * 流程业务实例id 一个流程业务唯一，本表中也唯一
     */
    @ExcelProperty(value = "流程业务实例id 一个流程业务唯一，本表中也唯一")
    private String processInstanceId;

    /**
     * 流程业务简要描述
     */
    @ExcelProperty(value = "流程业务简要描述")
    private String title;

    /**
     * 业务表id，理论唯一
     */
    @ExcelProperty(value = "业务表id，理论唯一")
    private String dataId;

    /**
     * 业务类名，用来获取spring容器里的服务对象
     */
    @ExcelProperty(value = "业务类名，用来获取spring容器里的服务对象")
    private String serviceImplName;

    /**
     * 申请人
     */
    @ExcelProperty(value = "申请人")
    private String proposer;

    /**
     * 流程状态说明，有：启动  撤回  驳回  审批中  审批通过  审批异常
     */
    @ExcelProperty(value = "流程状态说明，有：启动  撤回  驳回  审批中  审批通过  审批异常")
    private String actStatus;

    /**
     * 当前的节点定义上的Id,
     */
    @ExcelProperty(value = "当前的节点定义上的Id,")
    private String taskId;

    /**
     * 当前的节点
     */
    @ExcelProperty(value = "当前的节点")
    private String taskName;

    /**
     * 当前的节点实例上的Id
     */
    @ExcelProperty(value = "当前的节点实例上的Id")
    private String taskNameId;

    /**
     * 当前的节点可以处理的用户名
     */
    @ExcelProperty(value = "当前的节点可以处理的用户名")
    private String todoUsers;

    /**
     * 处理过的人
     */
    @ExcelProperty(value = "处理过的人")
    private String doneUsers;

    /**
     * 当前任务节点的优先级 流程定义的时候所填
     */
    @ExcelProperty(value = "当前任务节点的优先级 流程定义的时候所填")
    private String priority;

    /**
     * 前端页面显示的路由地址
     */
    @ExcelProperty(value = "前端页面显示的路由地址")
    private String routeName;

    /**
     * 流程实例主键
     */
    @ExcelProperty(value = "流程实例主键")
    private String deployId;


}
