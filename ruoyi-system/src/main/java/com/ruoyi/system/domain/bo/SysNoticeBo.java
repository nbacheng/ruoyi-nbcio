package com.ruoyi.system.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 通知公告业务对象 sys_notice
 *
 * @author ruoyi
 * @date 2023-09-21
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeBo extends BaseEntity {

    /**
     * 公告ID
     */
    @NotNull(message = "公告ID不能为空", groups = { EditGroup.class })
    private Long noticeId;

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告 3待办）
     */
    @NotBlank(message = "公告类型（1通知 2公告 3待办）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String noticeType;

    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空", groups = { AddGroup.class, EditGroup.class })
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    @NotBlank(message = "公告状态（0正常 1关闭）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     * 发布人
     */
    @NotBlank(message = "发布人不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sender;

    /**
     * 优先级（L低，M中，H高）
     */
    @NotBlank(message = "优先级（L低，M中，H高）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String priority;

    /**
     * 通告对象类型（USER:指定用户，ALL:全体用户）
     */
    @NotBlank(message = "通告对象类型（USER:指定用户，ALL:全体用户）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String msgType;

    /**
     * 发布状态（0未发布，1已发布，2已撤销）
     */
    @NotBlank(message = "发布状态（0未发布，1已发布，2已撤销）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sendStatus;

    /**
     * 发布时间
     */
    @NotNull(message = "发布时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date sendTime;

    /**
     * 撤销时间
     */
    @NotNull(message = "撤销时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date cancelTime;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;


}
