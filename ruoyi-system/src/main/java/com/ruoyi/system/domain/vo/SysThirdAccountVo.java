package com.ruoyi.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.annotation.ExcelDictFormat;
import com.ruoyi.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 【请填写功能名称】视图对象 sys_third_account
 *
 * @author nbacheng
 * @date 2023-09-30
 */
@Data
@ExcelIgnoreUnannotated
public class SysThirdAccountVo {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @ExcelProperty(value = "编号")
    private Long id;

    /**
     * 第三方登录id
     */
    @ExcelProperty(value = "第三方登录id")
    private String sysUserId;

    /**
     * 头像
     */
    @ExcelProperty(value = "头像")
    private String avatar;

    /**
     * 状态(1-正常,2-冻结)
     */
    @ExcelProperty(value = "状态(1-正常,2-冻结)")
    private Integer status;

    /**
     * 真实姓名
     */
    @ExcelProperty(value = "真实姓名")
    private String realname;

    /**
     * 第三方账号
     */
    @ExcelProperty(value = "第三方账号")
    private String thirdUserUuid;

    /**
     * 第三方app用户账号
     */
    @ExcelProperty(value = "第三方app用户账号")
    private String thirdUserId;

    /**
     * 登录来源
     */
    @ExcelProperty(value = "登录来源")
    private String thirdType;

    /**
     * 是否star了项目
     */
    @ExcelProperty(value = "是否star了项目")
    private Integer star;


}
