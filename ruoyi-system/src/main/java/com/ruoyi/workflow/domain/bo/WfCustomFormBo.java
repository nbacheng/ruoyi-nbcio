package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程业务单业务对象 wf_custom_form
 *
 * @author nbacheng
 * @date 2023-10-09
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class WfCustomFormBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 业务表单名称
     */
    @NotBlank(message = "业务表单名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String businessName;

    /**
     * 业务服务名称
     */
    @NotBlank(message = "业务服务名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String businessService;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String flowName;

    /**
     * 关联流程发布主键
     */
    @NotBlank(message = "关联流程发布主键不能为空", groups = { AddGroup.class, EditGroup.class })
    private String deployId;

    /**
     * 前端路由地址
     */
    @NotBlank(message = "前端路由地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String routeName;

    /**
     * 组件注入方法
     */
    @NotBlank(message = "组件注入方法不能为空", groups = { AddGroup.class, EditGroup.class })
    private String component;


}
