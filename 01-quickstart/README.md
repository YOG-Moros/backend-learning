# 01-quickstart

这是项目的第一个 Spring Boot 入门模块，演示了如何创建应用、编写 REST 接口并返回 JSON 对象。

## 目录结构

```text
src/
├─ main/java/com/example/quickstart/
│  ├─ QuickstartApplication.java       # 应用启动类
│  ├─ controller/GreetingController.java # REST 控制器
│  └─ model/GreetingResponse.java       # JSON 响应对象
└─ test/java/com/example/quickstart/
   └─ controller/GreetingControllerTest.java
```

## 依赖继承

模块通过父 POM 继承 Spring Boot 版本、Java 编译配置以及 `spring-boot-starter-web`、Lombok 和 `spring-boot-starter-test` 等公共依赖。模块自己的 `pom.xml` 不重复声明这些依赖。

## 运行

在项目根目录执行：

```bash
mvn -pl 01-quickstart spring-boot:run
```

启动后访问 `GET http://localhost:8080/api/hello`，响应示例：

```json
{
  "message": "Hello, Spring Boot!",
  "module": "01-quickstart"
}
```

## 测试

```bash
mvn -pl 01-quickstart test
```
