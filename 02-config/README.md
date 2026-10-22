# 02-配置管理 学习小结

对应老师教程《02 · 配置管理》，代码结构与老师保持一致，仅把姓名等个人信息换成本人的。本模块演示四类核心用法：`@Value` 进阶（占位符 / 默认值 / 随机值 / SpEL）、`@ConfigurationProperties` 类型安全绑定、多环境 profile 与 `@Profile`、`@Validated` 启动期配置校验。

## 目录结构（与老师一致）

```text
02-config
└── src/main
    ├── java/com/example/config
    │   ├── ConfigApplication.java
    │   ├── controller
    │   │   ├── ConfigController.java
    │   │   └── StudentController.java
    │   ├── properties
    │   │   ├── AppProperties.java
    │   │   └── StudentProperties.java
    │   └── service
    │       ├── EnvService.java
    │       ├── DevEnvService.java
    │       ├── TestEnvService.java
    │       └── ProdEnvService.java
    └── resources
        ├── application.yml
        ├── application-dev.yml
        ├── application-test.yml
        └── application-prod.yml
```

与老师工程的差异说明：包名 `top.mqxu.config` → `com.example.config`；占位符前缀 `mqxu` → `cch`（值为本人姓名）；按老师「课后练习·进阶」要求补了 `application-test.yml` 与 `TestEnvService`，实现 dev/test/prod 三环境切换。

## 一、application.yml 要点

```yaml
server:
  port: 8002

spring:
  profiles:
    active: dev

cch:
  name: 陈昶好
  job: 学生

app:
  name: 配置管理模块
  author: ${cch.name}
  port: 8002
  max-count: 100
```

- `student` 段演示 YAML 四种结构：标量、List（hobbies）、Map（scores）、嵌套对象（address）、List 嵌套对象（courses）；
- `app.author: ${cch.name}` 是**占位符引用**，运行时替换成「陈昶好」；
- `app.remark: SpringBoot配置学习` 已配置，`/config/value` 的 remark 字段取该值；
- `spring.profiles.active: dev` 默认激活 dev，可用 `--spring.profiles.active=prod` 覆盖。

## 二、进阶 @Value 四种形态（写在 ConfigController 里）

| 形态 | 写法 | 说明 |
|---|---|---|
| 占位符引用 | `@Value("${app.author}")` | app.author 在 yml 里又引用了 cch.name |
| 默认值 | `@Value("${app.remark:暂无备注}")` | app.remark 已配置为「SpringBoot配置学习」，删掉该行即回落到默认值 |
| 随机值 | `@Value("${random.uuid}")`、`@Value("${random.int(1,100)}")` | Spring 内置 RandomValuePropertySource |
| SpEL | `@Value("#{${student.age} >= 18 ? '成年' : '未成年'}")` | 先解析 `${student.age}` 得 18，再算三元表达式 |

注入方式：`AppProperties`、`EnvService` 用 `@RequiredArgsConstructor` + `final` 字段构造器注入（官方推荐）；`@Value` 用于零散标量。

## 三、多环境 + @Profile

- `application-dev.yml` / `-test.yml` / `-prod.yml` 分别配置 `env.name`、`env.description`；
- `service` 包下 `EnvService` 接口，`DevEnvService`（`@Service + @Profile("dev")`）、`TestEnvService`、`ProdEnvService` 分别实现，覆写方法都加了 `@Override`；
- 激活哪个 profile，容器就只创建对应的 Bean，`/config/env` 的输出随之切换。

## 四、配置校验（@Validated + JSR-303）

`AppProperties` 加 `@Validated`，字段约束：

| 字段 | 注解 |
|---|---|
| name | `@NotBlank` |
| port | `@Min(1)`、`@Max(65535)` |
| maxCount | `@Min(1)`、`@Max(1000)` |

校验发生在**启动阶段**：把 `app.port` 改成 `99999` 再启动，直接报 `BindValidationException` 拒绝启动，改回即可。

说明：本项目已升级到 Spring Boot 3，校验注解导入 `jakarta.validation`，与老师工程一致（Boot 2 时代是 `javax.validation`，升级后需整体替换）。

## 五、接口清单（Apifox 同步用）

| 接口 | 期望现象 |
|---|---|
| `GET /student/info` | 返回完整 student JSON（hobbies、scores、address、courses） |
| `GET /config/basic` | 「服务器端口是：8002，应用名称是：02-配置管理」 |
| `GET /config/my` | 「我的姓名是：陈昶好，职业是：学生」 |
| `GET /config/value` | JSON：randomInt、author、remark（SpringBoot配置学习）、randomUuid、adult |
| `GET /config/app` | 返回 student 配置对象 JSON（address、age、birthDate、courses 等） |
| `GET /config/env` | 「当前环境：dev，开发环境；Profile Bean：我是 dev 环境专属的 Bean」 |

Apifox 环境的 baseUrl 配成 `http://localhost:8002`，用 IDEA 的 Apifox Helper 同步后逐个测试。

## 六、案例 / 使用场景总结

| 案例 | 使用场景 |
|---|---|
| `@Value` | 零散单个值，需要默认值 / 随机值 / SpEL |
| `@ConfigurationProperties` | 成组、结构化配置（List、Map、嵌套对象） |
| 多环境 profile | 区分开发 / 测试 / 生产环境 |
| `@Validated` | 启动时校验配置，防止非法配置流入运行期 |

## 七、运行与验证

```bash
mvn -pl 02-config -am spring-boot:run
mvn -pl 02-config -am spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod
mvn -pl 02-config test
```

切换到 prod 再访问 `/config/env`，应输出「我是 prod 环境专属的 Bean」。
