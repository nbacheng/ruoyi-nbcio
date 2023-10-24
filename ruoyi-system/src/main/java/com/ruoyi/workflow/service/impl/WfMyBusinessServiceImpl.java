package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.bo.WfMyBusinessBo;
import com.ruoyi.workflow.domain.vo.WfMyBusinessVo;
import com.ruoyi.workflow.domain.WfMyBusiness;
import com.ruoyi.workflow.mapper.WfMyBusinessMapper;
import com.ruoyi.workflow.service.IWfMyBusinessService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * @Description: 流程业务扩展表
 * @Author: nbacheng
 * @Date:   2021-11-25
 * @Version: V1.0
 */
@RequiredArgsConstructor
@Service
public class WfMyBusinessServiceImpl extends ServiceImpl<WfMyBusinessMapper, WfMyBusiness> implements IWfMyBusinessService {

    private final WfMyBusinessMapper baseMapper;

    public WfMyBusiness getByDataId(String dataId) {
        LambdaQueryWrapper<WfMyBusiness> flowMyBusinessLambdaQueryWrapper = new LambdaQueryWrapper<>();
        flowMyBusinessLambdaQueryWrapper.eq(WfMyBusiness::getDataId,dataId)
        ;
        //如果保存数据前未调用必调的FlowCommonService.initActBusiness方法，就会有问题
        WfMyBusiness business = this.getOne(flowMyBusinessLambdaQueryWrapper);
        return business;
    }
    
    /**
     * 查询流程业务扩展
     */
    @Override
    public WfMyBusinessVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询流程业务扩展列表
     */
    @Override
    public TableDataInfo<WfMyBusinessVo> queryPageList(WfMyBusinessBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<WfMyBusiness> lqw = buildQueryWrapper(bo);
        Page<WfMyBusinessVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询流程业务扩展列表
     */
    @Override
    public List<WfMyBusinessVo> queryList(WfMyBusinessBo bo) {
        LambdaQueryWrapper<WfMyBusiness> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<WfMyBusiness> buildQueryWrapper(WfMyBusinessBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<WfMyBusiness> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getProcessDefinitionKey()), WfMyBusiness::getProcessDefinitionKey, bo.getProcessDefinitionKey());
        lqw.eq(StringUtils.isNotBlank(bo.getProcessDefinitionId()), WfMyBusiness::getProcessDefinitionId, bo.getProcessDefinitionId());
        lqw.eq(StringUtils.isNotBlank(bo.getProcessInstanceId()), WfMyBusiness::getProcessInstanceId, bo.getProcessInstanceId());
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), WfMyBusiness::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getDataId()), WfMyBusiness::getDataId, bo.getDataId());
        lqw.like(StringUtils.isNotBlank(bo.getServiceImplName()), WfMyBusiness::getServiceImplName, bo.getServiceImplName());
        lqw.eq(StringUtils.isNotBlank(bo.getProposer()), WfMyBusiness::getProposer, bo.getProposer());
        lqw.eq(StringUtils.isNotBlank(bo.getActStatus()), WfMyBusiness::getActStatus, bo.getActStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskId()), WfMyBusiness::getTaskId, bo.getTaskId());
        lqw.like(StringUtils.isNotBlank(bo.getTaskName()), WfMyBusiness::getTaskName, bo.getTaskName());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskNameId()), WfMyBusiness::getTaskNameId, bo.getTaskNameId());
        lqw.eq(StringUtils.isNotBlank(bo.getTodoUsers()), WfMyBusiness::getTodoUsers, bo.getTodoUsers());
        lqw.eq(StringUtils.isNotBlank(bo.getDoneUsers()), WfMyBusiness::getDoneUsers, bo.getDoneUsers());
        lqw.eq(StringUtils.isNotBlank(bo.getPriority()), WfMyBusiness::getPriority, bo.getPriority());
        lqw.like(StringUtils.isNotBlank(bo.getRouteName()), WfMyBusiness::getRouteName, bo.getRouteName());
        lqw.eq(StringUtils.isNotBlank(bo.getDeployId()), WfMyBusiness::getDeployId, bo.getDeployId());
        return lqw;
    }

    /**
     * 新增流程业务扩展
     */
    @Override
    public Boolean insertByBo(WfMyBusinessBo bo) {
        WfMyBusiness add = BeanUtil.toBean(bo, WfMyBusiness.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程业务扩展
     */
    @Override
    public Boolean updateByBo(WfMyBusinessBo bo) {
        WfMyBusiness update = BeanUtil.toBean(bo, WfMyBusiness.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(WfMyBusiness entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除流程业务扩展
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
