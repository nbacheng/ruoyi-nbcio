package com.ruoyi.workflow.service;

import com.ruoyi.workflow.domain.vo.WfMyBusinessVo;
import com.ruoyi.workflow.domain.WfMyBusiness;
import com.ruoyi.workflow.domain.bo.WfMyBusinessBo;
import com.ruoyi.common.core.page.TableDataInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 流程业务扩展Service接口
 *
 * @author nbacheng
 * @date 2023-10-11
 */
public interface IWfMyBusinessService extends IService<WfMyBusiness> {

    /**
     * 查询流程业务扩展
     */
    WfMyBusinessVo queryById(Long id);

    /**
     * 查询流程业务扩展列表
     */
    TableDataInfo<WfMyBusinessVo> queryPageList(WfMyBusinessBo bo, PageQuery pageQuery);

    /**
     * 查询流程业务扩展列表
     */
    List<WfMyBusinessVo> queryList(WfMyBusinessBo bo);

    /**
     * 新增流程业务扩展
     */
    Boolean insertByBo(WfMyBusinessBo bo);

    /**
     * 修改流程业务扩展
     */
    Boolean updateByBo(WfMyBusinessBo bo);

    /**
     * 校验并批量删除流程业务扩展信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

}
