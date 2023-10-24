## 前端技术
 
- 基础框架：[ant-design-vue](https://github.com/vueComponent/ant-design-vue) - Ant Design Of Vue 实现
- Element UI 
- JavaScript框架：Vue
- Webpack
- node
- yarn
- eslint
- @vue/cli 3.2.1
- fullcalendar 
- v-charts
- echarts
- luckysheet
- [vue-cropper](https://github.com/xyxiao001/vue-cropper) - 头像裁剪组件
- [@antv/g2](https://antv.alipay.com/zh-cn/index.html) - Alipay AntV 数据可视化图表
- [Viser-vue](https://viserjs.github.io/docs.html#/viser/guide/installation)  - antv/g2 封装实现

## 项目地址
- Gitee：<https://gitee.com/nbacheng/ruoyi-nbcio>

## 在线演示
演示服务不限制操作，希望大家按需使用，不要恶意添加脏数据或对服务器进行攻击等操作。

[RuoYi-Nbcio 在线演示](http://122.227.135.243:9666/)

|                 | 账号  | 密码      |
|---------------- | ----- | -------- |
| 超管账户 目前只能通过gitee授权免密码登录或star后进群要密码  
| 监控中心（未运行） | ruoyi | 123456   |
| 任务调度中心      | admin | 123456   |
| 数据监控中心      | ruoyi | 123456   |

## 技术交流群

QQ交流群: 703572701 

## 友情链接
- [基于jeec-boo3.0的nbcio-boot项目](https://gitee.com/nbacheng/nbcio-boot)： NBCIO 亿事达企业管理平台。

## 开发

```bash
# 克隆项目
git clone https://gitee.com/nbacheng/ruoyi-nbcio.git

# 进入项目目录
cd ruoyi-ui

# 安装依赖
npm install

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev
```

浏览器访问 http://localhost:9666

## 发布

```bash
# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod
```