# 排队取号压测报告

## 1. 结论摘要

| 项目 | 结果 |
|---|---|
| 测试范围 | 取号、查询进度、重复取号、容量上限 |
| 最高并发 | 1000 |
| 取号失败率 | 0% |
| 取号 P95（1000 并发） | 56ms |
| 查询进度 P95（1000 并发） | 40ms |
| 重复取号拦截 | 通过，返回 1102 |
| 容量上限拦截 | 通过，返回 1004 |
| 异步落库 | **未验证**（Kafka 未启动） |

取号主链路满足单门店瞬时 1000 人取号场景。异步落库链路不在本次验证范围内。

## 2. 测试环境

| 项 | 配置 |
|---|---|
| 操作系统 | Windows 单机 |
| 后端 | Java 17 + Spring Boot 3.2.5 |
| 数据库 | MySQL 8 |
| 缓存 | Redis 7.2.16 |
| 消息队列 | Kafka 未启动 |
| 压测工具 | Locust 2.47.0 + Python 3.12 |
| 压测模式 | 单机、无界面模式 |

## 3. 测试范围

**覆盖**

- `POST /api/v1/queue/take` 取号
- `GET /api/v1/queue/progress` 查询进度
- `POST /api/v1/client/login` 压测用户登录
- 重复取号拦截
- 队列容量上限拦截
- Redis 号码分配与前方人数

**不覆盖**

- Kafka 异步落库与消费者
- 本地消息补偿重试
- 叫号、取消、完成、过号
- 前端页面
- 服务端 CPU、线程池、连接池、Redis QPS 指标

## 4. 压测场景与结果

压测脚本：`locustfile.py`

每个虚拟用户的流程：

1. 随机手机号登录 C 端
2. 取号一次
3. 取号成功后轮询查询进度

### 4.1 500 并发

```bash
locust -f locustfile.py --headless -u 500 -r 100 -t 30s --host http://localhost:8080
```

| 接口 | 请求数 | 失败 | P50 | P95 | Max |
|---|---|---|---|---|---|
| `/queue/take` | 500 | 0 | 25ms | 420ms | 478ms |
| `/queue/progress` | 10160 | 0 | 6ms | 18ms | 97ms |
| `/client/login` | 500 | 0 | 150ms | 280ms | 380ms |
| 汇总 | 11160 | 0 | 6ms | 76ms | 480ms |

总 TPS：375

### 4.2 1000 并发

```bash
locust -f locustfile.py --headless -u 1000 -r 200 -t 30s --host http://localhost:8080
```

| 接口 | 请求数 | 失败 | P50 | P95 | Max |
|---|---|---|---|---|---|
| `/queue/take` | 1000 | 0 | 21ms | 56ms | 114ms |
| `/queue/progress` | 19884 | 0 | 10ms | 40ms | 290ms |
| `/client/login` | 1000 | 0 | 250ms | 600ms | 860ms |
| 汇总 | 21884 | 0 | 11ms | 76ms | 860ms |

总 TPS：726

### 4.3 修复前后对比

| 指标 | 修复前（500 并发） | 修复后（500 并发） |
|---|---|---|
| 取号失败率 | 47.6% | 0% |
| 取号 P50 | 2000ms | 25ms |
| 取号 P95 | 2155ms | 420ms |

## 5. 边界验证

### 5.1 同一手机号重复取号

```bash
# 第一次取号：成功
{"code":200,"data":{"ticketNo":"A2300","status":"WAITING"}}

# 第二次取号：被拦截
{"code":1102,"message":"您已有进行中的排队号码"}
```

结果：通过。

### 5.2 队列容量上限

创建一个容量为 3 的临时队列，使用 4 个不同手机号依次取号：

| 用户 | 结果 |
|---|---|
| 用户 1 | 成功，C001，前方 0 人 |
| 用户 2 | 成功，C002，前方 1 人 |
| 用户 3 | 成功，C003，前方 2 人 |
| 用户 4 | 失败，`1004 队列已满，请稍后再试` |

结果：通过。

### 5.3 查询进度一致性

| 号码 | 状态 | 前方人数 |
|---|---|---|
| C001 | WAITING | 0 |
| C003 | WAITING | 2 |

结果：通过，与 Redis 队列顺序一致。

## 6. 压测中发现的问题与修复

### 6.1 排队 Key 写入后立即过期

**现象**

取号接口返回成功，但 Redis 中 `meta`、`users`、`queue` key 不存在，`expired_keys` 持续增长。

**原因**

`take_ticket.lua` 中 `EXPIRE` 的 ttl 参数实际生效为 0，key 写入后立即被删除。

**修复**

移除 Lua 内所有 `EXPIRE`，改由 Java 在取号成功后显式设置 24 小时 TTL。

提交：`fd745b4`

### 6.2 Kafka 发送阻塞取号线程

**现象**

取号 P50 达到 2000ms，正好等于 Kafka `max.block.ms`。

**原因**

事件线程池被并发请求打满，`CallerRunsPolicy` 把 Kafka 发送任务回退到取号请求线程，Kafka 不可达时阻塞 2 秒。

**修复**

- 取号线程只把消息放入内存队列，O(1)
- 独立发送线程池消费内存队列
- 发送失败或缓冲区满时写本地消息表
- Kafka `max.block.ms` 降到 500ms

提交：`f39e16d`、`829221a`

## 7. 已知限制

1. **异步落库未验证**
   - Kafka 未启动
   - MySQL `ticket` 表今日新增 0 条
   - `local_message` 存在积压消息
   - 查询进度走 Redis，因此不受影响

2. **本地消息可能永久失败**
   - 当前重试 10 次后标记为失败
   - Kafka 长时间不可用时不会自动恢复
   - 需要后续改为持续指数退避重试

3. **压测客户端瓶颈**
   - 1000 并发时 Locust 进程 CPU 超过 90%
   - 响应时间可能被客户端限制，服务端实际余量更大

4. **Redis 历史数据未清理**
   - 压测前清理含花括号的 key 未成功
   - `aheadCount` 数值包含历史排队数据
   - 不影响成功率和响应时间统计

5. **未采集服务端指标**
   - 没有 Tomcat 线程池、HikariCP、Redis QPS、JVM GC 数据
   - 无法精确判断服务端瓶颈

6. **未做阶梯压测**
   - 只有 500 和 1000 两组数据
   - 未定位性能拐点

7. **取号 TPS 统计口径**
   - 脚本每个用户只取一次号
   - 报告中的 TPS 包含登录和进度查询，不是纯取号 TPS

## 8. 复现步骤

### 8.1 准备

```bash
# 安装 Locust
python -m pip install locust -i https://pypi.org/simple/

# 调整队列容量，避免压测前就满
UPDATE queue_template SET capacity = 10000 WHERE id IN (1, 2);
```

### 8.2 启动后端

确保后端、Redis、MySQL 已启动。Kafka 可不启动，取号会降级写本地消息表。

### 8.3 执行压测

```bash
cd loadtest
locust -f locustfile.py --headless -u 1000 -r 200 -t 30s --host http://localhost:8080
```

### 8.4 边界验证

```bash
# 同一手机号重复取号
curl -X POST http://localhost:8080/api/v1/queue/take \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -H "Idempotency-Key: dup-check-001" \
  -d '{"storeId":1,"templateId":1,"userPhone":"13911111111","source":"MINI_PROGRAM"}'

# 查询进度
curl "http://localhost:8080/api/v1/queue/progress?storeId=1&templateId=1&ticketNo=A001" \
  -H "Authorization: Bearer <token>"
```

## 9. 后续建议

1. 启动 Kafka 后验证异步落库和本地消息补偿
2. 将本地消息重试改为持续指数退避，避免永久失败
3. 补充服务端指标采集
4. 做 100/300/500/800/1000 阶梯压测，定位拐点
5. 压测前清理 Redis 历史数据，保证 `aheadCount` 准确
6. 补充叫号、取消、完成、过号链路压测
