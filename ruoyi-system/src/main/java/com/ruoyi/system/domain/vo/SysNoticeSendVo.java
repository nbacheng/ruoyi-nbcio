package com.ruoyi.system.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.common.annotation.ExcelDictFormat;
import com.ruoyi.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 用户公告阅读标记视图对象 sys_notice_send
 *
 * @author ruoyi
 * @date 2023-09-21
 */
@Data
@ExcelIgnoreUnannotated
public class SysNoticeSendVo {

    private static final long serialVersionUID = 1L;

    /**
     * 公告发送ID
     */
    @ExcelProperty(value = "公告发送ID")
    private Long sendId;

    /**
     * 公告ID
     */
    @ExcelProperty(value = "公告ID")
    private Long noticeId;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     * 阅读状态（0未读，1已读）
     */
    @ExcelProperty(value = "阅读状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=未读，1已读")
    private String readFlag;

    /**
     * 阅读时间
     */
    @ExcelProperty(value = "阅读时间")
    private Date readTime;


}
