package com.ruoyi.web.controller.workflow;

import java.util.List;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.workflow.domain.vo.WfMyBusinessVo;
import com.ruoyi.workflow.domain.bo.WfMyBusinessBo;
import com.ruoyi.workflow.service.IWfMyBusinessService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 流程业务扩展
 *
 * @author nbacheng
 * @date 2023-10-11
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/myBusiness")
public class WfMyBusinessController extends BaseController {

    private final IWfMyBusinessService iWfMyBusinessService;

    /**
     * 查询流程业务扩展列表
     */
    @SaCheckPermission("workflow:myBusiness:list")
    @GetMapping("/list")
    public TableDataInfo<WfMyBusinessVo> list(WfMyBusinessBo bo, PageQuery pageQuery) {
        return iWfMyBusinessService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出流程业务扩展列表
     */
    @SaCheckPermission("workflow:myBusiness:export")
    @Log(title = "流程业务扩展", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(WfMyBusinessBo bo, HttpServletResponse response) {
        List<WfMyBusinessVo> list = iWfMyBusinessService.queryList(bo);
        ExcelUtil.exportExcel(list, "流程业务扩展", WfMyBusinessVo.class, response);
    }

    /**
     * 获取流程业务扩展详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("workflow:myBusiness:query")
    @GetMapping("/{id}")
    public R<WfMyBusinessVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iWfMyBusinessService.queryById(id));
    }

    /**
     * 新增流程业务扩展
     */
    @SaCheckPermission("workflow:myBusiness:add")
    @Log(title = "流程业务扩展", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WfMyBusinessBo bo) {
        return toAjax(iWfMyBusinessService.insertByBo(bo));
    }

    /**
     * 修改流程业务扩展
     */
    @SaCheckPermission("workflow:myBusiness:edit")
    @Log(title = "流程业务扩展", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WfMyBusinessBo bo) {
        return toAjax(iWfMyBusinessService.updateByBo(bo));
    }

    /**
     * 删除流程业务扩展
     *
     * @param ids 主键串
     */
    @SaCheckPermission("workflow:myBusiness:remove")
    @Log(title = "流程业务扩展", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iWfMyBusinessService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
