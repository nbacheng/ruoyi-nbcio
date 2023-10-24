package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.bo.WfDemoBo;
import com.ruoyi.workflow.domain.vo.WfDemoVo;
import com.ruoyi.workflow.domain.WfDemo;
import com.ruoyi.workflow.domain.WfMyBusiness;
import com.ruoyi.workflow.mapper.WfDemoMapper;
import com.ruoyi.workflow.service.IWfDemoService;
import com.ruoyi.workflow.service.WfCallBackServiceI;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * DEMOService业务层处理
 *
 * @author nbacheng
 * @date 2023-10-12
 */
@RequiredArgsConstructor
@Service("wfDemoService")
public class WfDemoServiceImpl extends ServiceImpl<WfDemoMapper, WfDemo> implements IWfDemoService, WfCallBackServiceI {

    private final WfDemoMapper baseMapper;

    /**
     * 查询DEMO
     */
    @Override
    public WfDemoVo queryById(Long demoId){
        return baseMapper.selectVoById(demoId);
    }

    /**
     * 查询DEMO列表
     */
    @Override
    public TableDataInfo<WfDemoVo> queryPageList(WfDemoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<WfDemo> lqw = buildQueryWrapper(bo);
        Page<WfDemoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询DEMO列表
     */
    @Override
    public List<WfDemoVo> queryList(WfDemoBo bo) {
        LambdaQueryWrapper<WfDemo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<WfDemo> buildQueryWrapper(WfDemoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<WfDemo> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getUserName()), WfDemo::getUserName, bo.getUserName());
        lqw.like(StringUtils.isNotBlank(bo.getNickName()), WfDemo::getNickName, bo.getNickName());
        lqw.eq(StringUtils.isNotBlank(bo.getEmail()), WfDemo::getEmail, bo.getEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getAvatar()), WfDemo::getAvatar, bo.getAvatar());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), WfDemo::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增DEMO
     */
    @Override
    public Boolean insertByBo(WfDemoBo bo) {
        WfDemo add = BeanUtil.toBean(bo, WfDemo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setDemoId(add.getDemoId());
        }
        return flag;
    }

    /**
     * 修改DEMO
     */
    @Override
    public Boolean updateByBo(WfDemoBo bo) {
        WfDemo update = BeanUtil.toBean(bo, WfDemo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(WfDemo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除DEMO
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public Page<WfDemoVo> myPage(Page<WfDemoVo> page, QueryWrapper<WfDemoVo> queryWrapper) {
        return baseMapper.myPage(page, queryWrapper);
    }
    
	@Override
	public void afterFlowHandle(WfMyBusiness business) {
		//流程操作后做些什么
        business.getTaskNameId();//接下来审批的节点
        business.getValues();//前端传进来的参数
        business.getActStatus();//流程状态 ActStatus.java
        //....其他
		
	}

	@Override
	public Object getBusinessDataById(String dataId) {
		 return this.getById(dataId);
	}

	@Override
	public Map<String, Object> flowValuesOfTask(String taskNameId, Map<String, Object> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> flowCandidateUsernamesOfTask(String taskNameId, Map<String, Object> values) {
		// TODO Auto-generated method stub
		return null;
	}
}
