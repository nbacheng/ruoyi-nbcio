## Ruoyi-Nbcio V1.0.0 NBCIO亿事达企业管理平台简介

[![码云Gitee](https://gitee.com/nbacheng/ruoyi-nbcio/badge/star.svg?theme=blue)](https://gitee.com/nbacheng/ruoyi-nbcio)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://gitee.com/nbacheng/ruoyi-nbcio/blob/master/LICENSE)
[![](https://img.shields.io/badge/Author-宁波阿成-orange.svg)](http://122.227.135.243:9666/)
[![](https://img.shields.io/badge/Blog-个人博客-blue.svg)](https://nbacheng.blog.csdn.net)
[![](https://img.shields.io/badge/version-1.0.0-brightgreen.svg)](https://gitee/nbacheng/ruoyi-nbcio)
[![使用STS开发维护](https://img.shields.io/badge/STS-提供支持-blue.svg)](https://spring.io/tools)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-blue.svg)]()
[![JDK-8+](https://img.shields.io/badge/JDK-8-green.svg)]()
[![JDK-11](https://img.shields.io/badge/JDK-11-green.svg)]()

## 注意
   以后主要在gitee上更新，gitee地址为https://gitee.com/nbacheng/ruoyi-nbcio
- 本项目基于 [RuoYi-Flowable-Plus](https://gitee.com/KonBAI-Q/ruoyi-flowable-plus) 进行二次开发，从nbcio-boot(https://gitee.com/nbacheng/nbcio-boot)项目
  移植过来相关功能，脚手架功能同步更新 [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus) 项目。
- 项目处于开发移植阶段，目前初步完成流程相关工作，后续还会继续完善，目前仅推荐用于学习、毕业设计等个人使用，确有商业应用的请知会作者。

## 参考文档
- 项目文档：[RuoYi-Nbcio开发文档 目前暂时指向nbcio-boot项目文档 见演示系统里的在线帮助]
- 脚手架文档：[RuoYi-Vue-Plus文档](https://gitee.com/dromara/RuoYi-Vue-Plus/wikis/pages)

## 项目地址
- Gitee：<https://gitee.com/nbacheng/ruoyi-nbcio>

## 增加的主要功能

   1、表单设计修改为formdesigner。
   
   2、增加消息提醒功能。
   
   3、支持动态角色与用户,流程流转原来的userid方式修改为username的方式。
   
   4、全新修改了原先启动流程的过程。
   
   5、增加自定义业务表单
   
   6、支持本地图片上传
   
   7、增加自定义业务表单的流程流转
   
   8、支持多实例流程流转和多实例自定义条件
   
   9、支持部门经理审批
   
   

## 支持项目
-  如果项目对你有帮助，请给项目点个Star，同时也可以请作者喝杯咖啡吧！
![](https://oscimg.oschina.net/oscnet/up-58088c35672c874bd5a95c2327300d44dca.png)

## 在线演示
演示服务不限制操作，希望大家按需使用，不要恶意添加脏数据或对服务器进行攻击等操作。

[RuoYi-Nbcio 在线演示](http://122.227.135.243:9666/)

|                 | 账号  | 密码      |
|---------------- | ----- | -------- |
| 超管账户         目前只能通过gitee授权免密码登录或star后进群要密码
| 监控中心（未运行） | ruoyi | 123456   |
| 任务调度中心      | admin | 123456   |
| 数据监控中心      | ruoyi | 123456   |

## 技术交流群

QQ交流群: 703572701 

## 友情链接
- [基于jeec-boo3.0的nbcio-boot项目](https://gitee.com/nbacheng/nbcio-boot)： NBCIO 亿事达企业管理平台。

## 后端技术架构
- 基础框架：Spring Boot 2.7.11

- 持久层框架：Mybatis-plus 3.5.3.1

- 安全框架：Sa-Token 1.34.0

- 缓存框架：redis

- 日志打印：logback

- 其他：fastjson，poi，Swagger-ui，quartz, lombok（简化代码）等。

## 开发环境

- 语言：Java 8 java 11

- IDE(JAVA)： STS安装lombok插件 或者 IDEA

- 依赖管理：Maven

- 数据库：MySQL5.7+  &  Oracle 11g & SqlServer & postgresql & 国产等更多数据库

- 缓存：Redis

## 前端技术

- 基础框架：ant-design-vue - Ant Design Of Vue 实现

- Element UI JavaScript 框架：Vue

- Webpack node npm eslint @vue/cli 3.2.1

- fullcalendar v-charts echarts luckysheet

- vue-cropper - 头像裁剪组件 @antv/g2 - Alipay AntV 数据可视化图表

- Viser-vue - antv/g2 封装实现

## 参与开源
- 如遇到问题，欢迎提交到 [issues](https://gitee.com/nbacheng/ruoyi-nbcio/issues)（请按模版进行填写信息）。
- 欢迎fork项目，同时提交相关功能。

## 特别鸣谢
- [RuoYi-Flowable-Plus](https://gitee.com/KonBAI-Q/ruoyi-flowable-plus) 
- [bpmn-process-designer](https://gitee.com/MiyueSC/bpmn-process-designer)
- [formDesigner](https://gitee.com/wurong19870715/formDesigner)

## 演示图例
<table style="width:100%; text-align:center">
<tbody>

登录界面

![](https://oscimg.oschina.net/oscnet/up-0a892b45665e14a21a171620847ef33536a.png)

流程分类

![](https://oscimg.oschina.net/oscnet/up-8cec03ba0a44455add3bb4ffac0740d78ca.png)

流程表单

![](https://oscimg.oschina.net/oscnet/up-64febc26bd3f1ba24b8a4066afad42e1f07.png)

流程模型定义

![](https://oscimg.oschina.net/oscnet/up-e21b2262a7e00045eac525bd9d34f314ec6.png)

流程布置管理

![](https://oscimg.oschina.net/oscnet/up-ff34b7eb779ea31fb93c84c6bb3a5ac3b49.png)

自定义业务表单关联创建

![](https://oscimg.oschina.net/oscnet/up-cdd3ec9e312d03389bf9b5b8b975ddfbf85.png)

新建普通的OA流程

![](https://oscimg.oschina.net/oscnet/up-0fc1b0886801a1af4d7fb0d8c4a20d9054a.png)

基于formdesigner的表单设计

![](https://oscimg.oschina.net/oscnet/up-abd87b4f6b5b1552d0e433d8a42dc5cd95c.png)

流程设计

![](https://oscimg.oschina.net/oscnet/up-7a058da9b79a0fcd13bc05bf686cb0e4a97.png)

普通OA流程的发起

![](https://oscimg.oschina.net/oscnet/up-0b8438d0f1d2288f92ab76cc502ecce2386.png)

自定义业务流程的发起，单表案例

![](https://oscimg.oschina.net/oscnet/up-58d242ecc0ffa7c1f8daac1733c26583a94.png)

普通OA流程的审批

![](https://oscimg.oschina.net/oscnet/up-9c55d3b622100acbbf553df1a100ef4e9cb.png)

普通OA流程的任务办理

![](https://oscimg.oschina.net/oscnet/up-6ff18bb82a8e3fee3ad3f5bea0c04162d46.png)

普通OA流程的表单信息

![](https://oscimg.oschina.net/oscnet/up-923af5ee1685d88f53b62314b1a1c53f0bb.png)

普通OA流程的流转记录

![](https://oscimg.oschina.net/oscnet/up-3fb048caafa240aa97acc68d734148a1948.png)

普通OA流程的流程跟踪

![](https://oscimg.oschina.net/oscnet/up-4d93951e9af906cd5bdea044c372a288f6b.png)

自定义业务表单流程的表单信息，其它跟OA流程一样

![](https://oscimg.oschina.net/oscnet/up-b78b5a5145c80745cf3d9591b65ceecaef9.png)

流程结束

![](https://oscimg.oschina.net/oscnet/up-ce19517b7a15664032128d491c7dd1eb780.png)

已办任务

![](https://oscimg.oschina.net/oscnet/up-57ab589dcca8a39d80998039da348f8e2c2.png)

抄送我的

![](https://oscimg.oschina.net/oscnet/up-888868ebeeb36b3dae2a422fb0e0a496733.png)

自定义业务流程相关信息

![](https://oscimg.oschina.net/oscnet/up-b99f34fa23f1056ca8d70baca93ada49558.png)
</tbody>
</table>
