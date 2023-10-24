package com.ruoyi.workflow.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程业务单对象 wf_custom_form
 *
 * @author nbacheng
 * @date 2023-10-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_custom_form")
public class WfCustomForm extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 业务表单名称
     */
    private String businessName;
    /**
     * 业务服务名称
     */
    private String businessService;
    /**
     * 
     */
    private String flowName;
    /**
     * 关联流程发布主键
     */
    private String deployId;
    /**
     * 前端路由地址
     */
    private String routeName;
    /**
     * 组件注入方法
     */
    private String component;

}
