package com.ruoyi.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.mapper.BaseMapperPlus;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.vo.SysNoticeVo;

/**
 * 通知公告Mapper接口
 *
 * @author nbacheng
 * @date 2023-09-21
 */
public interface SysNoticeMapper extends BaseMapperPlus<SysNoticeMapper, SysNotice, SysNoticeVo> {

	List<SysNotice> querySysNoticeListByUserId(Page<SysNotice> page, @Param("userId")String userId,@Param("msgCategory")String msgCategory);

}
