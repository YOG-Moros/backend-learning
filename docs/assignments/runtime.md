# 四份作业接口实测记录

采集时间：2026-11-06T16:06:38+09:00。
使用源码提交 ab71b3c 构建的 JAR 在本地临时端口启动，以下内容来自实际 HTTP 请求。
Security 使用专用学习测试数据库，短信为内存模拟；测试进程采集完成后停止。响应中的登录令牌不公开。
这些是静态结果快照，不能替代公开在线服务，也不代表 GitHub Actions 的执行结果。

## 08-security

| 请求 | 身份 | 实测 HTTP |
| --- | --- | ---: |
| GET `/stage1/hello` | 未登录 | 401 |
| GET `/stage1/hello` | alice | 200 |
| GET `/stage3/audit/logs` | carol | 403 |
| GET `/stage3/audit/logs` | bob | 200 |

登录成功：HTTP 200，返回 Bearer 令牌（已省略令牌内容）。

### alice 的课程令牌访问 /api/hello

HTTP **200**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "username": "alice",
    "authorities": [
      "ARTICLE_DELETE",
      "ROLE_USER",
      "FACTOR_PASSWORD"
    ],
    "message": "你好，alice"
  }
}
```

### alice 的课程令牌访问 /api/admin/dashboard

HTTP **403**

```json
{
  "code": 403,
  "message": "无权限访问",
  "data": null
}
```

### alice 的课程令牌访问 /api/tasks

HTTP **401**

```json
{
  "code": 401,
  "message": "未登录或令牌已失效",
  "data": null
}
```

## 10-doc

实际从 `/v3/api-docs/book` 读取到的接口说明：

| 方法 | 路径 | summary |
| --- | --- | --- |
| GET | `/books` | 分页查询图书 |
| POST | `/books` | 新增图书 |
| GET | `/books/boom` | 触发未预期异常 |
| GET | `/books/categories` | 查询全部分类 |
| GET | `/books/{id}` | 查询图书详情 |
| DELETE | `/books/{id}` | 删除图书 |

### 查询前两本图书

HTTP **200**

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "author": "周志明",
        "category": "TECH",
        "createTime": "2026-11-06T16:03:00.8383258",
        "id": 1,
        "isbn": "9787111641247",
        "price": 129.0,
        "publishDate": "2019-12-01",
        "stock": 10,
        "title": "深入理解 Java 虚拟机"
      },
      {
        "author": "Joshua Bloch",
        "category": "TECH",
        "createTime": "2026-11-06T16:03:00.8383258",
        "id": 2,
        "isbn": "9787111213828",
        "price": 89.0,
        "publishDate": "2008-01-01",
        "stock": 12,
        "title": "Effective Java"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 2
  },
  "message": "success"
}
```

## 11-test

请求：`POST /orders/calculate`

```json
{"userId": 1, "amount": 300, "memberLevel": "GOLD", "quantity": 1}
```

### GOLD 会员金额试算

HTTP **200**

```json
{
  "code": 200,
  "data": {
    "originAmount": 300,
    "discount": 53.0,
    "finalAmount": 247.0,
    "appliedRules": [
      "满 300 减 40",
      "金卡会员 95 折"
    ]
  },
  "message": "success"
}
```

### 金额为 0 的非法请求

HTTP **400**

```json
{
  "code": 400,
  "data": null,
  "message": "金额必须大于 0"
}
```

## 12-actuator

### 健康检查

HTTP **200**

```json
{
  "components": {
    "diskSpace": {
      "details": {
        "total": 257517678592,
        "free": 56617562112,
        "threshold": 10485760,
        "path": "D:\\backend-learning\\.",
        "exists": true
      },
      "status": "UP"
    },
    "inventory": {
      "details": {
        "url": "http://inventory-service/health",
        "latencyMs": 0,
        "checkedAt": 1793948799981
      },
      "status": "UP"
    },
    "livenessState": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    },
    "ssl": {
      "details": {
        "expiringChains": [],
        "invalidChains": [],
        "validChains": []
      },
      "status": "UP"
    }
  },
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

### 应用信息

HTTP **200**

```json
{
  "module": {
    "name": "13-应用监控",
    "profiles": "",
    "port": "18312",
    "startedAt": "2026-11-06T07:02:56.892Z"
  },
  "owner": {
    "team": "backend-learning",
    "contact": "cch@example.com"
  }
}
```

### 连续下单三次后的 Counter

HTTP **200**

```json
{
  "availableTags": [
    {
      "tag": "application",
      "values": [
        "13-应用监控"
      ]
    }
  ],
  "description": "累计创建成功的订单数",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 3.0
    }
  ],
  "name": "business.orders.created"
}
```

### Prometheus 业务指标导出

以下是本次响应中的业务指标行（其他 JVM/HTTP 指标省略）：

```text
# HELP business_order_handle_seconds 订单处理耗时
# TYPE business_order_handle_seconds summary
business_order_handle_seconds{application="13-应用监控",result="failure",quantile="0.5"} 0.0
business_order_handle_seconds{application="13-应用监控",result="failure",quantile="0.95"} 0.0
business_order_handle_seconds{application="13-应用监控",result="failure",quantile="0.99"} 0.0
business_order_handle_seconds_count{application="13-应用监控",result="failure"} 1
business_order_handle_seconds_sum{application="13-应用监控",result="failure"} 0.0
business_order_handle_seconds{application="13-应用监控",result="success",quantile="0.5"} 0.0
business_order_handle_seconds{application="13-应用监控",result="success",quantile="0.95"} 0.0
business_order_handle_seconds{application="13-应用监控",result="success",quantile="0.99"} 0.0
business_order_handle_seconds_count{application="13-应用监控",result="success"} 3
business_order_handle_seconds_sum{application="13-应用监控",result="success"} 0.0
# HELP business_order_handle_seconds_max 订单处理耗时
# TYPE business_order_handle_seconds_max gauge
business_order_handle_seconds_max{application="13-应用监控",result="failure"} 0.0
business_order_handle_seconds_max{application="13-应用监控",result="success"} 0.0
# HELP business_orders_total 累计创建成功的订单数
# TYPE business_orders_total counter
business_orders_total{application="13-应用监控"} 3.0
# HELP business_orders_failed_total
# TYPE business_orders_failed_total counter
business_orders_failed_total{application="13-应用监控",reason="stock_empty"} 1.0
# HELP business_orders_processing
# TYPE business_orders_processing gauge
business_orders_processing{application="13-应用监控"} 0.0
```

未暴露端点 `GET /actuator/env` 实测返回 **404**。
