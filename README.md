# Queue Ticketing 排队叫号 SaaS 平台

一套用于学习与分享的通用排队叫号系统，参考大众点评线上取号、线下叫号的业务思路，但不绑定单一行业。

## 项目定位

- **学习项目**：覆盖 Spring Boot 3、MyBatis-Plus、Redis 分布式锁、幂等、领域事件、JWT 鉴权、MapStruct、SpringDoc。
- **可演示**：内置演示数据，启动后可在浏览器完成「取号 → 叫号 → 完成/过号 → 取消」全流程。
- **可扩展**：按领域模块划分，后续可平滑拆分微服务或扩展多行业。

## 架构

```
┌─────────────────────────────────────────────────────────────┐
│                    C 端 H5 / 微信小程序                       │
│         取号 / 查进度 / 取消 / 订阅提醒                       │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS
┌───────────────────────────▼─────────────────────────────────┐
│                     Queue Ticketing Monolith                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 门店域    │ │ 队列域    │ │ 号码域    │ │ 通知域    │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐                                               │
│  │ 账户权限域│                                               │
│  └──────────┘                                               │
└──────────┬──────────────────────────────┬───────────────────┘
           │                              │
    ┌──────▼──────┐              ┌────────▼────────┐
    │    MySQL     │              │      Redis      │
    │  持久化/唯一  │              │ 号码/锁/幂等     │
    └──────────────┘              └─────────────────┘
           │
           ▼
    Spring Event（事务提交后异步通知）
```

## 技术栈

- Java 17 + Spring Boot 3.2
- MyBatis-Plus + MySQL 8
- Redis + Redisson
- JWT（jjwt）
- MapStruct
- SpringDoc OpenAPI
- Vue 3 + Vite + Element Plus

## 目录结构

```
queue-ticketing/
├── pom.xml                          # 聚合 pom
├── LICENSE
├── docker-compose.yml               # MySQL + Redis + Kafka
├── queue-ticketing-backend/
│   ├── sql/init.sql                 # 建表 + 演示数据
│   └── src/main/java/com/queue/ticketing/
│       ├── common/                  # 响应、异常、常量、锁、幂等、JWT
│       ├── config/                  # Redis、Redisson、线程池、CORS、TraceId
│       ├── modules/
│       │   ├── auth/                # 员工账号、登录上下文
│       │   ├── store/               # 商户、门店、柜台
│       │   ├── queue/               # 队列模板、实例、规则、定时任务
│       │   ├── ticket/              # 取号单、状态机、号码生成
│       │   └── notify/              # 通知渠道、通知日志、事件监听
│       └── interfaces/              # Controller、拦截器、异常处理
└── queue-ticketing-frontend/
    └── src/
        ├── api/                     # axios 封装
        ├── router/                  # 路由
        └── views/
            ├── Login.vue            # 商户登录
            ├── StoreConfig.vue      # 门店与队列配置
            ├── TakeTicketSimulator.vue # 取号模拟
            ├── CallConsole.vue      # 叫号控制台
            └── ClientTake.vue       # C 端 H5 取号页
```

## 核心流程

### 取号

1. C 端登录（演示环境用手机号换取 JWT）。
2. 校验队列状态、容量、重复取号。
3. Redis 原子递增生成号码，MySQL 唯一索引兜底。
4. 写入取号单，发送取号成功事件。
5. 返回号码、前方人数、预计等待时间。

### 叫号

1. 店员在控制台点击「下一位」。
2. 按 `updated_at` 取最老 WAITING 单据。
3. 乐观锁更新为 CALLED，记录柜台 ID。
4. 更新队列当前叫号与等待人数。
5. 发布叫号事件，通知用户及前方 N 人。

### 过号 / 取消

- 过号：CALLED → MISSED，按规则顺延或作废。
- 取消：WAITING/CALLED → CANCELLED，同步等待人数。

### 状态机

```
WAITING ──叫号──> CALLED ──完成──> SERVED
   │                  │
   │──取消──> CANCELLED│──过号──> MISSED ──顺延──> WAITING
   │                  │
   │──超时──> EXPIRED  │──取消──> CANCELLED
```

## 快速开始

### 1. 启动基础设施

```bash
docker-compose up -d
```

如果本机已有 MySQL/Redis，可跳过此步，直接修改 `application-dev.yml` 连接信息。

### 2. 初始化数据库

`docker-compose` 会自动执行 `queue-ticketing-backend/sql/init.sql`。手动执行：

```bash
mysql -uroot -p < queue-ticketing-backend/sql/init.sql
```

演示数据包含：

- 商户：示例餐饮集团
- 门店：示例餐厅·人民广场店
- 柜台：1 号 / 2 号叫号台
- 队列：小桌（A）、大桌（B）
- 今日队列实例：ID 1 / 2
- 员工账号：`admin` / `123456`

### 3. 启动后端

```bash
cd queue-ticketing-backend
mvn spring-boot:run
```

- 服务地址：`http://localhost:8080`
- 接口文档：`http://localhost:8080/swagger-ui/index.html`

### 4. 启动前端

```bash
cd queue-ticketing-frontend
npm install
npm run dev
```

- B 端后台：`http://localhost:5173`
- C 端 H5：`http://localhost:5173/h5`

## 演示路径

1. 打开 `http://localhost:5173`，使用 `admin / 123456` 登录。
2. 进入「取号模拟」，输入手机号，选择门店 1、队列模板 1，点击取号。
3. 进入「叫号控制台」，队列实例填 1，柜台填 1，点击「下一位」。
4. 可标记完成或过号。
5. 打开 `http://localhost:5173/h5`，用手机号取号，查看进度。

## 核心 API

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/v1/staff/login` | 员工登录 |
| POST | `/api/v1/client/login` | C 端登录（演示） |
| POST | `/api/v1/tickets/take` | 取号 |
| GET | `/api/v1/tickets/{id}` | 查询取号单 |
| POST | `/api/v1/tickets/cancel` | 取消取号 |
| POST | `/api/v1/tickets/call` | 叫号 |
| POST | `/api/v1/tickets/miss` | 过号 |
| POST | `/api/v1/tickets/{id}/serve` | 标记完成 |
| GET | `/api/v1/stores/merchant/{merchantId}` | 门店列表 |
| GET | `/api/v1/queue-templates/merchant/{merchantId}` | 队列模板 |
| GET | `/api/v1/queue-instances` | 获取/创建队列实例 |

## 设计要点

1. **号码唯一**：Redis `INCR` + MySQL 唯一索引 `(queue_instance_id, ticket_no)` 双重保障。
2. **并发控制**：Redisson 分布式锁；叫号使用乐观锁 + 重试，支持多柜台并发。
3. **锁与事务顺序**：先加锁，再通过自注入代理进入事务，避免锁等待占用数据库连接。
4. **幂等**：取号接口基于 `Idempotency-Key`，Redis 缓存 5 分钟。
5. **领域事件**：`@TransactionalEventListener(AFTER_COMMIT)` + `@Async`，事务提交后异步通知。
6. **鉴权**：JWT + `@RequireAuth` 拦截器，B 端与 C 端身份隔离。
7. **可观测**：`TraceIdFilter` + MDC，日志与响应头携带 traceId。

## 已知限制

- 未编写单元测试与并发测试。
- 微信登录、订阅消息、支付为模拟或预留。
- 前端为最小演示页面，未做完整后台。
- 未实现多租户计费、开放 API、配置中心等 SaaS 能力。

后续改造方向见 `C:\Users\yh\.qoder\plans\queue-ticketing-saas-roadmap.md`。

## License

[MIT](./LICENSE)
