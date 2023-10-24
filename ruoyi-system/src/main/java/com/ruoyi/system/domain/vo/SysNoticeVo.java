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
 * 通知公告视图对象 sys_notice
 *
 * @author ruoyi
 * @date 2023-09-21
 */
@Data
@ExcelIgnoreUnannotated
public class SysNoticeVo {

    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @ExcelProperty(value = "公告ID")
    private Long noticeId;

    /**
     * 公告标题
     */
    @ExcelProperty(value = "公告标题")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告 3待办）
     */
    @ExcelProperty(value = "公告类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=通知,2=公告,3=待办")
    private String noticeType;

    /**
     * 公告内容
     */
    @ExcelProperty(value = "公告内容")
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    @ExcelProperty(value = "公告状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=关闭")
    private String status;

    /**
     * 发布人
     */
    @ExcelProperty(value = "发布人")
    private Long sender;

    /**
     * 优先级（L低，M中，H高）
     */
    @ExcelProperty(value = "优先级", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "L=低，M中，H高")
    private String priority;

    /**
     * 通告对象类型（USER:指定用户，ALL:全体用户）
     */
    @ExcelProperty(value = "通告对象类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "U=SER:指定用户，ALL:全体用户")
    private String msgType;

    /**
     * 发布状态（0未发布，1已发布，2已撤销）
     */
    @ExcelProperty(value = "发布状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=未发布，1已发布，2已撤销")
    private String sendStatus;

    /**
     * 发布时间
     */
    @ExcelProperty(value = "发布时间")
    private Date sendTime;

    /**
     * 撤销时间
     */
    @ExcelProperty(value = "撤销时间")
    private Date cancelTime;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
