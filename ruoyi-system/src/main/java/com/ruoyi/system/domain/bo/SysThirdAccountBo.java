package com.ruoyi.system.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 sys_third_account
 *
 * @author nbacheng
 * @date 2023-09-30
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysThirdAccountBo extends BaseEntity {

    /**
     * 编号
     */
    @NotNull(message = "编号不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 第三方登录id
     */
    @NotBlank(message = "第三方登录id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sysUserId;

    /**
     * 头像
     */
    @NotBlank(message = "头像不能为空", groups = { AddGroup.class, EditGroup.class })
    private String avatar;

    /**
     * 状态(1-正常,2-冻结)
     */
    @NotNull(message = "状态(1-正常,2-冻结)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String realname;

    /**
     * 第三方账号
     */
    @NotBlank(message = "第三方账号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdUserUuid;

    /**
     * 第三方app用户账号
     */
    @NotBlank(message = "第三方app用户账号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdUserId;

    /**
     * 登录来源
     */
    @NotBlank(message = "登录来源不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdType;

    /**
     * 是否star了项目
     */
    @NotNull(message = "是否star了项目不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer star;


}
