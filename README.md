# openCallHub
OpenCallHub 是一个强大且灵活的开源呼叫中心解决方案，旨在提供全面的客户服务和支持功能

#### [演示地址](http://59.110.143.217/) `账号admin/12345678`
软话机登录1000账号和1001账号，然后1000号发起呼叫，接听1001号，挂断。 如需体验联系作者

#### 在线收集需求中。。。。。。。当前mrcp模块已经集成sip和mrcpV2协议，意在方便对接各大厂商的语音服务，待完善，有能力者可提交PR共同完善

#### 1、项目结构

    |--- och-api      接口服务
    |--- och-common   基础功能模块
    |--- och-esl      ESL客户端模块
    |--- och-file     文件模块
    |--- och-ivr     ivr模块
    |--- och-mrcp    mrcp模块
    |--- och-security 安全模块
    |--- och-system   系统模块

#### 2、后端技术栈
- java17
- SpringBoot v3.3.1
- MySQL + MyBatis-Plus
- Redis 
- RabbitMQ
- ESL 
- SpringSecurity + JWT
- WebSocket
- kamailio
- freeswitch
- Netty


#### 3、快速开始
1. 克隆项目到本地
```bash
git clone https://github.com/iFindCallCenter/openCallHub.git
cd openCallHub
```
2. 数据表创建
```bash
openCallHub/doc/system.sql 创建数据库表
```
3. 配置
``application.yml文件 数据库配置 redis配置``
4. 编译项目
```bash
mvn clean install
java -jar och-api.0.0.1.jar
```

#### 4、开发计划
1. 后台管理-----------------开发中
2. 技能组-----------------开发中
3. ivr流程-----------------开发中
4. mrcp实现 ---------------已完成
5. 外呼任务
6. 电话条集成
7. 报表

##### 5、 [前端地址](https://gitee.com/zhongjiawei999/waihu-app) `https://gitee.com/zhongjiawei999/waihu-app`

#### 6、 联系方式： 有能力可加微信交流，有线路需求也可以咨询

![微信](image.png)

##### 7. **说明：** 本项目仅供学习交流使用，请勿用于非法用途，否则后果自负。
  