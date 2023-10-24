package com.ruoyi.workflow.mapper;

import com.ruoyi.workflow.domain.WfCustomForm;
import com.ruoyi.workflow.domain.vo.CustomFormVo;
import com.ruoyi.workflow.domain.vo.WfCustomFormVo;

import org.apache.ibatis.annotations.Param;

import com.ruoyi.common.core.mapper.BaseMapperPlus;

/**
 * 流程业务单Mapper接口
 *
 * @author nbacheng
 * @date 2023-10-09
 */
public interface WfCustomFormMapper extends BaseMapperPlus<WfCustomFormMapper, WfCustomForm, WfCustomFormVo> {
	void updateCustom(@Param("customFormVo") CustomFormVo customFormVo);
	WfCustomForm selectSysCustomFormById(String formId);
	WfCustomForm selectSysCustomFormByServiceName(String serviceName);
}
