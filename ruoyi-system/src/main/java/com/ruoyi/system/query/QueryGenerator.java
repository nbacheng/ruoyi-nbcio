package com.ruoyi.system.query;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SpringContextUtils;
import com.ruoyi.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * QueryGenerator
 * @Author nbacheng
 * @Date 2023年10月14日
 */

@Slf4j
public class QueryGenerator {
	public static final String SQL_RULES_COLUMN = "SQL_RULES_COLUMN";

	private static final String BEGIN = "_begin";
	private static final String END = "_end";
	/**
	 * 数字类型字段，拼接此后缀 接受多值参数
	 */
	private static final String MULTI = "_MultiString";
	private static final String STAR = "*";
	private static final String COMMA = ",";
	/**
	 * 查询 逗号转义符 相当于一个逗号【作废】
	 */
	public static final String QUERY_COMMA_ESCAPE = "++";
	private static final String NOT_EQUAL = "!";
	/**页面带有规则值查询，空格作为分隔符*/
	private static final String QUERY_SEPARATE_KEYWORD = " ";
	/**高级查询前端传来的参数名*/
	private static final String SUPER_QUERY_PARAMS = "superQueryParams";
	/** 高级查询前端传来的拼接方式参数名 */
	private static final String SUPER_QUERY_MATCH_TYPE = "superQueryMatchType";
	/** 单引号 */
	public static final String SQL_SQ = "'";
	/**排序列*/
	private static final String ORDER_COLUMN = "column";
	/**排序方式*/
	private static final String ORDER_TYPE = "order";
	private static final String ORDER_TYPE_ASC = "ASC";

	/**mysql 模糊查询之特殊字符下划线 （_、\）*/
	public static final String LIKE_MYSQL_SPECIAL_STRS = "_,%";
	
	//*********数据库类型****************************************
		public static final String DB_TYPE_MYSQL = "MYSQL";
		public static final String DB_TYPE_ORACLE = "ORACLE";
		public static final String DB_TYPE_DM = "DM";//达梦数据库
		public static final String DB_TYPE_POSTGRESQL = "POSTGRESQL";
		public static final String DB_TYPE_SQLSERVER = "SQLSERVER";
		public static final String DB_TYPE_MARIADB = "MARIADB";
		public static final String DB_TYPE_DB2 = "DB2";
		public static final String DB_TYPE_HSQL = "HSQL";
	
	/** 当前系统数据库类型 */
    private static String DB_TYPE = "";
	
	/**时间格式化 */
	private static final ThreadLocal<SimpleDateFormat> local = new ThreadLocal<SimpleDateFormat>();
	private static SimpleDateFormat getTime(){
		SimpleDateFormat time = local.get();
		if(time == null){
			time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			local.set(time);
		}
		return time;
	}
	
	/**
	 * 获取查询条件构造器QueryWrapper实例 通用查询条件已被封装完成
	 * @param searchObj 查询实体
	 * @param parameterMap request.getParameterMap()
	 * @return QueryWrapper实例
	 */
	public static <T> QueryWrapper<T> initQueryWrapper(T searchObj,Map<String, String[]> parameterMap){
		long start = System.currentTimeMillis();
		QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
		installMplus(queryWrapper, searchObj, parameterMap);
		log.debug("---查询条件构造器初始化完成,耗时:"+(System.currentTimeMillis()-start)+"毫秒----");
		return queryWrapper;
	}

	/**
	 * 组装Mybatis Plus 查询条件
	 * <p>使用此方法 需要有如下几点注意:   
	 * <br>1.使用QueryWrapper 而非LambdaQueryWrapper;
	 * <br>2.实例化QueryWrapper时不可将实体传入参数   
	 * <br>错误示例:如QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>(jeecgDemo);
	 * <br>正确示例:QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
	 * <br>3.也可以不使用这个方法直接调用 {@link #initQueryWrapper}直接获取实例
	 */
	private static void installMplus(QueryWrapper<?> queryWrapper,Object searchObj,Map<String, String[]> parameterMap) {
		
		/*
		 * 注意:权限查询由前端配置数据规则 当一个人有多个所属部门时候 可以在规则配置包含条件 orgCode 包含 #{sys_org_code}
		但是不支持在自定义SQL中写orgCode in #{sys_org_code} 
		当一个人只有一个部门 就直接配置等于条件: orgCode 等于 #{sys_org_code} 或者配置自定义SQL: orgCode = '#{sys_org_code}'
		*/
		
		//区间条件组装 模糊查询 高级查询组装 简单排序 权限查询
		PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(searchObj);
	
		String name, type, column;
		// update-begin--Author:taoyan  Date:20200923 for：issues/1671 如果字段加注解了@TableField(exist = false),不走DB查询-------
		//定义实体字段和数据库字段名称的映射 高级查询中 只能获取实体字段 如果设置TableField注解 那么查询条件会出问题
		Map<String,String> fieldColumnMap = new HashMap<String,String>();
		for (int i = 0; i < origDescriptors.length; i++) {
			//aliasName = origDescriptors[i].getName();  mybatis  不存在实体属性 不用处理别名的情况
			name = origDescriptors[i].getName();
			type = origDescriptors[i].getPropertyType().toString();
			try {
				if (judgedIsUselessField(name)|| !PropertyUtils.isReadable(searchObj, name)) {
					continue;
				}

				Object value = PropertyUtils.getSimpleProperty(searchObj, name);
				column = getTableFieldName(searchObj.getClass(), name);
				if(column==null){
					//column为null只有一种情况 那就是 添加了注解@TableField(exist = false) 后续都不用处理了
					continue;
				}
				fieldColumnMap.put(name,column);
				//区间查询
				doIntervalQuery(queryWrapper, parameterMap, type, name, column);
				//判断单值  参数带不同标识字符串 走不同的查询
				//TODO 这种前后带逗号的支持分割后模糊查询需要否 使多选字段的查询生效
				if (null != value && value.toString().startsWith(COMMA) && value.toString().endsWith(COMMA)) {
					String multiLikeval = value.toString().replace(",,", COMMA);
					String[] vals = multiLikeval.substring(1, multiLikeval.length()).split(COMMA);
					final String field = StringUtils.camelToUnderline(column);
					if(vals.length>1) {
						queryWrapper.and(j -> {
							j = j.like(field,vals[0]);
							for (int k=1;k<vals.length;k++) {
								j = j.or().like(field,vals[k]);
							}
							//return j;
						});
					}else {
						queryWrapper.and(j -> j.like(field,vals[0]));
					}
				}else {
					//根据参数值带什么关键字符串判断走什么类型的查询
					QueryRuleEnum rule = convert2Rule(value);
					value = replaceValue(rule,value);
					// add -begin 添加判断为字符串时设为全模糊查询
					//if( (rule==null || QueryRuleEnum.EQ.equals(rule)) && "class java.lang.String".equals(type)) {
						// 可以设置左右模糊或全模糊，因人而异
						//rule = QueryRuleEnum.LIKE;
					//}
					// add -end 添加判断为字符串时设为全模糊查询
					addEasyQuery(queryWrapper, column, rule, value);
				}
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		// 排序逻辑 处理 
		doMultiFieldsOrder(queryWrapper, parameterMap);
				
		//高级查询
		doSuperQuery(queryWrapper, parameterMap, fieldColumnMap);
		// update-end--Author:taoyan  Date:20200923 for：issues/1671 如果字段加注解了@TableField(exist = false),不走DB查询-------
		
	}


	/**
	 * 区间查询
	 * @param queryWrapper query对象
	 * @param parameterMap 参数map
	 * @param type         字段类型
	 * @param filedName    字段名称
	 * @param columnName   列名称
	 */
	private static void doIntervalQuery(QueryWrapper<?> queryWrapper, Map<String, String[]> parameterMap, String type, String filedName, String columnName) throws ParseException {
		// 添加 判断是否有区间值
		String endValue = null,beginValue = null;
		if (parameterMap != null && parameterMap.containsKey(filedName + BEGIN)) {
			beginValue = parameterMap.get(filedName + BEGIN)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, beginValue, QueryRuleEnum.GE);

		}
		if (parameterMap != null && parameterMap.containsKey(filedName + END)) {
			endValue = parameterMap.get(filedName + END)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, endValue, QueryRuleEnum.LE);
		}
		//多值查询
		if (parameterMap != null && parameterMap.containsKey(filedName + MULTI)) {
			endValue = parameterMap.get(filedName + MULTI)[0].trim();
			addQueryByRule(queryWrapper, columnName.replace(MULTI,""), type, endValue, QueryRuleEnum.IN);
		}
	}
	
	//多字段排序 TODO 需要修改前端
	private static void doMultiFieldsOrder(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap) {
		String column=null,order=null;
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_COLUMN)) {
			column = parameterMap.get(ORDER_COLUMN)[0];
		}
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_TYPE)) {
			order = parameterMap.get(ORDER_TYPE)[0];
		}
        log.info("排序规则>>列:" + column + ",排序方式:" + order);
		if (ObjectUtils.isNotEmpty(column) && ObjectUtils.isNotEmpty(order)) {
			//update-begin--Author:scott  Date:20210531 for：36 多条件排序无效问题修正-------
			// 排序规则修改
			// 将现有排序 _ 前端传递排序条件{....,column: 'column1,column2',order: 'desc'} 翻译成sql "column1,column2 desc"
			// 修改为 _ 前端传递排序条件{....,column: 'column1,column2',order: 'desc'} 翻译成sql "column1 desc,column2 desc"
			if (order.toUpperCase().indexOf(ORDER_TYPE_ASC)>=0) {
				//queryWrapper.orderByAsc(ObjectUtils.camelToUnderline(column));
				String columnStr = StringUtils.camelToUnderline(column);
				String[] columnArray = columnStr.split(",");
				queryWrapper.orderByAsc(Arrays.asList(columnArray));
			} else {
				//queryWrapper.orderByDesc(ObjectUtils.camelToUnderline(column));
				String columnStr = StringUtils.camelToUnderline(column);
				String[] columnArray = columnStr.split(",");
				queryWrapper.orderByDesc(Arrays.asList(columnArray));
			}
			//update-end--Author:scott  Date:20210531 for：36 多条件排序无效问题修正-------
		}
	}
	
	/**
	 * 高级查询
	 * @param queryWrapper 查询对象
	 * @param parameterMap 参数对象
	 * @param fieldColumnMap 实体字段和数据库列对应的map
	 */
	private static void doSuperQuery(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
		if(parameterMap!=null&& parameterMap.containsKey(SUPER_QUERY_PARAMS)){
			String superQueryParams = parameterMap.get(SUPER_QUERY_PARAMS)[0];
			String superQueryMatchType = parameterMap.get(SUPER_QUERY_MATCH_TYPE) != null ? parameterMap.get(SUPER_QUERY_MATCH_TYPE)[0] : MatchTypeEnum.AND.getValue();
            MatchTypeEnum matchType = MatchTypeEnum.getByValue(superQueryMatchType);
            // update-begin--Author:sunjianlei  Date:20200325 for：高级查询的条件要用括号括起来，防止和用户的其他条件冲突 -------
            try {
                superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
                List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
                if (conditions == null || conditions.size() == 0) {
                    return;
                }
                log.info("---高级查询参数-->" + conditions.toString());
                queryWrapper.and(andWrapper -> {
                    for (int i = 0; i < conditions.size(); i++) {
                        QueryCondition rule = conditions.get(i);
                        if (ObjectUtils.isNotEmpty(rule.getField())
                                && ObjectUtils.isNotEmpty(rule.getRule())
                                && ObjectUtils.isNotEmpty(rule.getVal())) {

                            log.debug("SuperQuery ==> " + rule.toString());

                            //update-begin-author:taoyan date:20201228 for: 【高级查询】 oracle 日期等于查询报错
							Object queryValue = rule.getVal();
                            if("date".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(),DateUtils.date_sdf.get());
							}else if("datetime".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(), DateUtils.datetimeFormat.get());
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级查询没有类型转换，查询参数都是字符串类型 ----
							String dbType = rule.getDbType();
							if (ObjectUtils.isNotEmpty(dbType)) {
								try {
									String valueStr = String.valueOf(queryValue);
									switch (dbType.toLowerCase().trim()) {
										case "int":
											queryValue = Integer.parseInt(valueStr);
											break;
										case "bigdecimal":
											queryValue = new BigDecimal(valueStr);
											break;
										case "short":
											queryValue = Short.parseShort(valueStr);
											break;
										case "long":
											queryValue = Long.parseLong(valueStr);
											break;
										case "float":
											queryValue = Float.parseFloat(valueStr);
											break;
										case "double":
											queryValue = Double.parseDouble(valueStr);
											break;
										case "boolean":
											queryValue = Boolean.parseBoolean(valueStr);
											break;
									}
								} catch (Exception e) {
									log.error("高级查询值转换失败：", e);
								}
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级查询没有类型转换，查询参数都是字符串类型 ----
                            addEasyQuery(andWrapper, fieldColumnMap.get(rule.getField()), QueryRuleEnum.getByValue(rule.getRule()), queryValue);
							//update-end-author:taoyan date:20201228 for: 【高级查询】 oracle 日期等于查询报错

                            // 如果拼接方式是OR，就拼接OR
                            if (MatchTypeEnum.OR == matchType && i < (conditions.size() - 1)) {
                                andWrapper.or();
                            }
                        }
                    }
                    //return andWrapper;
                });
            } catch (UnsupportedEncodingException e) {
                log.error("--高级查询参数转码失败：" + superQueryParams, e);
            } catch (Exception e) {
                log.error("--高级查询拼接失败：" + e.getMessage());
                e.printStackTrace();
            }
            // update-end--Author:sunjianlei  Date:20200325 for：高级查询的条件要用括号括起来，防止和用户的其他条件冲突 -------
		}
		//log.info(" superQuery getCustomSqlSegment: "+ queryWrapper.getCustomSqlSegment());
	}
	/**
	 * 根据所传的值 转化成对应的比较方式
	 * 支持><= like in !
	 * @param value
	 * @return
	 */
	public static QueryRuleEnum convert2Rule(Object value) {
		// 避免空数据
		// update-begin-author:taoyan date:20210629 for: 查询条件输入空格导致return null后续判断导致抛出null异常
		if (value == null) {
			return QueryRuleEnum.EQ;
		}
		String val = (value + "").toString().trim();
		if (val.length() == 0) {
			return QueryRuleEnum.EQ;
		}
		// update-end-author:taoyan date:20210629 for: 查询条件输入空格导致return null后续判断导致抛出null异常
		QueryRuleEnum rule =null;

		//update-begin--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
		//TODO 此处规则，只适用于 le lt ge gt
		// step 2 .>= =<
		if (rule == null && val.length() >= 3) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(2, 3))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 2));
			}
		}
		// step 1 .> <
		if (rule == null && val.length() >= 2) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(1, 2))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 1));
			}
		}
		//update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284---------------------

		// step 3 like
		if (rule == null && val.contains(STAR)) {
			if (val.startsWith(STAR) && val.endsWith(STAR)) {
				rule = QueryRuleEnum.LIKE;
			} else if (val.startsWith(STAR)) {
				rule = QueryRuleEnum.LEFT_LIKE;
			} else if(val.endsWith(STAR)){
				rule = QueryRuleEnum.RIGHT_LIKE;
			}
		}

		// step 4 in
		if (rule == null && val.contains(COMMA)) {
			//TODO in 查询这里应该有个bug  如果一字段本身就是多选 此时用in查询 未必能查询出来
			rule = QueryRuleEnum.IN;
		}
		// step 5 != 
		if(rule == null && val.startsWith(NOT_EQUAL)){
			rule = QueryRuleEnum.NE;
		}
		// step 6 xx+xx+xx 这种情况适用于如果想要用逗号作精确查询 但是系统默认逗号走in 所以可以用++替换【此逻辑作废】
		if(rule == null && val.indexOf(QUERY_COMMA_ESCAPE)>0){
			rule = QueryRuleEnum.EQ_WITH_ADD;
		}

		//update-begin--Author:taoyan  Date:20201229 for：initQueryWrapper组装sql查询条件错误 #284---------------------
		//特殊处理：Oracle的表达式to_date('xxx','yyyy-MM-dd')含有逗号，会被识别为in查询，转为等于查询
		if(rule == QueryRuleEnum.IN && val.indexOf("yyyy-MM-dd")>=0 && val.indexOf("to_date")>=0){
			rule = QueryRuleEnum.EQ;
		}
		//update-end--Author:taoyan  Date:20201229 for：initQueryWrapper组装sql查询条件错误 #284---------------------

		return rule != null ? rule : QueryRuleEnum.EQ;
	}
	
	/**
	 * 替换掉关键字字符
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private static Object replaceValue(QueryRuleEnum rule, Object value) {
		if (rule == null) {
			return null;
		}
		if (! (value instanceof String)){
			return value;
		}
		String val = (value + "").toString().trim();
		if (rule == QueryRuleEnum.LIKE) {
			value = val.substring(1, val.length() - 1);
			//mysql 模糊查询之特殊字符下划线 （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.LEFT_LIKE || rule == QueryRuleEnum.NE) {
			value = val.substring(1);
			//mysql 模糊查询之特殊字符下划线 （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.RIGHT_LIKE) {
			value = val.substring(0, val.length() - 1);
			//mysql 模糊查询之特殊字符下划线 （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.IN) {
			value = val.split(",");
		} else if (rule == QueryRuleEnum.EQ_WITH_ADD) {
			value = val.replaceAll("\\+\\+", COMMA);
		}else {
			//update-begin--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
			if(val.startsWith(rule.getValue())){
				//TODO 此处逻辑应该注释掉-> 如果查询内容中带有查询匹配规则符号，就会被截取的（比如：>=您好）
				value = val.replaceFirst(rule.getValue(),"");
			}else if(val.startsWith(rule.getCondition()+QUERY_SEPARATE_KEYWORD)){
				value = val.replaceFirst(rule.getCondition()+QUERY_SEPARATE_KEYWORD,"").trim();
			}
			//update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
		}
		return value;
	}
	
	private static void addQueryByRule(QueryWrapper<?> queryWrapper,String name,String type,String value,QueryRuleEnum rule) throws ParseException {
		if(ObjectUtils.isNotEmpty(value)) {
			Object temp;
			// 针对数字类型字段，多值查询
			if(value.indexOf(COMMA)!=-1){
				temp = value;
				addEasyQuery(queryWrapper, name, rule, temp);
				return;
			}

			switch (type) {
			case "class java.lang.Integer":
				temp =  Integer.parseInt(value);
				break;
			case "class java.math.BigDecimal":
				temp =  new BigDecimal(value);
				break;
			case "class java.lang.Short":
				temp =  Short.parseShort(value);
				break;
			case "class java.lang.Long":
				temp =  Long.parseLong(value);
				break;
			case "class java.lang.Float":
				temp =   Float.parseFloat(value);
				break;
			case "class java.lang.Double":
				temp =  Double.parseDouble(value);
				break;
			case "class java.util.Date":
				temp = getDateQueryByRule(value, rule);
				break;
			default:
				temp = value;
				break;
			}
			addEasyQuery(queryWrapper, name, rule, temp);
		}
	}
	
	/**
	 * 获取日期类型的值
	 * @param value
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Date getDateQueryByRule(String value,QueryRuleEnum rule) throws ParseException {
		Date date = null;
		if(value.length()==10) {
			if(rule==QueryRuleEnum.GE) {
				//比较大于
				date = getTime().parse(value + " 00:00:00");
			}else if(rule==QueryRuleEnum.LE) {
				//比较小于
				date = getTime().parse(value + " 23:59:59");
			}
			//TODO 日期类型比较特殊 可能oracle下不一定好使
		}
		if(date==null) {
			date = getTime().parse(value);
		}
		return date;
	}
	
	/**
	  * 根据规则走不同的查询
	 * @param queryWrapper QueryWrapper
	 * @param name         字段名字
	 * @param rule         查询规则
	 * @param value        查询条件值
	 */
	private static void addEasyQuery(QueryWrapper<?> queryWrapper, String name, QueryRuleEnum rule, Object value) {
		if (value == null || rule == null || ObjectUtils.isEmpty(value)) {
			return;
		}
		name = StringUtils.camelToUnderline(name);
		log.info("--查询规则-->"+name+" "+rule.getValue()+" "+value);
		switch (rule) {
		case GT:
			queryWrapper.gt(name, value);
			break;
		case GE:
			queryWrapper.ge(name, value);
			break;
		case LT:
			queryWrapper.lt(name, value);
			break;
		case LE:
			queryWrapper.le(name, value);
			break;
		case EQ:
		case EQ_WITH_ADD:
			queryWrapper.eq(name, value);
			break;
		case NE:
			queryWrapper.ne(name, value);
			break;
		case IN:
			if(value instanceof String) {
				queryWrapper.in(name, (Object[])value.toString().split(","));
			}else if(value instanceof String[]) {
				queryWrapper.in(name, (Object[]) value);
			}
			//update-begin-author:taoyan date:20200909 for:【bug】in 类型多值查询 不适配postgresql #1671
			else if(value.getClass().isArray()) {
				queryWrapper.in(name, (Object[])value);
			}else {
				queryWrapper.in(name, value);
			}
			//update-end-author:taoyan date:20200909 for:【bug】in 类型多值查询 不适配postgresql #1671
			break;
		case LIKE:
			queryWrapper.like(name, value);
			break;
		case LEFT_LIKE:
			queryWrapper.likeLeft(name, value);
			break;
		case RIGHT_LIKE:
			queryWrapper.likeRight(name, value);
			break;
		default:
			log.info("--查询规则未匹配到---");
			break;
		}
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	private static boolean judgedIsUselessField(String name) {
		return "class".equals(name) || "ids".equals(name)
				|| "page".equals(name) || "rows".equals(name)
				|| "sort".equals(name) || "order".equals(name);
	}
	
	/**
	* @author: scott
	* @Description: 去掉值前后单引号
	* @date: 2020/3/19 21:26
	* @param ruleValue: 
	* @Return: java.lang.String
	*/
	public static String trimSingleQuote(String ruleValue) {
		if (ObjectUtils.isEmpty(ruleValue)) {
			return "";
		}
		if (ruleValue.startsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(1);
		}
		if (ruleValue.endsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(0, ruleValue.length() - 1);
		}
		return ruleValue;
	}
	
	/**
	 * 获取sql中的#{key} 这个key组成的set
	 */
	public static Set<String> getSqlRuleParams(String sql) {
		if(ObjectUtils.isEmpty(sql)){
			return null;
		}
		Set<String> varParams = new HashSet<String>();
		String regex = "\\#\\{\\w+\\}";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		while(m.find()){
			String var = m.group();
			varParams.add(var.substring(var.indexOf("{")+1,var.indexOf("}")));
		}
		return varParams;
	}
	
	/**
	 * 获取查询条件 
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @return
	 */
	public static String getSingleQueryConditionSql(String field,String alias,Object value,boolean isString) {
		return getSingleQueryConditionSql(field, alias, value, isString,null);
	}

	/**
	 * 报表获取查询条件 支持多数据源
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	public static String getSingleQueryConditionSql(String field,String alias,Object value,boolean isString, String dataBaseType) {
		if (value == null) {
			return "";
		}
		field =  alias+StringUtils.camelToUnderline(field);
		QueryRuleEnum rule = QueryGenerator.convert2Rule(value);
		return getSingleSqlByRule(rule, field, value, isString, dataBaseType);
	}

	/**
	 * 获取单个查询条件的值
	 * @param rule
	 * @param field
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	private static String getSingleSqlByRule(QueryRuleEnum rule,String field,Object value,boolean isString, String dataBaseType) {
		String res = "";
		switch (rule) {
		case GT:
			res =field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case GE:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case LT:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case LE:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case EQ:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case EQ_WITH_ADD:
			res = field+" = "+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case NE:
			res = field+" <> "+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case IN:
			res = field + " in "+getInConditionValue(value, isString);
			break;
		case LIKE:
			res = field + " like "+getLikeConditionValue(value);
			break;
		case LEFT_LIKE:
			res = field + " like "+getLikeConditionValue(value);
			break;
		case RIGHT_LIKE:
			res = field + " like "+getLikeConditionValue(value);
			break;
		default:
			res = field+" = "+getFieldConditionValue(value, isString, dataBaseType);
			break;
		}
		return res;
	}


	/**
	 * 获取单个查询条件的值
	 * @param rule
	 * @param field
	 * @param value
	 * @param isString
	 * @return
	 */
	private static String getSingleSqlByRule(QueryRuleEnum rule,String field,Object value,boolean isString) {
		return getSingleSqlByRule(rule, field, value, isString, null);
	}

	/**
	 * 获取查询条件的值
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	private static String getFieldConditionValue(Object value,boolean isString, String dataBaseType) {
		String str = value.toString().trim();
		if(str.startsWith("!")) {
			str = str.substring(1);
		}else if(str.startsWith(">=")) {
			str = str.substring(2);
		}else if(str.startsWith("<=")) {
			str = str.substring(2);
		}else if(str.startsWith(">")) {
			str = str.substring(1);
		}else if(str.startsWith("<")) {
			str = str.substring(1);
		}else if(str.indexOf(QUERY_COMMA_ESCAPE)>0) {
			str = str.replaceAll("\\+\\+", COMMA);
		}
		if(dataBaseType==null){
			dataBaseType = getDbType();
		}
		if(isString) {
			if(DB_TYPE_SQLSERVER.equals(dataBaseType)){
				return " N'"+str+"' ";
			}else{
				return " '"+str+"' ";
			}
		}else {
			// 如果不是字符串 有一种特殊情况 popup调用都走这个逻辑 参数传递的可能是“‘admin’”这种格式的
			if(DB_TYPE_SQLSERVER.equals(dataBaseType) && str.endsWith("'") && str.startsWith("'")){
				return " N"+str;
			}
			return value.toString();
		}
	}

	private static String getInConditionValue(Object value,boolean isString) {
		//update-begin-author:taoyan date:20210628 for: 查询条件如果输入,导致sql报错
		String[] temp = value.toString().split(",");
		if(temp.length==0){
			return "('')";
		}
		if(isString) {
			List<String> res = new ArrayList<>();
			for (String string : temp) {
				if(DB_TYPE_SQLSERVER.equals(getDbType())){
					res.add("N'"+string+"'");
				}else{
					res.add("'"+string+"'");
				}
			}
			return "("+String.join("," ,res)+")";
		}else {
			return "("+value.toString()+")";
		}
		//update-end-author:taoyan date:20210628 for: 查询条件如果输入,导致sql报错
	}
	
	private static String getLikeConditionValue(Object value) {
		String str = value.toString().trim();
		if(str.startsWith("*") && str.endsWith("*")) {
			if(DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'%"+str.substring(1,str.length()-1)+"%'";
			}else{
				return "'%"+str.substring(1,str.length()-1)+"%'";
			}
		}else if(str.startsWith("*")) {
			if(DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'%"+str.substring(1)+"'";
			}else{
				return "'%"+str.substring(1)+"'";
			}
		}else if(str.endsWith("*")) {
			if(DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'"+str.substring(0,str.length()-1)+"%'";
			}else{
				return "'"+str.substring(0,str.length()-1)+"%'";
			}
		}else {
			if(str.indexOf("%")>=0) {
				if(DB_TYPE_SQLSERVER.equals(getDbType())){
					if(str.startsWith("'") && str.endsWith("'")){
						return "N"+str;
					}else{
						return "N"+"'"+str+"'";
					}
				}else{
					if(str.startsWith("'") && str.endsWith("'")){
						return str;
					}else{
						return "'"+str+"'";
					}
				}
			}else {
				if(DB_TYPE_SQLSERVER.equals(getDbType())){
					return "N'%"+str+"%'";
				}else{
					return "'%"+str+"%'";
				}
			}
		}
	}


	/**
	 * 获取系统数据库类型
	 */
	private static String getDbType(){
		return getDatabaseType();
	}
	
	private static String getDatabaseType() {
        DataSource dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
        try {
            return getDatabaseTypeByDataSource(dataSource);
        } catch (SQLException e) {
            //e.printStackTrace();
            log.warn(e.getMessage(),e);
            return "";
        }
    }
	
	/**
     * 获取数据库类型
     * @param dataSource
     * @return
     * @throws SQLException
     */
    private static String getDatabaseTypeByDataSource(DataSource dataSource) throws SQLException{
        if("".equals(DB_TYPE)) {
            Connection connection = dataSource.getConnection();
            try {
                DatabaseMetaData md = connection.getMetaData();
                String dbType = md.getDatabaseProductName().toLowerCase();
                if(dbType.indexOf("mysql")>=0) {
                    DB_TYPE = DB_TYPE_MYSQL;
                }else if(dbType.indexOf("oracle")>=0 ||dbType.indexOf("dm")>=0) {
                    DB_TYPE = DB_TYPE_ORACLE;
                }else if(dbType.indexOf("sqlserver")>=0||dbType.indexOf("sql server")>=0) {
                    DB_TYPE = DB_TYPE_SQLSERVER;
                }else if(dbType.indexOf("postgresql")>=0) {
                    DB_TYPE = DB_TYPE_POSTGRESQL;
                }else if(dbType.indexOf("mariadb")>=0) {
                    DB_TYPE = DB_TYPE_MARIADB;
                }else {
                    log.error("数据库类型:[" + dbType + "]不识别!");
                    //throw new JeecgBootException("数据库类型:["+dbType+"]不识别!");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }finally {
                connection.close();
            }
        }
        return DB_TYPE;

    }


	/**
	 * 获取class的 包括父类的
	 * @param clazz
	 * @return
	 */
	private static List<Field> getClassFields(Class<?> clazz) {
		List<Field> list = new ArrayList<Field>();
		Field[] fields;
		do{
			fields = clazz.getDeclaredFields();
			for(int i = 0;i<fields.length;i++){
				list.add(fields[i]);
			}
			clazz = clazz.getSuperclass();
		}while(clazz!= Object.class&&clazz!=null);
		return list;
	}

	/**
	 * 获取表字段名
	 * @param clazz
	 * @param name
	 * @return
	 */
	private static String getTableFieldName(Class<?> clazz, String name) {
		try {
			//如果字段加注解了@TableField(exist = false),不走DB查询
			Field field = null;
			try {
				field = clazz.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				//e.printStackTrace();
			}

			//如果为空，则去父类查找字段
			if (field == null) {
				List<Field> allFields = getClassFields(clazz);
				List<Field> searchFields = allFields.stream().filter(a -> a.getName().equals(name)).collect(Collectors.toList());
				if(searchFields!=null && searchFields.size()>0){
					field = searchFields.get(0);
				}
			}

			if (field != null) {
				TableField tableField = field.getAnnotation(TableField.class);
				if (tableField != null){
					if(tableField.exist() == false){
						//如果设置了TableField false 这个字段不需要处理
						return null;
					}else{
						String column = tableField.value();
						//如果设置了TableField value 这个字段是实体字段
						if(!"".equals(column)){
							return column;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * mysql 模糊查询之特殊字符下划线 （_、\）
	 *
	 * @param value:
	 * @Return: java.lang.String
	 */
	private static String specialStrConvert(String value) {
		if (DB_TYPE_MYSQL.equals(getDbType()) || DB_TYPE_MARIADB.equals(getDbType())) {
			String[] special_str = QueryGenerator.LIKE_MYSQL_SPECIAL_STRS.split(",");
			for (String str : special_str) {
				if (value.indexOf(str) !=-1) {
					value = value.replace(str, "\\" + str);
				}
			}
		}
		return value;
	}
}
