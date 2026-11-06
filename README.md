# backend-learning

按 `docx/` 中 14 份课程规范组织的 Spring Boot 多模块项目。使用 **Java 21、Spring Boot 4.1.1、Maven 3.9+**；每章独立启动，根 POM 只聚合模块并统一构建与依赖。

## 模块导航

| 目录 | 内容 | 默认端口 | `spec` 端口 | 原目录 |
| --- | --- | --- | --- | --- |
| 01-quickstart | 用户接口、参数、404 | 8001 | 8001 | 不变 |
| 02-config | 配置绑定、校验、环境与优先级 | 8002 | 8002 | 不变 |
| 03-logging | 日志级别、滚动、MDC、脱敏 | 8003 | 8003 | 不变 |
| 04-web | CRUD、校验、分页、Web 组件 | 8004 | 8004 | 不变 |
| 05-mybatis | 数据访问、事务、订单生命周期 | 8005 | 8005 | 不变 |
| 06-redis | 数据类型、缓存、锁、限流、短信、排行榜 | 8006 | 8006 | 不变 |
| 07-schedule | 周期/动态调度、天气与邮件演示 | 8008 | 8007 | 08-schedule |
| 08-security | 分阶段安全案例及原任务批改系统 | 8009 | 8008 | 09-security |
| 09-file | 本地/OSS、批量上传、下载和预览 | 8010 | 8009 | 10-file |
| 10-doc | 图书 CRUD、OpenAPI 分组和契约 | 8011 | 8010 | 11-doc |
| 11-test | 单元/切片/集成测试、JaCoCo | 8012 | 8011 | 12-test |
| 12-actuator | 健康、信息、业务指标、Prometheus | 8013 | 8012 | 13-actuator |
| 13-deploy | 可执行 JAR、Docker、Compose、CI | 8014 | 8013 | 14-deploy |
| 14-toolkit | 验证码、签名、短链、分片、Excel 等 | 8016 | 8014 | 16-toolkit |
| 15-cases/zhihu | 知乎专题扩展项目 | 8015 | — | 15-zhihu |
| 15-cases/mq | MQ 课程占位聚合模块 | — | — | 07-mq |

默认端口及旧接口保留。07–14 章添加 `spec` Spring profile 对齐文档端口；不要混用两套端口配置同时启动全部模块。包名保留 `top.cch`。额外模块保留 artifactId `15-zhihu`、`07-mq`。

## 构建与运行

```bash
mvn -version
mvn clean verify
mvn -pl 04-web spring-boot:run
mvn -pl 08-security spring-boot:run -Dspring-boot.run.profiles=spec
mvn -pl 13-deploy -am package -DskipTests
java -jar 13-deploy/target/app.jar
```

IDE 导入根 `pom.xml`，项目 SDK、Maven Importer 和 Maven Runner 均选择 JDK 21。部署模块产物为 `13-deploy/target/app.jar`，其他应用按 Maven 坐标命名 JAR；根项目、15-cases 和空 MQ 模块不生成应用 JAR。

05、06、08 的集成测试需要 MySQL，06 和 08 的 Redis 短信测试需要 Redis。测试中使用 Redis DB 14 / 13，仅删除测试相关键；应用默认分别使用 DB 10 / 11。运行全量测试前设置专用测试数据库，**开发配置的 SQL 初始化会重建示例表**：

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/backend_learning_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = '<本地测试数据库密码>'
$env:REDIS_HOST = 'localhost'
$env:REDIS_PORT = '6379'
$env:REDIS_PASSWORD = '<本地 Redis 密码>'
mvn -B clean verify
```

数据库需预先创建。无密码 Redis 可使用 `-Dspring.data.redis.password=`。不启动中间件时可以只测试相应模块，例如 `mvn -pl 04-web,09-file,10-doc,11-test,14-toolkit -am test`。CI 在临时 MySQL/Redis 服务中执行全量 `clean verify`，保留测试、覆盖率与 OpenAPI 报告，并构建部署模块镜像做就绪探测。

可选 Druid：`mvn -pl 05-mybatis -Pdruid spring-boot:run -Dspring-boot.run.profiles=druid`。同时启用 Maven 依赖 profile 和 Spring 配置 profile；通过显式配置绑定独立 Druid 库，不使用 Boot 3 starter，不开放默认管理员监控页。

## 配置与兼容说明

- 数据源使用 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`；知乎扩展使用 `ZHIHU_DB_URL`、`ZHIHU_DB_USERNAME`、`ZHIHU_DB_PASSWORD`。`prod` 关闭演示建表，并要求明确提供数据源和相应密钥。
- 06 短信默认模拟；真实通道使用 `SMS_ACCOUNT_SID`、`SMS_ACCOUNT_TOKEN`、`SMS_APP_ID` 并配置模板。07 天气与邮件默认模拟，真实调用需设置 `cch.schedule.mock-enabled=false` 以及 `WEATHER_API_KEY`、`WEATHER_API_URL`、`MAIL_USERNAME`、`MAIL_PASSWORD`、`MAIL_RECIPIENTS`。
- 08 保留 `/auth/**`、`/admin/**`、`/api/tasks/**` 的旧 JWT 和业务码。新增 `/stage1/**` 至 `/stage4/**`、`/api/auth/**` 与课程授权接口，使用独立 `lesson_user` / `lesson_role` 和 JWT issuer。课程账号 alice/bob/carol/dave/frank/erin 的教学密码为 `123456`，其中 dave 停用、frank 锁定；旧任务账号保持原种子数据。
- 08 短信存储可设置 `SECURITY_SMS_STORE=redis`（默认 memory）。这两种实现均为教学验证码生成与校验，没有接入真实短信供应商；`prod` 关闭验证码回显和手机号自动注册。
- 02 保留旧 `/config/app` 返回学生配置，新增 `/config/app-properties` 返回应用配置。各章按规范保持各自的响应格式。
- 09 默认本地存储。OSS 密钥通过环境变量或本地 `aliyun-oss.properties` 配置，真实属性文件不进 Git、不打进 JAR；模板见该模块 resources。外部文件路径相对于启动工作目录。
- 14 的 `prod` 关闭图形验证码答案回显，要求 `TOOLKIT_APP_SECRET`、`TOOLKIT_AES_KEY`。工具库仍为单实例教学实现，重启后的持久化不在当前实现范围内。

已有本地凭据在本次重构的忽略目录 `.refactor-backup/local-env.ps1` 中保存，可在当前机器以 `. ./.refactor-backup/local-env.ps1` 导入；该文件不上传。新机器请自行设置环境变量。

## 文档、监控与部署

- 运行 10-doc 后打开 `http://localhost:8011/swagger-ui/index.html`（spec 为 8010）。`prod` 禁用在线文档。执行 `scripts/export-openapi.ps1` 可重新生成 `docs/apifox/10-doc.openapi.json`（book 分组）与 `all.openapi.json`（10-doc 的全部分组）。合并文件不代表所有 14 个独立应用的契约。
- JaCoCo 报告位于 `11-test/target/site/jacoco/index.html`。
- Prometheus 抓取配置和告警位于 `12-actuator/prometheus/`；默认目标对应兼容端口 8013，启用 spec 时改为 8012。
- `docker compose -f 13-deploy/docker-compose.yml up --build -d`。镜像默认暴露兼容端口 8014。
- 逐章差异、实现结果、验证范围及待确认事项见 [规范差异与重构记录](docs/规范差异与重构记录.md)。
