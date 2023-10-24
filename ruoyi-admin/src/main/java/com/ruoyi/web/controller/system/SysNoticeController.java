package com.ruoyi.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.service.CommonService;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.SysNoticeSend;
import com.ruoyi.system.mapper.SysNoticeMapper;
import com.ruoyi.system.mapper.SysNoticeSendMapper;
import com.ruoyi.system.service.ISysNoticeSendService;
import com.ruoyi.system.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告 信息操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {

    private final ISysNoticeService noticeService;
    private final ISysNoticeSendService noticeSendService;
    
    @Resource
    private CommonService commonService;

    /**
     * 获取通知公告列表
     */
    @SaCheckPermission("system:notice:list")
    @GetMapping("/list")
    public TableDataInfo<SysNotice> list(SysNotice notice, PageQuery pageQuery) {
        return noticeService.selectPageNoticeList(notice, pageQuery);
    }

    /**
     * 根据通知公告编号获取详细信息
     *
     * @param noticeId 公告ID
     */
    @SaCheckPermission("system:notice:query")
    @GetMapping(value = "/{noticeId}")
    public R<SysNotice> getInfo(@PathVariable Long noticeId) {
        return R.ok(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @SaCheckPermission("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysNotice notice) {
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @SaCheckPermission("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysNotice notice) {
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
	 * 补充用户数据，并返回系统消息
	 * @return
	 */
    @Log(title = "系统消息")
    @GetMapping("/listByUser")
    public R<Map<String, Object>> listByUser(@RequestParam(required = false, defaultValue = "5") Integer pageSize) {
    	LoginUser loginUser = commonService.getLoginUser();
		Long userId = loginUser.getUserId();
		// 1.将系统消息补充到用户通告阅读标记表中
		LambdaQueryWrapper<SysNotice> querySaWrapper = new LambdaQueryWrapper<SysNotice>();
		querySaWrapper.eq(SysNotice::getMsgType,Constants.MSG_TYPE_ALL); // 全部人员
		querySaWrapper.eq(SysNotice::getStatus,Constants.CLOSE_FLAG_0.toString());  // 未关闭
		querySaWrapper.eq(SysNotice::getSendStatus, Constants.HAS_SEND); //已发布
		//querySaWrapper.ge(SysNotice::getEndTime, loginUser.getCreateTime()); //新注册用户不看结束通知
		querySaWrapper.notInSql(SysNotice::getNoticeId,"select notice_id from sys_notice_send where user_id='"+userId+"'");
		List<SysNotice> notices = noticeService.list(querySaWrapper);
		if(notices.size()>0) {
			for(int i=0;i<notices.size();i++) {	
				//因为websocket没有判断是否存在这个用户，要是判断会出现问题，故在此判断逻辑
				LambdaQueryWrapper<SysNoticeSend> query = new LambdaQueryWrapper<>();
				query.eq(SysNoticeSend::getNoticeId,notices.get(i).getNoticeId());
				query.eq(SysNoticeSend::getUserId,userId);
				SysNoticeSend one = noticeSendService.getOne(query);
				if(null==one){
					SysNoticeSend noticeSend = new SysNoticeSend();
					noticeSend.setNoticeId(notices.get(i).getNoticeId());
					noticeSend.setUserId(userId);
					noticeSend.setReadFlag(Constants.NO_READ_FLAG);
					noticeSendService.save(noticeSend);
				}
			}
		}
		// 2.查询用户未读的系统消息
		Page<SysNotice> anntMsgList = new Page<SysNotice>(0, pageSize);
		anntMsgList = noticeService.querySysNoticePageByUserId(anntMsgList,userId,"1");//通知公告消息
		Page<SysNotice> sysMsgList = new Page<SysNotice>(0, pageSize);
		sysMsgList = noticeService.querySysNoticePageByUserId(sysMsgList,userId,"2");//系统消息
		Page<SysNotice> todealMsgList = new Page<SysNotice>(0, pageSize);
		todealMsgList = noticeService.querySysNoticePageByUserId(todealMsgList,userId,"3");//待办消息
		Map<String,Object> sysMsgMap = new HashMap<String, Object>();
		sysMsgMap.put("sysMsgList", sysMsgList.getRecords());
		sysMsgMap.put("sysMsgTotal", sysMsgList.getTotal());
		sysMsgMap.put("anntMsgList", anntMsgList.getRecords());
		sysMsgMap.put("anntMsgTotal", anntMsgList.getTotal());
		sysMsgMap.put("todealMsgList", todealMsgList.getRecords());
		sysMsgMap.put("todealMsgTotal", todealMsgList.getTotal());
		return R.ok(sysMsgMap);
    }
    
    /**
     * 删除通知公告
     *
     * @param noticeIds 公告ID串
     */
    @SaCheckPermission("system:notice:remove")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
