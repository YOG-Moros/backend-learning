# 02-config

这个模块演示 Spring Boot 的外部化配置：YAML 默认值、Profile 覆盖、环境变量占位符，以及将配置安全地绑定为类型化的 Java 对象。

## 目录结构

```text
src/
|- main/
|  |- java/com/example/config/
|  |  |- ConfigApplication.java
|  |  |- properties/AppProperties.java
|  |  |- controller/ConfigController.java
|  |  `- model/ConfigResponse.java
|  `- resources/
|     |- application.yml
|     `- application-dev.yml
`- test/java/com/example/config/
   |- controller/ConfigControllerTest.java
   `- properties/DevelopmentProfilePropertiesTest.java
```

## 配置内容

`application.yml` 提供 `app` 前缀下的默认配置。`AppProperties` 使用
`@ConfigurationProperties(prefix = "app")` 将它们绑定为 Java 字段：

- `app.name`：应用名称，可由 `APP_NAME` 环境变量覆盖。
- `app.environment`：当前环境标签。
- `app.features-enabled`：布尔开关。
- `app.request-timeout`：绑定为 `Duration`，支持 `2s`、`500ms` 这类写法。
- `app.allowed-origins`：绑定为字符串列表。

激活 `dev` Profile 时，`application-dev.yml` 中同名属性会覆盖默认值；列表属性整体替换，而不是逐项合并。

## 运行

在仓库根目录执行：

```bash
mvn -pl 02-config spring-boot:run
```

默认服务端口为 `8081`。启动后访问 `GET http://localhost:8081/api/config`，可查看当前实际生效的配置。

以 dev Profile 运行：

```bash
mvn -pl 02-config spring-boot:run -Dspring-boot.run.profiles=dev
```

也可以通过环境变量覆盖默认应用名。PowerShell 示例：

```powershell
$env:APP_NAME = "local-config-demo"
mvn -pl 02-config spring-boot:run
```

## 测试

```bash
mvn -pl 02-config test
```

测试覆盖默认配置接口响应，以及激活 `dev` Profile 后的属性绑定结果。
