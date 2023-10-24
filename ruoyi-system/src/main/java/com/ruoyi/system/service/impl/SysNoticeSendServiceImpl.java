package com.ruoyi.system.service.impl;

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
import com.ruoyi.system.domain.bo.SysNoticeSendBo;
import com.ruoyi.system.domain.vo.SysNoticeSendVo;
import com.ruoyi.system.domain.SysNoticeSend;
import com.ruoyi.system.mapper.SysNoticeSendMapper;
import com.ruoyi.system.model.NoticeSendModel;
import com.ruoyi.system.service.ISysNoticeSendService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户公告阅读标记Service业务层处理
 *
 * @author nbacheng
 * @date 2023-09-21
 */
@RequiredArgsConstructor
@Service
public class SysNoticeSendServiceImpl extends ServiceImpl<SysNoticeSendMapper, SysNoticeSend> implements ISysNoticeSendService {

    private final SysNoticeSendMapper baseMapper;

    /**
     * 查询用户公告阅读标记
     */
    @Override
    public SysNoticeSendVo queryById(Long sendId){
        return baseMapper.selectVoById(sendId);
    }

    /**
     * 查询用户公告阅读标记列表
     */
    @Override
    public TableDataInfo<SysNoticeSendVo> queryPageList(SysNoticeSendBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysNoticeSend> lqw = buildQueryWrapper(bo);
        Page<SysNoticeSendVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询用户公告阅读标记列表
     */
    @Override
    public List<SysNoticeSendVo> queryList(SysNoticeSendBo bo) {
        LambdaQueryWrapper<SysNoticeSend> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysNoticeSend> buildQueryWrapper(SysNoticeSendBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysNoticeSend> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getSendId() != null, SysNoticeSend::getSendId, bo.getSendId());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeId().toString()), SysNoticeSend::getNoticeId, bo.getNoticeId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserId().toString()), SysNoticeSend::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getReadFlag()), SysNoticeSend::getReadFlag, bo.getReadFlag());
        lqw.eq(bo.getReadTime() != null, SysNoticeSend::getReadTime, bo.getReadTime());
        return lqw;
    }

    /**
     * 新增用户公告阅读标记
     */
    @Override
    public Boolean insertByBo(SysNoticeSendBo bo) {
        SysNoticeSend add = BeanUtil.toBean(bo, SysNoticeSend.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setSendId(add.getSendId());
        }
        return flag;
    }

    /**
     * 修改用户公告阅读标记
     */
    @Override
    public Boolean updateByBo(SysNoticeSendBo bo) {
        SysNoticeSend update = BeanUtil.toBean(bo, SysNoticeSend.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysNoticeSend entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除用户公告阅读标记
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

	@Override
	public Page<NoticeSendModel> getMyNoticeSendPage(Page<NoticeSendModel> pageList, NoticeSendModel noticeSendModel) {
		return pageList.setRecords(baseMapper.getMyNoticeSendList(pageList, noticeSendModel));
	}
}
