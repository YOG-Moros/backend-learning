# 模块 11 · Spring Boot 测试综合练习

[全部作业](../docs/assignments/README.md) · [测试明细与覆盖率](../docs/assignments/verification.md#11-test) · [实际接口响应](../docs/assignments/runtime.md#11-test) · [教程](../docx/11-test.md)

围绕订单金额试算，使用 JUnit 5、Mockito、MockMvc 与 Spring Boot 完成三层测试。**本地 28 项测试全部通过**：Service 16 项、Web 切片 9 项、集成测试 3 项。

## 作业描述

本作业依据模块11教程，围绕订单金额试算完成单元测试、Web切片测试与集成测试。业务包含满减优惠、会员折扣、金额精度处理和库存校验，测试覆盖优惠边界、非法参数、库存不足及异常响应等场景。使用JUnit编写用例，通过Mockito模拟外部依赖，并利用MockMvc检查请求处理与返回结果。项目接入JaCoCo生成覆盖率报告，本地28项测试全部通过，核心订单服务行覆盖率为100%、分支覆盖率约91.7%。展示页提供测试源码、覆盖率摘要和接口实测结果，方便逐项核验。

## 三层测试与源码

| 层次 | 测试方式 | 批改入口与重点 |
| --- | --- | --- |
| 单元测试 | JUnit 5 + Mockito，不启动 Spring | [OrderServiceTest](src/test/java/top/cch/test/service/OrderServiceTest.java)：满减边界、金卡叠加、精度、非法金额、库存不足、依赖调用 |
| Web 切片 | `@WebMvcTest` + `@MockitoBean` + MockMvc | [OrderControllerTest](src/test/java/top/cch/test/controller/OrderControllerTest.java)：参数校验、HTTP 状态、JSON 结构、异常转换 |
| 集成测试 | `@SpringBootTest` + MockMvc | [OrderIntegrationTest](src/test/java/top/cch/test/OrderIntegrationTest.java)：真实 Controller → Service → 库存客户端调用链 |
| 覆盖率 | JaCoCo | [Maven 配置](pom.xml)、[本地覆盖率摘要](../docs/assignments/verification.md#11-test-覆盖率) |

## 被测业务规则

源码：[OrderService](src/main/java/top/cch/test/service/OrderService.java) · [OrderController](src/main/java/top/cch/test/controller/OrderController.java) · [请求 DTO](src/main/java/top/cch/test/dto/OrderRequest.java)

| 输入情形 | 业务结果 |
| --- | --- |
| 普通会员，金额 99 | 不满减，实付 99.00 |
| 普通会员，金额 100 | 减 10，实付 90.00 |
| 普通会员，金额 300 | 减 40，实付 260.00 |
| GOLD 会员，金额 300 | 先减 40，再打 95 折，实付 247.00 |
| 金额小于等于 0 | 拒绝请求 |
| 库存不足 | 返回业务异常 |

金额使用 BigDecimal，优惠保留两位小数；满 300 与满 100 不叠加。边界及参数化用例均在单元测试中提供具体断言。

## 覆盖率结果

最近一次本地 JaCoCo 报告中，**OrderService 行覆盖率 100%（26/26），分支覆盖率 91.7%（11/12）**。这只是该业务类的覆盖率，整个模块的数据见[完整摘要](../docs/assignments/verification.md#11-test-覆盖率)。未把它标为“全项目 100%”。

## 需要复现时

Java 21、Maven 3.9+，无需 MySQL、Redis。在仓库根目录执行：

```bash
mvn -pl 11-test -am clean verify
mvn -pl 11-test spring-boot:run -Dspring-boot.run.profiles=spec
```

报告生成于 `11-test/target/site/jacoco/index.html`。本机接口为 `POST http://localhost:8011/orders/calculate`；不启用 spec 时端口为 8012。

```json
{"userId":1,"amount":300,"memberLevel":"GOLD","quantity":1}
```

本地接口实测返回的优惠金额为 53.00，实付为 247.00，完整响应见页首实测链接。
