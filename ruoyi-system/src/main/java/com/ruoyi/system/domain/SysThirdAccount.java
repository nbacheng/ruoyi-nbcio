package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 sys_third_account
 *
 * @author nbacheng
 * @date 2023-09-30
 */
@Data
@TableName("sys_third_account")
public class SysThirdAccount  {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 第三方登录id
     */
    private String sysUserId;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 状态(1-正常,2-冻结)
     */
    private Integer status;
    /**
     * 删除状态(0-正常,1-已删除)
     */
    @TableLogic
    private Integer delFlag;
    /**
     * 真实姓名
     */
    private String realname;
    /**
     * 第三方账号
     */
    private String thirdUserUuid;
    /**
     * 第三方app用户账号
     */
    private String thirdUserId;
    /**
     * 登录来源
     */
    private String thirdType;
    /**
     * 是否star了项目
     */
    private Integer star;

}
