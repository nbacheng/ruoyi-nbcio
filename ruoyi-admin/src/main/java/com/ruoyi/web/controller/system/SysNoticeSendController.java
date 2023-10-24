package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Arrays;
import java.util.Date;

import lombok.RequiredArgsConstructor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.constant.Constants;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.vo.SysNoticeSendVo;
import com.ruoyi.system.domain.SysNoticeSend;
import com.ruoyi.system.domain.bo.SysNoticeSendBo;
import com.ruoyi.system.model.NoticeSendModel;
import com.ruoyi.system.service.ISysNoticeSendService;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.service.CommonService;

/**
 * 用户公告阅读标记
 *
 * @author nbacheng
 * @date 2023-09-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/noticeSend")
public class SysNoticeSendController extends BaseController {

    private final ISysNoticeSendService iSysNoticeSendService;
    @Resource
    private CommonService commonService;

    /**
     * 查询用户公告阅读标记列表
     */
    @SaCheckPermission("system:noticeSend:list")
    @GetMapping("/list")
    public TableDataInfo<SysNoticeSendVo> list(SysNoticeSendBo bo, PageQuery pageQuery) {
        return iSysNoticeSendService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户公告阅读标记列表
     */
    @SaCheckPermission("system:noticeSend:export")
    @Log(title = "用户公告阅读标记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SysNoticeSendBo bo, HttpServletResponse response) {
        List<SysNoticeSendVo> list = iSysNoticeSendService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户公告阅读标记", SysNoticeSendVo.class, response);
    }

    /**
     * 获取用户公告阅读标记详细信息
     *
     * @param sendId 主键
     */
    @SaCheckPermission("system:noticeSend:query")
    @GetMapping("/{sendId}")
    public R<SysNoticeSendVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long sendId) {
        return R.ok(iSysNoticeSendService.queryById(sendId));
    }

    /**
     * 新增用户公告阅读标记
     */
    @SaCheckPermission("system:noticeSend:add")
    @Log(title = "用户公告阅读标记", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysNoticeSendBo bo) {
        return toAjax(iSysNoticeSendService.insertByBo(bo));
    }

    /**
     * 修改用户公告阅读标记
     */
    @SaCheckPermission("system:noticeSend:edit")
    @Log(title = "用户公告阅读标记", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysNoticeSendBo bo) {
        return toAjax(iSysNoticeSendService.updateByBo(bo));
    }
    
    /**
     * 更新用户公告阅读状态标记
     */
    @SaCheckPermission("system:noticeSend:edit")
    @Log(title = "更新用户公告阅读状态标记", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping(value = "/updateUserIdAndNotice")
    public R<Void> updateUserIdAndNotice(@RequestBody JSONObject json) {
		Long noticeId = json.getLong("noticeId");
		LoginUser loginUser = commonService.getLoginUser();
		Long userId = loginUser.getUserId();
		LambdaUpdateWrapper<SysNoticeSend> updateWrapper = new UpdateWrapper().lambda();
		updateWrapper.set(SysNoticeSend::getReadFlag, Constants.HAS_READ_FLAG);
		updateWrapper.set(SysNoticeSend::getReadTime, new Date());
		updateWrapper.last("where notice_id ="+noticeId+" and user_id ="+userId);
		SysNoticeSend noticeSend = new SysNoticeSend();
		iSysNoticeSendService.update(noticeSend, updateWrapper);
		return R.ok("更新用户公告阅读状态标记成功");
    }
    
    /**
	 * 获取我的消息
	 * @return
	 */
    @SaCheckPermission("system:noticeSend:list")
    @PostMapping(value = "/getMyNoticeSend")
    public  R<Page<NoticeSendModel>> getMyNoticeSend(@RequestBody JSONObject json) {	
    	LoginUser loginUser = commonService.getLoginUser();
		Long userId = loginUser.getUserId();
		Integer pageNum = json.getInteger("pageNum");
		Integer pageSize = json.getInteger("pageSize");	
		NoticeSendModel noticeSendModel = new NoticeSendModel();
		noticeSendModel.setUserId(userId);
		noticeSendModel.setPageNo((pageNum-1)*pageSize);
		noticeSendModel.setPageSize(pageSize);
		Page<NoticeSendModel> pageList = new Page<NoticeSendModel>(pageNum,pageSize);
		pageList = iSysNoticeSendService.getMyNoticeSendPage(pageList, noticeSendModel);
		return R.ok(pageList);
    }

    /**
     * 删除用户公告阅读标记
     *
     * @param sendIds 主键串
     */
    @SaCheckPermission("system:noticeSend:remove")
    @Log(title = "用户公告阅读标记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sendIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] sendIds) {
        return toAjax(iSysNoticeSendService.deleteWithValidByIds(Arrays.asList(sendIds), true));
    }
}
