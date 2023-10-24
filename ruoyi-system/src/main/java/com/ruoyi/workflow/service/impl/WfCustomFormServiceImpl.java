package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.bo.WfCustomFormBo;
import com.ruoyi.workflow.domain.vo.CustomFormVo;
import com.ruoyi.workflow.domain.vo.WfCustomFormVo;
import com.ruoyi.workflow.domain.WfCustomForm;
import com.ruoyi.workflow.mapper.WfCustomFormMapper;
import com.ruoyi.workflow.service.IWfCustomFormService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 流程业务单Service业务层处理
 *
 * @author nbacheng
 * @date 2023-10-09
 */
@RequiredArgsConstructor
@Service
public class WfCustomFormServiceImpl implements IWfCustomFormService {

    private final WfCustomFormMapper baseMapper;

    /**
     * 查询流程业务单
     */
    @Override
    public WfCustomFormVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询流程业务单列表
     */
    @Override
    public TableDataInfo<WfCustomFormVo> queryPageList(WfCustomFormBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<WfCustomForm> lqw = buildQueryWrapper(bo);
        Page<WfCustomFormVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询流程业务单列表
     */
    @Override
    public List<WfCustomFormVo> queryList(WfCustomFormBo bo) {
        LambdaQueryWrapper<WfCustomForm> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<WfCustomForm> buildQueryWrapper(WfCustomFormBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<WfCustomForm> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getBusinessName()), WfCustomForm::getBusinessName, bo.getBusinessName());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessService()), WfCustomForm::getBusinessService, bo.getBusinessService());
        lqw.like(StringUtils.isNotBlank(bo.getFlowName()), WfCustomForm::getFlowName, bo.getFlowName());
        lqw.eq(StringUtils.isNotBlank(bo.getDeployId()), WfCustomForm::getDeployId, bo.getDeployId());
        lqw.like(StringUtils.isNotBlank(bo.getRouteName()), WfCustomForm::getRouteName, bo.getRouteName());
        lqw.eq(StringUtils.isNotBlank(bo.getComponent()), WfCustomForm::getComponent, bo.getComponent());
        return lqw;
    }

    /**
     * 新增流程业务单
     */
    @Override
    public Boolean insertByBo(WfCustomFormBo bo) {
        WfCustomForm add = BeanUtil.toBean(bo, WfCustomForm.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程业务单
     */
    @Override
    public Boolean updateByBo(WfCustomFormBo bo) {
        WfCustomForm update = BeanUtil.toBean(bo, WfCustomForm.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(WfCustomForm entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除流程业务单
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

	@Override
	public void updateCustom(CustomFormVo customFormVo) {
		baseMapper.updateCustom(customFormVo);
	}

	@Override
	public WfCustomForm selectSysCustomFormByServiceName(String serviceName) {
		return baseMapper.selectSysCustomFormByServiceName(serviceName);
	}
}
