package com.ruoyi.flowable.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.service.CommonService;


/**
 * 流程表达式应用类
 * @author nbacheng
 * @date 2023-05-16
 */

@Service
public class flowExp {
	@Resource
	private CommonService commonService;
	
	public  String getDynamicAssignee() {//动态单个用户例子
		SysUser loginUser = commonService.getSysUserByUserName(TaskUtils.getUserName());
		return loginUser.getUserName();
	}
	
	public  List<String> getDynamicList() {//动态多个用户例子
		List<String> userlist = new ArrayList<String>();
		List<SysUser> list = new ArrayList<SysUser>();
		list = commonService.getUserListByRoleId("1");//管理员角色
		for(SysUser sysuser : list) {
			userlist.add(sysuser.getUserName());
		}
		return userlist;
	}
	
	//根据用户获取部门经理
	public  String getDynamicManager(String userName) { //动态部门经理例子
		// 获取部门负责人列表 		
  		return commonService.getDepLeaderByUserName(userName);
	}
	
	
	/**
	* 反射调用方法
	* @param newObj 实例化的一个对象
	* @param methodName 对象的方法名
	* @param args 参数数组
	* @return 返回值
	* @throws Exception
	*/
	public  Object invokeMethod(Object newObj, String methodName, Object[] args)throws Exception {
		Class ownerClass = newObj.getClass();
		Class[] argsClass = new Class[args.length];
		for (int i = 0, j = args.length; i < j; i++) {
		  argsClass[i] = args[i].getClass();
		}
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(newObj, args);
	}

}
