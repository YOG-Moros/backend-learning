# 模块 12 · Spring Boot 应用监控综合练习

[全部作业](../docs/assignments/README.md) · [测试结果](../docs/assignments/verification.md#12-actuator) · [实际接口响应](../docs/assignments/runtime.md#12-actuator) · [教程](../docx/12-actuator.md)

使用 Actuator 与 Micrometer 展示健康检查、应用信息、业务指标及 Prometheus 导出。**本地 17 项测试全部通过**，包含 7 项端点测试和 10 项组件测试。

## 作业描述

本作业依据模块12教程，使用Actuator与Micrometer完成Spring Boot应用监控练习。项目按需开放健康检查、应用信息、指标查询和Prometheus导出端点，并增加模拟库存服务健康检查与自定义应用信息。围绕订单业务记录成功次数、失败原因、处理耗时和处理中数量，通过实际请求观察指标变化，同时限制标签取值，避免产生过多监控序列。项目提供Prometheus抓取与告警配置示例，本地17项测试全部通过。展示页保留健康状态、指标响应和源码链接，便于直观查看监控效果。

## 完成内容与源码

| 教程内容 | 实现与批改入口 |
| --- | --- |
| 端点选择性暴露 | [application.yml](src/main/resources/application.yml)：只开放 health、info、metrics、prometheus；env 访问返回 404 |
| 自定义健康检查 | [InventoryHealthIndicator](src/main/java/top/cch/actuator/health/InventoryHealthIndicator.java)：通过配置模拟库存依赖正常/异常，返回判断依据 |
| 自定义应用信息 | [AppInfoContributor](src/main/java/top/cch/actuator/info/AppInfoContributor.java)：模块、Profile、端口、负责人及 JVM 启动时间 |
| Counter / Timer / Gauge | [BusinessMetrics](src/main/java/top/cch/actuator/metrics/BusinessMetrics.java)：成功/失败计数、处理耗时、处理中数量 |
| 请求触发指标变化 | [BusinessController](src/main/java/top/cch/actuator/controller/BusinessController.java)：下单、慢请求、失败演示 |
| 指标标签控制 | 失败原因限定为 stock_empty、pay_timeout、invalid_request、other，避免任意输入产生无限序列 |
| Prometheus 抓取与告警 | [抓取配置](prometheus/prometheus.yml)、[告警规则](prometheus/alerts.yml) |

## 如何核对运行效果

这些操作已在本地采集响应，点开[实测记录](../docs/assignments/runtime.md#12-actuator)即可查看：

| 操作 | 观察点 |
| --- | --- |
| GET `/actuator/health` | 总体状态及 inventory 组件状态 |
| GET `/actuator/info` | 自定义 module 与 owner 信息 |
| POST `/business/orders` 三次 | 成功订单 Counter 增加 3 |
| GET `/actuator/metrics/business.orders.created` | JSON 格式的 COUNT |
| GET `/business/fail?reason=stock_empty` | 按原因记录失败次数 |
| GET `/actuator/prometheus` | Prometheus 文本格式中的 Counter / Gauge / Timer |
| GET `/actuator/env` | 未暴露端点返回 404 |

Micrometer 名称 `business.orders.created` 在本版本的 Prometheus 导出中对应 `business_orders_total`，告警使用后者。具体导出行保留在实测记录中。

## 测试设计

[端点集成测试](src/test/java/top/cch/actuator/ActuatorEndpointTest.java)验证真实 HTTP 响应和指标增量；[组件测试](src/test/java/top/cch/actuator)覆盖健康状态切换、固定启动时间、各类仪表和标签数量有界。

## 需要运行时

Java 21、Maven 3.9+，无需 MySQL、Redis。在仓库根目录执行：

```bash
mvn -pl 12-actuator spring-boot:run -Dspring-boot.run.profiles=spec
mvn -pl 12-actuator -am test
```

本机健康检查：`http://localhost:8012/actuator/health`；未启用 spec 时使用兼容端口 8013。Prometheus 示例默认抓取 8013，使用 spec 时需同步改为 8012。

已验证应用的指标导出；Prometheus 服务、告警规则的实际加载和通知投递尚未联调。库存健康检查是教学模拟，没有连接真实库存服务。
