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
import com.ruoyi.workflow.domain.vo.CustomFormVo;
import com.ruoyi.workflow.domain.vo.WfCustomFormVo;
import com.ruoyi.workflow.domain.bo.WfCustomFormBo;
import com.ruoyi.workflow.service.IWfCustomFormService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 流程业务单
 *
 * @author nbacheng
 * @date 2023-10-09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/customForm")
public class WfCustomFormController extends BaseController {

    private final IWfCustomFormService iWfCustomFormService;

    /**
     * 查询流程业务单列表
     */
    @SaCheckPermission("workflow:customForm:list")
    @GetMapping("/list")
    public TableDataInfo<WfCustomFormVo> list(WfCustomFormBo bo, PageQuery pageQuery) {
        return iWfCustomFormService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出流程业务单列表
     */
    @SaCheckPermission("workflow:customForm:export")
    @Log(title = "流程业务单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(WfCustomFormBo bo, HttpServletResponse response) {
        List<WfCustomFormVo> list = iWfCustomFormService.queryList(bo);
        ExcelUtil.exportExcel(list, "流程业务单", WfCustomFormVo.class, response);
    }

    /**
     * 获取流程业务单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("workflow:customForm:query")
    @GetMapping("/{id}")
    public R<WfCustomFormVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(iWfCustomFormService.queryById(id));
    }

    /**
     * 新增流程业务单
     */
    @SaCheckPermission("workflow:customForm:add")
    @Log(title = "流程业务单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WfCustomFormBo bo) {
        return toAjax(iWfCustomFormService.insertByBo(bo));
    }

    /**
     * 修改流程业务单
     */
    @SaCheckPermission("workflow:customForm:edit")
    @Log(title = "流程业务单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody WfCustomFormBo bo) {
        return toAjax(iWfCustomFormService.updateByBo(bo));
    }
    
    /**
     * 关联流程业务单
     */
    @SaCheckPermission("workflow:customForm:edit")
    @Log(title = "流程业务单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/updateCustom")
    public R<Void> updateCustom( @RequestBody CustomFormVo customFormVo) {
        iWfCustomFormService.updateCustom(customFormVo);
        return R.ok("关联流程成功!");
    }

    /**
     * 删除流程业务单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("workflow:customForm:remove")
    @Log(title = "流程业务单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(iWfCustomFormService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
