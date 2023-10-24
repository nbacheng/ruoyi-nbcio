package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SysNoticeSend;
import com.ruoyi.system.domain.vo.SysNoticeSendVo;
import com.ruoyi.system.model.NoticeSendModel;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.mapper.BaseMapperPlus;

/**
 * 用户公告阅读标记Mapper接口
 *
 * @author nbacheng
 * @date 2023-09-21
 */
public interface SysNoticeSendMapper extends BaseMapperPlus<SysNoticeSendMapper, SysNoticeSend, SysNoticeSendVo> {

	List<NoticeSendModel> getMyNoticeSendList(Page<NoticeSendModel> pageList, @Param("noticeSendModel")NoticeSendModel noticeSendModel);

}
