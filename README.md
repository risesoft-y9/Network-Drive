<p align="center">
 <img alt="logo" src="https://vue.youshengyun.com/files/img/qrCodeLogo.png">
</p>
<p align="center">基于SpringBoot+Vue前后端分离的网络硬盘</p>
<p align="center">
 <a href='https://gitee.com/risesoft-y9/y9-storage/stargazers'><img src='https://gitee.com/risesoft-y9/y9-storage/badge/star.svg?theme=dark' alt='star'></img></a>
    <img src="https://img.shields.io/badge/version-v9.6.9-yellow.svg">
    <img src="https://img.shields.io/badge/Spring%20Boot-2.7-blue.svg">
    <img alt="logo" src="https://img.shields.io/badge/Vue-3.3-red.svg">
    <img alt="" src="https://img.shields.io/badge/JDK-11-green.svg">
    <a href="https://gitee.com/risesoft-y9/y9-core/blob/master/LICENSE">
<img src="https://img.shields.io/badge/license-GPL3-blue.svg"></a>
<img src="https://img.shields.io/badge/total%20lines-367k-blue.svg">
</p>

## 简介

网络硬盘是通过存储、分类、检索、分享、协作、下发、回收、展示等方式管理文档、文件、图片、音频、视频等资料的工具。网络硬盘擅长在国产的私有化环境中管控文档权限、存储空间分配、安全加密、链接分享，同时支持一定轻量级的文件任务收发。网络硬盘需要依赖开源的数字底座进行人员岗位管控。[系统在线体验----->>>>>](#在线体验)

## 开源地址

源码地址：<https://github.com/risesoft-y9/Network-Drive>

## 源码目录

```
y9-storage -- 网络硬盘模块
 ├── risenet-y9boot-webapp-storage -- 网络硬盘webapp
vue -- 前端工程
 ├── y9vue-storage  -- 网络硬盘前端工程
```

## 产品特点

### 多空间管理模式

具备“我的文件”、“部门文件”、“公共文件”、“共享空间”多种默认的管理和分享文件模式，可以适应绝大多数的机构内部管理方法和制度。

### 国产私有化部署

网络硬盘支持在国产化环境中进行私有化部署并且支持容器化集群。

### 权限与存储分配

网络硬盘运用开源数字底座的人员权限管理，同时内部自带对于用户的存储空间限制，可以高效且安全的完成人员、存储相关的分配。

### 文件任务上报

支持文件任务发布和机构人员上报文件的功能，以多个独立单体文件为核心完成简单的文件任务和文件采集工作。

### 安全加密分享

支持内部共享、生成外部直链、文件夹加密、直链加密等各种安全功能，从而在共享的基础上防止文件的泄露与外扩。

## 功能架构图

![功能架构图](https://vue.youshengyun.com/files/img/网络硬盘功能架构图.png)

## 功能描述

| 序&nbsp;号 | 特&nbsp;点&nbsp;名&nbsp;称 | 特点描述                                                  |
|----------|------------------------|-------------------------------------------------------|
| 1        | 我的文件                   | 显示我新建的文件夹和上传的文件                                       |
| 2        | 共享空间                   | 显示我共享给其他人的和他人共享给我的文件夹和文件，支持文件下载、删除、收藏等操作              |
| 3        | 共享记录                   | 显示我共享给其他人的文件记录                                        |
| 4        | 部门文件                   | 显示当前人所在部门内所有人上传和新建的文件夹或文件                             |
| 5        | 文件上报                   | 显示所有人文件上报记录，支持文件下载、删除、收藏等操作                           |
| 6        | 文件上报管理                 | 管理文件上报记录，支持对文件上报进行删除、下载、收藏、分类、移动等操作                   |
| 7        | 公共文件                   | 显示公共文件夹和文件，支持文件下载、收藏等操作                               |
| 8        | 公共文件管理                 | 管理公共文件夹和文件，支持对公共文件夹和文件进行删除、下载、收藏、分类、移动等操作             |
| 9        | 我的收藏                   | 显示我收藏的文件夹和文件，支持文件下载、删除、收藏等操作                          |
| 10       | 我的回收站                  | 显示我删除的文件夹和文件，支持文件删除、恢复等操作                             |
| 11       | 存储空间管理                 | 管理存储空间，支持对每个人的存储空间进行扩容、缩容、删除等操作                       |
| 12       | 新建文件夹                  | 支持在线新建文件夹                                             |
| 13       | 上传文件                   | 支持在线上传文件                                              |
| 14       | 下载文件                   | 支持在线下载文件                                              |
| 15       | 分享文件                   | 支持将文件分享给其他人                                           |   
| 16       | 删除文件                   | 支持在线删除文件                                              |
| 17       | 移动文件                   | 支持在线移动文件到指定文件夹                                        |
| 18       | 文件夹加密                  | 支持对文件夹进行加密，加密后文件无法下载、删除、分享、移动等操作                      |
| 19       | 文件收藏                   | 支持对文件进行收藏，收藏的文件可以在“我的收藏”中查看                           |
| 20       | 直连下载                   | 支持直连下载文件，无需登录，可加密文件，下载时，需输入密码下载文件                     |
| 21       | 文件搜索                   | 支持在文件列表中进行搜索，支持模糊搜索、全文搜索、日期范围搜索、文件类型搜索、文件大小搜索、文件标签搜索等 |
| 22       | 文件分享                   | 支持将文件分享给其他人                                           |

## 后端技术选型

| 序号 | 依赖              | 版本      | 官网                                                                                                                 |
|----|-----------------|---------|--------------------------------------------------------------------------------------------------------------------|
| 1  | Spring Boot     | 2.7.10  | <a href="https://spring.io/projects/spring-boot" target="_blank">官网</a>                                            |
| 2  | SpringDataJPA   | 2.7.10  | <a href="https://spring.io/projects/spring-data-jpa" target="_blank">官网</a>                                        |
| 3  | SpringDataRedis | 2.7.10  | <a href="https://spring.io/projects/spring-data-redis" target="_blank">官网</a>                                      |
| 4  | SpringKafka     | 2.8.11  | <a href="https://spring.io/projects/spring-kafka" target="_blank">官网</a>                                           |
| 5  | nacos           | 2.2.1   | <a href="https://nacos.io/zh-cn/docs/v2/quickstart/quick-start.html" target="_blank">官网</a>                        |
| 6  | druid           | 1.2.16  | <a href="https://github.com/alibaba/druid/wiki/%E9%A6%96%E9%A1%B5" target="_blank">官网</a>                          |
| 7  | Jackson         | 2.13.5  | <a href="https://github.com/FasterXML/jackson-core" target="_blank">官网</a>                                         |
| 8  | javers          | 6.13.0  | <a href="https://github.com/javers/javers" target="_blank">官网</a>                                                  |
| 9  | lombok          | 1.18.26 | <a href="https://projectlombok.org/" target="_blank">官网</a>                                                        |
| 10 | logback         | 1.2.11  | <a href="https://www.docs4dev.com/docs/zh/logback/1.3.0-alpha4/reference/introduction.html" target="_blank">官网</a> | 

## 前端技术选型

| 序号 | 依赖           | 版本      | 官网                                                                     |
|----|--------------|---------|------------------------------------------------------------------------|
| 1  | vue          | 3.3.2   | <a href="https://cn.vuejs.org/" target="_blank">官网</a>                 |
| 2  | vite2        | 2.9.13  | <a href="https://vitejs.cn/" target="_blank">官网</a>                    |
| 3  | vue-router   | 4.0.13  | <a href="https://router.vuejs.org/zh/" target="_blank">官网</a>          |
| 4  | pinia        | 2.0.11  | <a href="https://pinia.vuejs.org/zh/" target="_blank">官网</a>           |
| 5  | axios        | 0.24.0  | <a href="https://www.axios-http.cn/" target="_blank">官网</a>            |
| 6  | typescript   | 4.5.4   | <a href="https://www.typescriptlang.org/" target="_blank">官网</a>       |
| 7  | core-js      | 3.20.1  | <a href="https://www.npmjs.com/package/core-js" target="_blank">官网</a> |
| 8  | element-plus | 2.2.29  | <a href="https://element-plus.org/zh-CN/" target="_blank">官网</a>       |
| 9  | sass         | 1.58.0  | <a href="https://www.sass.hk/" target="_blank">官网</a>                  |
| 10 | animate.css  | 4.1.1   | <a href="https://animate.style/" target="_blank">官网</a>                |
| 11 | vxe-table    | 4.3.5   | <a href="https://vxetable.cn" target="_blank">官网</a>                   |
| 12 | echarts      | 5.3.2   | <a href="https://echarts.apache.org/zh/" target="_blank">官网</a>        |
| 13 | svgo         | 1.3.2   | <a href="https://github.com/svg/svgo" target="_blank">官网</a>           |
| 14 | lodash       | 4.17.21 | <a href="https://lodash.com/" target="_blank">官网</a>                   |

## 中间件选型

| 序号 | 工具               | 版本   | 官网                                                                        |
|----|------------------|------|---------------------------------------------------------------------------|
| 1  | JDK              | 11   | <a href="https://openjdk.org/" target="_blank">官网</a>                     |
| 2  | Tomcat           | 9.0+ | <a href="https://tomcat.apache.org/" target="_blank">官网</a>               | 
| 3  | filezilla server | 1.7+ | <a href="https://www.filezilla.cn/download/server" target="_blank">官网</a> |

## 数据库选型

| 序号 | 工具    | 版本         | 官网                                                         |
|----|-------|------------|------------------------------------------------------------|
| 1  | Mysql | 5.7 / 8.0+ | <a href="https://www.mysql.com/cn/" target="_blank">官网</a> |
| 2  | Redis | 6.2+       | <a href="https://redis.io/" target="_blank">官网</a>         |

## 信创兼容适配

| 序号 | 类型   | 对象                 |
|:---|------|--------------------|
| 1  | 浏览器  | 奇安信、火狐、谷歌、360等     |
| 2  | 插件   | 金山、永中、数科、福昕等       |
| 3  | 中间件  | 东方通、金蝶、宝兰德等        |
| 4  | 数据库  | 人大金仓、达梦、高斯等        |
| 5  | 操作系统 | 统信、麒麟、中科方德等        |
| 6  | 芯片   | ARM体系、MIPS体系、X86体系 |

## 在线体验

### 网络硬盘

**演示地址
**：<https://demo.youshengyun.com/storage/>

> 演示账号：user 密码：Risesoft@2024
>

## 文档专区

开发文档：https://docs.youshengyun.com/

| 序号 | 名称                                                                                                                  |
|----|---------------------------------------------------------------------------------------------------------------------|
| 1  | <a href="https://vue.youshengyun.com/files/内部Java开发规范手册.pdf" target="_blank">内部Java开发规范手册</a>                       |
| 2  | <a href="https://vue.youshengyun.com/files/日志组件使用文档.pdf" target="_blank">日志组件使用文档</a>                               |
| 3  | <a href="https://vue.youshengyun.com/files/文件组件使用文档.pdf" target="_blank">文件组件使用文档</a>                               |
| 4  | <a href="https://vue.youshengyun.com/files/代码生成器使用文档.pdf" target="_blank">代码生成器使用文档</a>                             |
| 5  | <a href="https://vue.youshengyun.com/files/配置文件说明文档.pdf" target="_blank">配置文件说明文档</a>                               |
| 6  | <a href="https://vue.youshengyun.com/files/常用工具类使用示例文档.pdf" target="_blank">常用工具类使用示例文档</a>                         |
| 7  | <a href="https://vue.youshengyun.com/files/有生博大Vue开发手册v1.0.pdf" target="_blank">前端开发手册</a>                          |
| 8  | <a href="https://vue.youshengyun.com/files/开发规范.pdf" target="_blank">前端开发规范</a>                                     |
| 9  | <a href="https://vue.youshengyun.com/files/代码格式化.pdf" target="_blank">前端代码格式化</a>                                   |
| 10 | <a href="https://vue.youshengyun.com/files/系统组件.pdf" target="_blank">前端系统组件</a>                                     |
| 11 | <a href="https://vue.youshengyun.com/files/通用方法.pdf" target="_blank">前端通用方法</a>                                     |
| 12 | <a href="https://vue.youshengyun.com/files/国际化.pdf" target="_blank">前端国际化</a>                                       |
| 13 | <a href="https://vue.youshengyun.com/files/Icon图标.pdf" target="_blank">前端Icon图标</a>                                 |
| 14 | <a href="https://vue.youshengyun.com/files/单点登录对接文档.pdf" target="_blank">单点登录对接文档</a>                               |
| 15 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘源码部署文档.pdf" target="_blank">网络硬盘源码部署文档</a>                   | |
| 16 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘手机端接口.pdf" target="_blank">网络硬盘手机端接口</a>                     |
| 17 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘Oracle数据库适配文档.pdf" target="_blank">Oracle数据库适配文档</a>         |
| 18 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘Dameng数据库适配文档.pdf" target="_blank">Dameng数据库适配文档</a>         |
| 19 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘PostgreSQL数据库适配文档.pdf" target="_blank">PostgreSQL数据库适配文档</a> |
| 20 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘KingBase数据库适配文档.pdf" target="_blank">Kingbase数据库适配文档</a>     |
| 21 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘Mariadb数据库适配文档.pdf" target="_blank">Mariadb数据库适配文档</a>       |
| 22 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘OceanBase数据库适配文档.pdf" target="_blank">OceanBase数据库适配文档</a>   |
| 23 | <a href="https://vue.youshengyun.com/files/storage/网络硬盘MySQL数据库适配文档.pdf" target="_blank">MySQL数据库适配文档</a>           | 
| 24 | <a href="https://docs.youshengyun.com/digitalbase/backend/hole/holeInfo" target="_blank">安全扫描漏洞问题及解决方案文档</a>        | 

## 网络硬盘截图

![文件列表](https://vue.youshengyun.com/files/img/网络硬盘文件列表.png)

![空间管理](https://vue.youshengyun.com/files/img/网络硬盘空间管理.png)

![文件共享](https://vue.youshengyun.com/files/img/网络硬盘文件共享.png)

![生成直链](https://vue.youshengyun.com/files/img/网络硬盘生成直链.png)

## 依赖开源项目

| 序&nbsp;号 | 项&nbsp;目&nbsp;&nbsp;名&nbsp;称 | 项目介绍                                                                                                                                         | 地&nbsp;址                                                                                                                                                          |
|----------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1        | 数字底座                         | 数字底座是一款面向大型政府、企业数字化转型，基于身份认证、组织架构、岗位职务、应用系统、资源角色等功能构建的统一且安全的管理支撑平台。数字底座基于三员管理模式，具备微服务、多租户、容器化和国产化，支持用户利用代码生成器快速构建自己的业务应用，同时可关联诸多成熟且好用的内部生态应用 | <a href="https://gitee.com/risesoft-y9/y9-core" target="_blank">码云</a> <a href="https://github.com/risesoft-y9/Digital-Infrastructure" target="_blank">GitHub</a> |

## 赞助与支持

### 中关村软件和信息服务产业创新联盟

官网：<a href="https://www.zgcsa.net" target="_blank">https://www.zgcsa.net</a>

### 北京有生博大软件股份有限公司

官网：<a href="https://www.risesoft.net/" target="_blank">https://www.risesoft.net/</a>

### 中国城市发展研究会

官网：<a href="https://www.china-cfh.com/" target="_blank">https://www.china-cfh.com/</a>

## 咨询与合作

联系人：曲经理

微信号：qq349416828

备注：开源咨询-姓名
<div><img style="width: 40%" src="https://vue.youshengyun.com/files/img/曲经理统一二维码咨询.png"><div/>
联系人：有生博大-咨询热线


座机号：010-86393151
<div><img style="width: 45%" src="https://vue.youshengyun.com/files/img/有生博大-咨询热线.png"><div/>


