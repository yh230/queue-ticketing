"""
排队取号压测脚本

用法：
    locust -f locustfile.py --headless -u 200 -r 50 -t 60s --host http://127.0.0.1:8080

参数说明：
    -u 并发用户数
    -r 每秒启动用户数
    -t 持续时间
    --host 后端地址

环境变量：
    STORE_ID     门店ID，默认 1
    TEMPLATE_ID  队列模板ID，默认 1
"""

import os
import random
import time

from locust import HttpUser, between, task

STORE_ID = int(os.getenv("STORE_ID", "1"))
TEMPLATE_ID = int(os.getenv("TEMPLATE_ID", "1"))


class QueueUser(HttpUser):
    wait_time = between(0.1, 0.5)

    def on_start(self):
        self.phone = "139" + "".join(random.choices("0123456789", k=8))
        self.token = None
        self.ticket_no = None
        self.finished = False
        self.login()

    def login(self):
        with self.client.post(
            "/api/v1/client/login",
            json={"phone": self.phone},
            name="/client/login",
            catch_response=True,
        ) as resp:
            body = resp.json() if resp.status_code == 200 else {}
            if body.get("code") == 200:
                self.token = body.get("data")
                resp.success()
            else:
                resp.failure(f"login failed: {body}")

    def auth_headers(self):
        headers = {"Idempotency-Key": f"{self.phone}-{time.time_ns()}"}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        return headers

    @task(10)
    def take_ticket(self):
        if not self.token or self.ticket_no or self.finished:
            return

        with self.client.post(
            "/api/v1/queue/take",
            json={
                "storeId": STORE_ID,
                "templateId": TEMPLATE_ID,
                "userPhone": self.phone,
                "source": "MINI_PROGRAM",
            },
            headers=self.auth_headers(),
            name="/queue/take",
            catch_response=True,
        ) as resp:
            body = resp.json() if resp.status_code == 200 else {}
            if body.get("code") == 200:
                self.ticket_no = body["data"]["ticketNo"]
                resp.success()
            else:
                # 队列已满 / 重复取号属于预期业务结果，标记失败便于统计
                self.finished = True
                resp.failure(f"{body.get('code')} {body.get('message')}")

    @task(3)
    def progress(self):
        if not self.token or not self.ticket_no:
            return

        self.client.get(
            "/api/v1/queue/progress",
            params={
                "storeId": STORE_ID,
                "templateId": TEMPLATE_ID,
                "ticketNo": self.ticket_no,
            },
            headers=self.auth_headers(),
            name="/queue/progress",
        )
