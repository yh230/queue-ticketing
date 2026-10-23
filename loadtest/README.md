# 排队取号压测

使用 [Locust](https://locust.io/) 对取号接口做并发压测。

完整压测结果、问题记录和已知限制见 [REPORT.md](./REPORT.md)。

## 安装

```bash
python -m pip install locust -i https://pypi.org/simple/
```

## 运行

Web 模式（浏览器打开 `http://localhost:8089` 配置并发）：

```bash
cd loadtest
locust -f locustfile.py --host http://127.0.0.1:8080
```

无界面模式（推荐压测）：

```bash
# 200 并发，每秒启动 50 个用户，持续 60 秒
locust -f locustfile.py --headless -u 200 -r 50 -t 60s --host http://127.0.0.1:8080
```

指定门店和队列：

```bash
STORE_ID=1 TEMPLATE_ID=1 locust -f locustfile.py --headless -u 200 -r 50 -t 60s --host http://127.0.0.1:8080
```

## 脚本行为

每个虚拟用户：

1. 用随机手机号登录 C 端，获取 JWT
2. 调用 `POST /api/v1/queue/take` 取号一次
3. 取号成功后每 3 次任务调用 `GET /api/v1/queue/progress` 查询进度

取号失败（队列满、重复取号）会记为失败请求，便于统计真实成功率。

## 结果解读

Locust 控制台重点看：

| 指标 | 含义 |
|---|---|
| Requests/s | 总 TPS |
| 95%ile | P95 响应时间 |
| Failures | 失败数，取号失败会包含队列满/风控拦截 |
| `/queue/take` 行 | 取号接口单独统计 |

## 压测前准备

1. 确保后端、Redis 已启动
2. 清空之前的测试数据，避免重复取号干扰：

```bash
redis-cli --scan --pattern 'q:{1}*' | xargs -r redis-cli del
```

3. 调整队列容量，避免一开始就满：

```sql
UPDATE queue_template SET capacity = 10000 WHERE id = 1;
```

4. 如果开启限流，注意门店令牌桶默认 `200/s`、用户频控 `5 次/分钟`

## 注意

- 压测会真实写入 Redis 和 MySQL
- 建议在测试库执行，不要压生产环境
- Kafka 不可用时取号仍成功，消息会进入 `local_message` 表
