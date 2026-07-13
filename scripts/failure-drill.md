# openCallHub 故障演练文档

## 演练目标
验证系统在常见故障场景下的容错能力和恢复流程。

---

## 场景 1: MySQL 主库宕机

**模拟**: `docker stop och-mysql`

**预期行为**:
- [ ] API 返回 503，健康检查 `/api/foundation/health` 显示数据库 DOWN
- [ ] 已有通话不受影响（状态保留在 Redis）
- [ ] 外呼任务自动暂停

**恢复**: `docker start och-mysql`

**验证**:
- [ ] Flyway 迁移自动重跑（如有未应用的迁移）
- [ ] 健康检查恢复 UP
- [ ] 外呼任务可手动恢复

---

## 场景 2: FreeSWITCH ESL 断连

**模拟**: `docker stop freeswitch`

**预期行为**:
- [ ] ESL 事件停止接收，现有通话保持在当前状态
- [ ] 健康检查显示 FreeSWITCH DOWN
- [ ] 告警触发（如配置了 Alertmanager）

**恢复**: `docker start freeswitch`

**验证**:
- [ ] ESL 自动重连
- [ ] 健康检查恢复
- [ ] 新呼叫路由正常

---

## 场景 3: RabbitMQ 不可用

**模拟**: `docker stop och-rabbitmq`

**预期行为**:
- [ ] 业务主流程仍可执行（在线事务不受影响）
- [ ] 领域事件发布失败，事件进入 outbox 待投递
- [ ] 异步报表暂停聚合

**恢复**: `docker start och-rabbitmq`

**验证**:
- [ ] outbox 中的事件自动重新投递
- [ ] 报表聚合恢复
- [ ] 健康检查显示 MQ UP

---

## 场景 4: 磁盘空间不足

**模拟**: 填充录音目录 `dd if=/dev/zero of=./data/files/large_file bs=1M count=10000`

**预期行为**:
- [ ] 录音写入失败记录错误日志
- [ ] 健康检查显示文件存储 DOWN
- [ ] 告警触发

**恢复**: `rm ./data/files/large_file`

---

## 场景 5: 网络分区（Redis 失联）

**模拟**: `docker network disconnect och-net och-redis`

**预期行为**:
- [ ] 分布式锁获取失败，并发操作降级为单节点串行
- [ ] 坐席状态缓存失效，使用数据库直查
- [ ] 健康检查显示 Redis DOWN

**恢复**: `docker network connect och-net och-redis`

---

## 演练记录模板

| 日期 | 场景 | 操作人 | 预期符合 | 恢复时间 | 备注 |
|------|------|--------|----------|----------|------|
| 2026-06-03 | MySQL宕机 | 张三 | ✅ | 45s | MySQL 自动重启 |
| | | | | | |
