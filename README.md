# Queue Ticketing 排队叫号系统

一套用于学习与分享的通用排队叫号系统，参考大众点评餐饮线上取号、线下叫号的业务思路，采用 **Redis Lua 原子取号 + Kafka 异步落库** 的高并发架构。

## 项目定位

- **学习项目**：覆盖 Spring Boot 3、Redis Lua、Kafka、JWT、MapStruct、SpringDoc、Vue 3。
- **高并发**：取号主链路不访问 MySQL，容量校验、防重复、号码分配在 Redis 中原子完成。
- **可演示**：内置演示数据，浏览器完成「取号 → 叫号 → 完成/过号 → 取消」全流程。
- **可扩展**：按领域模块划分，后续可平滑拆分微服务或扩展多行业。

## 架构

```
┌─────────────────────────────────────────────────────────────┐
│                    C 端 H5 / 微信小程序                       │
│         取号 / 查进度 / 取消 / 订阅提醒                       │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS
┌───────────────────────────▼─────────────────────────────────┐
│                  业务层（单体，按领域划分）                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ take-service │  │ query-service│  │ call-service │       │
│  │ 取号          │  │ 查进度        │  │ 叫号/完成/过号│       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                 │                 │               │
│         └────────┬────────┴────────┬────────┘               │
│                  ▼                 ▼                        │
│           ┌────────────┐   ┌─────────────┐                  │
│           │ Redis Lua  │   │ 内存消息队列  │                  │
│           │ 原子取号    │   │ 异步发送      │                  │
│           └────────────┘   └──────┬──────┘                  │
└───────────────────────────────────┼─────────────────────────┘
                                    ▼
                             ┌─────────────┐
                             │ Kafka       │
                             │ 取号/状态事件│
                             └──────┬──────┘
                                    ▼
                             ┌─────────────┐
                             │ Consumer    │
                             │ 异步写 MySQL │
                             └──────┬──────┘
                                    ▼
                             ┌─────────────┐
                             │ MySQL       │
                             │ 持久化兜底   │
                             └─────────────┘
```

**取号主链路顺序**

```
前端防抖 + Idempotency-Key
  → 门店令牌桶限流
  → 用户取号频控
  → Redis Lua 原子执行（容量 / 防重复 / 分配号码 / 写排队数据）
  → 返回号码
  → 内存队列 → Kafka → MySQL（异步）
```

**查询进度**直接读 Redis，不访问 MySQL。

## 技术栈

- Java 17 + Spring Boot 3.2
- MyBatis-Plus + MySQL 8
- Redis 7 + Lua 脚本
- Kafka（异步落库，可选启动）
- JWT（jjwt）
- MapStruct
- SpringDoc OpenAPI
- Vue 3 + Vite + Element Plus
- Locust（压测）

## 目录结构

```
queue-ticketing/
├── pom.xml
├── LICENSE
├── docker-compose.yml               # MySQL + Redis + Kafka
├── queue-ticketing-backend/
│   ├── sql/
│   │   └── init.sql                 # 建表 + 演示数据（只需执行一次）
│   └── src/main/
│       ├── java/com/queue/ticketing/
│       │   ├── common/
│       │   │   ├── result/          # Result、PageResult
│       │   │   ├── exception/       # BizException、错误码、全局异常
│       │   │   ├── annotation/      # Idempotent、RequireAuth、RateLimit
│       │   │   ├── ratelimit/       # 限流切面
│       │   │   ├── idempotent/      # 幂等切面
│       │   │   ├── event/           # 领域事件
│       │   │   ├── util/            # 分布式锁、JWT
│       │   │   └── web/             # 响应 advice、登录拦截器
│       │   ├── config/              # Redis、Redisson、线程池、Kafka、CORS
│       │   └── modules/
│       │       ├── auth/            # 员工与 C 端登录
│       │       ├── store/           # 商户、门店、柜台
│       │       ├── queue/
│       │       │   ├── controller/  # /api/v1/queue/*
│       │       │   ├── redis/       # Lua 执行、配置缓存、限流风控
│       │       │   ├── mq/          # Kafka 生产/消费、本地消息表
│       │       │   └── ...
│       │       ├── ticket/          # 取号单、状态机、旧版 DB 链路
│       │       └── notify/          # 通知渠道与事件监听
│       └── resources/
│           └── lua/                 # 取号/叫号/取消/完成/过号/限流脚本
├── queue-ticketing-frontend/
│   └── src/
│       ├── api/                     # request、auth、queue、ticket、queue-center
│       ├── router/
│       ├── utils/
│       └── views/
│           ├── login/Login.vue
│           ├── console/CallConsole.vue
│           ├── config/StoreConfig.vue
│           ├── simulator/TakeTicketSimulator.vue
│           └── h5/ClientTake.vue
└── loadtest/
    ├── locustfile.py                # 取号压测脚本
    ├── README.md
    └── REPORT.md                    # 压测报告
```

## 核心流程

### 取号

1. C 端登录，获取 JWT。
2. 前端生成 `Idempotency-Key`。
3. 门店令牌桶限流 + 用户频控。
4. Redis Lua 原子完成：检查队列状态、检查容量、检查同店重复、分配号码、写入排队数据。
5. 返回号码和前方人数。
6. 消息进入内存队列，异步发送 Kafka，消费者写入 MySQL。

### 叫号

1. 店员在叫号台点击对应队列的「下一位」。
2. Lua 取队首、移出队列、释放名额、标记当前叫号。
3. 发送叫号事件，异步落库。

### 取消 / 完成 / 过号

- 取消：WAITING/CALLED → CANCELLED，Lua 中释放名额。
- 完成：CALLED → SERVED，清空当前叫号。
- 过号：CALLED → MISSED，清空当前叫号，用户可重新取号。

### 状态机

```
WAITING ──叫号──> CALLED ──完成──> SERVED
   │                  │
   │──取消──> CANCELLED│──过号──> MISSED
   │                  │
   │──超时──> EXPIRED  │──取消──> CANCELLED
```

## 快速开始

### 1. 启动基础设施

只启动 MySQL 和 Redis：

```bash
docker-compose up -d mysql redis
```

需要验证异步落库时再启动 Kafka：

```bash
docker-compose up -d kafka
```

Kafka 不可用时，取号仍然成功，消息会写入 `local_message` 表，Kafka 恢复后由定时任务补发。

### 2. 初始化数据库

```bash
mysql -uroot -p < queue-ticketing-backend/sql/init.sql
```

`init.sql` 只执行一次。建表使用 `CREATE TABLE IF NOT EXISTS`，演示数据使用 `INSERT IGNORE`，重复执行不会产生重复数据。

演示数据包含：

- 商户：示例餐饮集团
- 门店：示例餐厅·人民广场店
- 柜台：1 号 / 2 号叫号台
- 队列：小桌（A）、大桌（B）
- 员工账号：`admin` / `123456`（仅演示）

### 3. 配置本地连接

```bash
cd queue-ticketing-backend/src/main/resources
cp application-dev.example.yml application-dev.yml
# 修改数据库密码，或设置环境变量 DB_PASSWORD
```

`application-dev.yml` 已被 `.gitignore` 排除，不会提交到仓库。

### 4. 启动后端

```bash
cd queue-ticketing-backend
mvn spring-boot:run
```

- 服务地址：`http://localhost:8080`
- 接口文档：`http://localhost:8080/swagger-ui/index.html`

### 5. 启动前端

```bash
cd queue-ticketing-frontend
npm install
npm run dev
```

- B 端后台：`http://localhost:5173`
- C 端 H5：`http://localhost:5173/h5`

## 演示路径

1. 打开 `http://localhost:5173`，使用 `admin / 123456` 登录。
2. 进入「门店与队列」，查看 Redis 实时队列状态。
3. 进入「取号模拟」，选择桌型，点击取号。
4. 进入「叫号控制台」，在对应队列行点击「下一位」，可完成或过号。
5. 打开 `http://localhost:5173/h5`，用手机号取号并查看进度。

## 核心 API

### 高并发取号链路

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/v1/queue/take` | 取号（Redis Lua） |
| GET | `/api/v1/queue/progress` | 查询进度（Redis） |
| GET | `/api/v1/queue/my` | 查询我的当前排队 |
| POST | `/api/v1/queue/cancel` | 取消取号 |
| POST | `/api/v1/queue/call` | 叫号 |
| POST | `/api/v1/queue/finish` | 完成服务 |
| POST | `/api/v1/queue/miss` | 过号 |
| GET | `/api/v1/queue/overview` | 门店队列总览（Redis） |

### 登录与配置

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/v1/staff/login` | 员工登录 |
| POST | `/api/v1/client/login` | C 端登录（演示） |
| GET | `/api/v1/stores/merchant/{merchantId}` | 门店列表 |
| GET | `/api/v1/queue-templates/merchant/{merchantId}` | 队列模板 |
| GET | `/api/v1/queue-instances` | 获取/创建队列实例 |

旧版 `/api/v1/tickets/*` 接口保留兼容，但不再是主链路。

## 设计要点

1. **Lua 原子取号**：容量校验、防重复、号码分配、排队写入在 Redis 中一次原子完成，不使用分布式锁。
2. **主链路不访问 MySQL**：取号只操作 Redis，MySQL 仅作为异步持久化兜底。
3. **异步落库**：取号线程只把消息放入内存队列，独立线程发送 Kafka，消费者幂等写 MySQL。
4. **最终一致**：Kafka 不可用时写 `local_message`，定时任务重发；发送失败重试 3 次后进入 `.DLQ`。
5. **多级拦截**：前端防抖 + Idempotency-Key、门店令牌桶、用户频控、Redis 防重复。
6. **降级保护**：Redis 不可用时取号返回系统繁忙；Kafka 不可用时不影响取号。
7. **按天重置**：号码序列 key 包含日期，数据 TTL 24 小时，每天从 A001/B001 重新开始。
8. **鉴权与追踪**：JWT + `@RequireAuth`，B/C 端身份隔离；`TraceIdFilter` 输出 traceId。

## 压测

压测工具：Locust

- 压测脚本：[loadtest/locustfile.py](loadtest/locustfile.py)
- 压测报告：[loadtest/REPORT.md](loadtest/REPORT.md)

结论摘要：

| 并发 | 取号失败率 | 取号 P95 |
|---|---|---|
| 500 | 0% | 420ms |
| 1000 | 0% | 56ms |

## 已知限制

- Kafka 未启动时，MySQL 落库延迟，`local_message` 会积压。
- 本地消息重试 10 次后会标记失败，需要后续改为持续重试。
- 压测覆盖取号场景，异步落库、叫号链路未做并发压测。
- 压测为单机 Locust，1000 并发时客户端 CPU 超过 90%。
- `JwtUtil` 内置默认密钥仅用于本地演示，部署前必须通过配置替换。
- 微信登录、订阅消息、支付为模拟或预留。
- 前端为最小演示页面，未做完整后台。
- 未实现多租户计费、开放 API、配置中心等 SaaS 能力。

## License

[MIT](./LICENSE)
