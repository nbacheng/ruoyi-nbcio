package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


/**
 * 通知公告表 sys_notice
 *
 * @author nbacheng
 * @date 2023-09-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    /**
     * 公告ID
     */
    @TableId(value = "notice_id", type = IdType.AUTO)
    private Long noticeId;

    /**
     * 公告标题
     */
    @Xss(message = "公告标题不能包含脚本字符")
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 0, max = 50, message = "公告标题不能超过{max}个字符")
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告 3待办）
     */
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;
    /**
     * 发布人
     */
    private Long sender;
    /**
     * 优先级（L低，M中，H高）
     */
    private String priority;
    /**
     * 通告对象类型（USER:指定用户，ALL:全体用户）
     */
    private String msgType;
    /**
     * 发布状态（0未发布，1已发布，2已撤销）
     */
    private String sendStatus;
    /**
     * 发布时间
     */
    private Date sendTime;
    /**
     * 撤销时间
     */
    private Date cancelTime;
    /**
     * 备注
     */
    private String remark;

}
