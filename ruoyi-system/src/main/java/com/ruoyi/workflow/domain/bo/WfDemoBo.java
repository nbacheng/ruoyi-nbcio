package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * DEMO业务对象 wf_demo
 *
 * @author nbacheng
 * @date 2023-10-12
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class WfDemoBo extends BaseEntity {

    /**
     * DEMO-ID
     */
    @NotNull(message = "DEMO-ID不能为空", groups = { EditGroup.class })
    private Long demoId;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空", groups = { AddGroup.class, EditGroup.class })
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
     * 备注
     */
    private String remark;


}
