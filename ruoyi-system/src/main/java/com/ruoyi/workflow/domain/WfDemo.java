package com.ruoyi.workflow.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * DEMO对象 wf_demo
 *
 * @author nbacheng
 * @date 2023-10-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_demo")
public class WfDemo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * DEMO-ID
     */
    @TableId(value = "demo_id", type = IdType.AUTO)
    private Long demoId;
    /**
     * 用户账号
     */
    private String userName;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 头像地址
     */
    private String avatar;
    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;
    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableLogic
    private String delFlag;
    /**
     * 备注
     */
    private String remark;

}
