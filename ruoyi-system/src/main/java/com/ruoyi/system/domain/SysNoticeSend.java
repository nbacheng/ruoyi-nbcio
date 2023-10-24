package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户公告阅读标记对象 sys_notice_send
 *
 * @author ruoyi
 * @date 2023-09-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice_send")
public class SysNoticeSend extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 公告发送ID
     */
    @TableId(value = "send_id", type = IdType.AUTO)
    private Long sendId;
    /**
     * 公告ID
     */
    private Long noticeId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 阅读状态（0未读，1已读）
     */
    private String readFlag;
    /**
     * 阅读时间
     */
    private Date readTime;

}
