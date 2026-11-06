# 模块 10 · Spring Boot 接口文档综合练习

[全部作业](../docs/assignments/README.md) · [测试结果](../docs/assignments/verification.md#10-doc) · [实际接口响应](../docs/assignments/runtime.md#10-doc) · [教程](../docx/10-doc.md)

以图书管理接口演示 springdoc-openapi：源码注解生成接口说明、参数、校验约束、示例与响应文档。**本地测试 19 项全部通过**，包括 8 项业务接口测试和 11 项文档契约测试。

## 作业描述

本作业依据模块10教程，以图书管理业务为案例，完成Spring Boot接口文档的生成、分组与验证。项目通过代码注解描述接口用途、请求参数、校验规则、响应状态及示例数据，并使用Swagger界面展示图书查询、新增和删除等操作。文档划分为图书业务与运维两组，支持导出OpenAPI文件，方便导入Apifox。测试覆盖接口行为、分组隔离、必填字段和文档内容等方面，本地19项测试全部通过。展示页附有导出文件、实测响应和源码入口，便于直接在线批改。

## 完成内容与源码

| 教程内容 | 实现与批改入口 |
| --- | --- |
| 接口标题、描述、状态码和请求示例 | [BookController](src/main/java/top/cch/doc/controller/BookController.java)：`@Operation`、`@ApiResponses`、`@ExampleObject` |
| 查询参数平铺 | [BookQuery](src/main/java/top/cch/doc/dto/BookQuery.java) 与 Controller 的 `@ParameterObject` |
| 必填、校验、枚举 Schema | [新增请求 DTO](src/main/java/top/cch/doc/dto/BookCreateRequest.java)、[图书实体](src/main/java/top/cch/doc/entity/Book.java)、[分类枚举](src/main/java/top/cch/doc/entity/Category.java) |
| 文档元信息、服务器与分组 | [OpenApiConfig](src/main/java/top/cch/doc/config/OpenApiConfig.java)：book 与 ops 分组 |
| 图书查询、新增、删除和错误处理 | [BookService](src/main/java/top/cch/doc/service/BookService.java)、[业务测试](src/test/java/top/cch/doc/BookApiTest.java) |
| 文档契约验证与导出 | [OpenApiDocumentTest](src/test/java/top/cch/doc/OpenApiDocumentTest.java)、[导出脚本](../scripts/export-openapi.ps1) |
| 生产环境关闭在线文档 | [application-prod.yml](src/main/resources/application-prod.yml) |

## 可直接在线查看的文档

- [图书分组 OpenAPI JSON](../docs/apifox/10-doc.openapi.json)：可在 GitHub 阅读，也可导入 Apifox。
- [本模块全部接口 OpenAPI JSON](../docs/apifox/all.openapi.json)：包含 book 与 ops；不是全项目 14 个应用的合并文件。
- [实测接口清单与响应](../docs/assignments/runtime.md#10-doc)：无需启动项目即可查看运行时导出的接口名称及图书分页结果。

| 接口 | 功能 |
| --- | --- |
| GET `/books` | 关键词、分类和分页查询 |
| GET `/books/{id}` | 图书详情；不存在时返回 404 |
| POST `/books` | 校验新增请求；重复 ISBN 返回 409 |
| DELETE `/books/{id}` | 删除图书 |
| GET `/books/categories` | 分类枚举 |
| GET `/books/boom` | 教学用异常响应演示 |

## 测试设计

[文档契约测试](src/test/java/top/cch/doc/OpenApiDocumentTest.java) 验证接口 summary、分组隔离、查询参数、路径参数、必填字段、枚举、元信息、服务器列表、响应码与示例，另生成两份可导入 JSON。测试的是实际 `/v3/api-docs` 响应。

## 需要运行时

本模块使用内存数据，不需要 MySQL 或 Redis。仓库根目录执行：

```bash
mvn -pl 10-doc spring-boot:run -Dspring-boot.run.profiles=spec
mvn -pl 10-doc -am test
```

本机 Swagger：`http://localhost:8010/swagger-ui/index.html`；book 文档：`http://localhost:8010/v3/api-docs/book`。未启用 spec 时端口为 8011。这些地址需在本机启动后访问。

PowerShell 执行 `./scripts/export-openapi.ps1` 可重新导出文档。GitHub 源码页不会运行 Swagger 的“Try it out”；若要实时请求，需要启动本模块。
