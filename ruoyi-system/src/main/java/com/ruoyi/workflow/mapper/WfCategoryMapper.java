package com.ruoyi.workflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ruoyi.common.core.mapper.BaseMapperPlus;
import com.ruoyi.workflow.domain.WfCategory;
import com.ruoyi.workflow.domain.vo.WfAppTypeVo;
import com.ruoyi.workflow.domain.vo.WfCategoryVo;

/**
 * 流程分类Mapper接口
 *
 * @author KonBAI
 * @date 2022-01-15
 */
public interface WfCategoryMapper extends BaseMapperPlus<WfCategoryMapper, WfCategory, WfCategoryVo> {
   String selectAppTypeByCode(@Param("code") String code);
   WfAppTypeVo selectAppTypeVoByCode(@Param("code") String code);
}
