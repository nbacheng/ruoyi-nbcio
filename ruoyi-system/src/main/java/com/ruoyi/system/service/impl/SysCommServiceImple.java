package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.WebsocketConst;
import com.ruoyi.common.core.domain.dto.MessageDTO;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.websocket.WebSocketServer;
import com.ruoyi.flowable.common.constant.TaskConstants;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.SysNoticeSend;
import com.ruoyi.system.mapper.CommonMapper;
import com.ruoyi.system.mapper.SysNoticeMapper;
import com.ruoyi.system.mapper.SysNoticeSendMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.core.service.CommonService;
import com.ruoyi.common.helper.LoginHelper;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SysCommServiceImple extends ServiceImpl<CommonMapper, Object> implements CommonService {

	@Value("${flowable.message-base-url}")
	private String msgBaseUrl;

    @Resource
	private SysUserMapper userMapper;
    @Resource
    SysNoticeMapper sysNoticeMapper;
	@Resource
    private WebSocketServer webSocket;
	@Resource
	private SysNoticeSendMapper sysNoticeSendMapper;
	@Resource
	ISysUserService sysUserService;
	@Resource
	ISysDeptService sysDeptService;
	
	@Resource
	private CommonMapper commonMapper;
	
	@Override
	public void sendSysNotice(MessageDTO message) {
		this.sendSysNotice(message.getFromUser(),
				message.getToUser(),
				message.getTitle(),
				message.getContent(),
				message.getCategory());		
	}

	/**
	 * 发消息
	 * @param fromUser
	 * @param toUser
	 * @param title
	 * @param msgContent
	 * @param setMsgCategory
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sendSysNotice(String fromUser, String toUser, String title, String msgContent, String setMsgCategory) {
		SysNotice notice = new SysNotice();
		notice.setNoticeTitle(title);
		notice.setNoticeContent(msgContent);
		notice.setNoticeType(setMsgCategory);
		notice.setSender(Long.valueOf(fromUser));
		notice.setPriority(Constants.PRIORITY_M);
		notice.setMsgType(Constants.MSG_TYPE_UESR);
		notice.setSendStatus(Constants.HAS_SEND);
		notice.setSendTime(new Date());
		notice.setMsgType(Constants.MSG_TYPE_UESR);
		notice.setStatus("0");
		sysNoticeMapper.insert(notice);
		// 2.插入用户通告阅读标记表记录
		String userId = toUser;
		String[] userIds = userId.split(",");
		Long noticeId = notice.getNoticeId();
		for(int i=0;i<userIds.length;i++) {
			if(ObjectUtil.isNotEmpty(userIds[i])) {
				SysUser sysUser = sysUserService.selectUserByUserName(userIds[i]);
				if(sysUser==null) {
					continue;
				}
				SysNoticeSend noticeSend = new SysNoticeSend();
				noticeSend.setNoticeId(noticeId);
				noticeSend.setUserId(sysUser.getUserId());
				noticeSend.setReadFlag(Constants.NO_READ_FLAG);
				sysNoticeSendMapper.insert(noticeSend);
				JSONObject obj = new JSONObject();
				obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				obj.put(WebsocketConst.MSG_USER_ID, sysUser.getUserName());
				obj.put(WebsocketConst.MSG_ID, notice.getNoticeId());
				obj.put(WebsocketConst.MSG_TXT, notice.getNoticeTitle());
				webSocket.sendMessage(sysUser.getUserName(), obj.toJSONString());
				webSocket.pushMessage(sysUser.getUserName(), obj.toJSONString());
			}
		}
		
	}

	@Override
	public String getBaseUrl() {
		return msgBaseUrl;
	}

	@Override
	public LoginUser getLoginUser() {
		LoginUser user = LoginHelper.getLoginUser();
		return user;
	}

	@Override
	public List<SysUser> getUserListByRoleId(String roleId) {
		String realId = roleId.replace(TaskConstants.ROLE_GROUP_PREFIX,"");
		List<Long>  listUserId = commonMapper.selectUserIdByRoleId(Long.valueOf(realId));
		List<SysUser> listUser = new ArrayList<SysUser>();
		if(ObjectUtil.isNotEmpty(listUserId)) {
			for(Long userid :listUserId) {
				if(ObjectUtil.isNotEmpty(userid)) {
				  SysUser sysUser = sysUserService.selectUserById(Long.valueOf(userid.toString()));
				  if(ObjectUtil.isNotEmpty(sysUser)) {
					  listUser.add(sysUser);
				  }
				}
				
			}
		}
		return listUser;
	}

	@Override
	public SysUser getSysUserByUserId(Long UserId) {
		
		return sysUserService.selectUserById(UserId);
	}

	@Override
	public SysUser getSysUserByUserName(String userName) {
		
		return sysUserService.selectUserByUserName(userName);
	}

	@Override
	public String getDepLeaderByUserName(String userName) {
		
		return sysDeptService.getDepLeaderByUserName(userName);
	}
	
}
