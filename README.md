# Queue Ticketing 排队叫号 SaaS 平台

类似大众点评的线上取号 + 线下排队叫号系统，支持多租户、多行业（餐饮、医院、政务等）。

## 核心能力

- **C 端微信小程序**：扫码取号、查看排队进度、取消取号、订阅提醒。
- **B 端商户后台**：门店/队列/柜台管理、叫号控制台、排队看板。
- **号码管理**：按队列原子生成排队号，状态机驱动取号→叫号→服务/过号→取消。
- **通知触达**：微信小程序订阅消息、短信（预留），支持前方 N 人提醒。
- **高并发控制**：Redis 原子计数、分布式锁、幂等、MySQL 唯一索引兜底。
- **领域事件**：基于 Spring Event 异步处理，Kafka 扩展已预留。

## 项目结构

```
queue-ticketing/
├── queue-ticketing-backend/     # Spring Boot 单体后端
│   ├── src/main/java/com/queue/ticketing/
│   │   ├── common/              # 通用工具、常量、异常、结果包装
│   │   ├── config/              # 配置类
│   │   ├── infrastructure/      # Redis、MQ、通知等基础设施
│   │   ├── modules/             # 业务领域模块
│   │   │   ├── auth/            # 账户权限
│   │   │   ├── store/           # 门店域
│   │   │   ├── queue/           # 队列域
│   │   │   ├── ticket/          # 号码域
│   │   │   └── notify/          # 通知域
│   │   └── interfaces/          # Controller 接口层
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── mapper/              # MyBatis XML
│   └── sql/
│       └── init.sql             # 建表脚本
├── queue-ticketing-frontend/    # B 端管理后台（Vue 3，预留）
└── docker-compose.yml           # 本地 MySQL + Redis + Kafka
```

## 技术栈

- Java 17 + Spring Boot 3.x
- MyBatis-Plus + MySQL 8
- Redis + Redisson
- Spring Event（领域事件，Kafka 扩展预留）
- 微信小程序订阅消息

## 核心状态机

```
WAITING --叫号--> CALLED --完成服务--> SERVED
   |                  |
   |--用户取消--> CANCELLED
   |                  |
   |                  |--过号--> MISSED --顺延/作废--> WAITING/CANCELLED
   |                  |
   |                  |--超时--> EXPIRED
```

## 关键表

- `merchant`：商户
- `store`：门店
- `counter`：柜台/窗口
- `queue_template`：队列模板
- `queue_instance`：队列实例
- `ticket`：取号单
- `subscription`：通知订阅
- `notify_log`：通知日志

## 本地开发

```bash
# 启动基础设施
docker-compose up -d

# 后端启动（需配置 application-dev.yml）
cd queue-ticketing-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# B端前端启动
cd queue-ticketing-frontend
npm install
npm run dev
```

## 核心 API 示例

### 创建商户
```bash
curl -X POST http://localhost:8080/api/v1/merchants \
  -H "Content-Type: application/json" \
  -d '{"name":"测试餐饮","industryType":"CATERING"}'
```

### 创建门店
```bash
curl -X POST http://localhost:8080/api/v1/stores \
  -H "Content-Type: application/json" \
  -d '{"merchantId":1,"name":"测试店","address":"xxx"}'
```

### 创建队列模板
```bash
curl -X POST http://localhost:8080/api/v1/queue-templates \
  -H "Content-Type: application/json" \
  -d '{"merchantId":1,"name":"小桌","prefix":"A","capacity":100}'
```

### C端取号
```bash
curl -X POST http://localhost:8080/api/v1/tickets/take \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: uuid-001" \
  -d '{"storeId":1,"templateId":1,"userId":"openid-001","source":"MINI_PROGRAM","idempotencyKey":"uuid-001"}'
```

### B端叫号
```bash
curl -X POST http://localhost:8080/api/v1/tickets/call \
  -H "Content-Type: application/json" \
  -d '{"queueInstanceId":1,"counterId":1}'
```

### 标记服务完成
```bash
curl -X POST http://localhost:8080/api/v1/tickets/1/serve
```

### 过号
```bash
curl -X POST http://localhost:8080/api/v1/tickets/miss \
  -H "Content-Type: application/json" \
  -d '{"ticketId":1}'
```

### 取消取号
```bash
curl -X POST http://localhost:8080/api/v1/tickets/cancel \
  -H "Content-Type: application/json" \
  -d '{"ticketId":1,"userId":"openid-001"}'
```

## 设计要点

1. **号码唯一性**：Redis 原子递增 + MySQL 唯一索引 `(queue_instance_id, ticket_no)` 双重保障。
2. **并发控制**：取号用 Redis 分布式锁或 Lua 脚本保证容量检查与号码生成原子性；叫号用 Redis 锁保证同队列串行。
3. **幂等性**：取号接口支持 `idempotency_key`，Redis 缓存 5 分钟，防止重复取号。
4. **领域事件**：叫号/过号/取消通过 Spring Event 发布，使用 `@TransactionalEventListener(AFTER_COMMIT)` 保证事务提交后异步通知。
5. **代码规范**：遵循阿里巴巴 Java 开发手册，使用 P3C 插件扫描。
