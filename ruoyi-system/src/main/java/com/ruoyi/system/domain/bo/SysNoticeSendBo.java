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
 * 用户公告阅读标记业务对象 sys_notice_send
 *
 * @author nbacheng
 * @date 2023-09-21
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeSendBo extends BaseEntity {

    /**
     * 公告发送ID
     */
    @NotNull(message = "公告发送ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sendId;

    /**
     * 公告ID
     */
    @NotBlank(message = "公告ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long noticeId;

    /**
     * 用户id
     */
    @NotBlank(message = "用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 阅读状态（0未读，1已读）
     */
    @NotBlank(message = "阅读状态（0未读，1已读）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String readFlag;

    /**
     * 阅读时间
     */
    @NotNull(message = "阅读时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date readTime;


}
