# 模块 08 · Spring Security 综合练习

[全部作业](../docs/assignments/README.md) · [测试结果](../docs/assignments/verification.md#08-security) · [实际接口响应](../docs/assignments/runtime.md#08-security) · [教程](../docx/08-security.md)

实现从 Basic 认证、角色权限到表单与 JWT 的完整课程案例，并保留原任务批改系统。**本地测试 39 项通过，0 失败、0 错误、0 跳过**；结果对应的源码版本和逐项用例见上方测试结果。

## 作业描述

本作业依据模块8教程，使用Spring Security完成后端身份认证与权限控制。项目从基础认证逐步扩展到接口白名单、角色授权、细粒度权限、表单登录及JWT认证，并结合数据库管理用户状态与权限信息。同时实现CSRF防护、跨域配置和模拟短信验证码登录，验证未登录、权限不足、账号停用及令牌异常等场景。为兼容原有任务系统，新旧认证接口采用独立的令牌校验规则。本地39项测试全部通过，展示页提供权限对照表、真实接口响应及源码链接，便于核对实现效果。

## 完成内容与源码

| 教程内容 | 实现与批改入口 |
| --- | --- |
| 阶段 1：Basic 认证 | [stage1 安全链](src/main/java/top/cch/security/config/LessonSecurityConfig.java)，未登录 401，合法账号可访问 |
| 阶段 2：白名单与请求方法 | 同一配置中 stage2：公开接口和 GET 文章可匿名，写操作需要认证 |
| 阶段 3：角色和权限 | stage3 区分 ADMIN、任一角色、同时拥有两种角色，以及 ARTICLE_DELETE 权限 |
| 阶段 4：表单、Session、CSRF、退出 | [登录页面](src/main/resources/static/stage4/login.html)、stage4 安全链；不携带 CSRF 的表单提交被拒绝 |
| JWT 登录与鉴权 | [认证接口](src/main/java/top/cch/security/controller/LessonAuthController.java)、[JWT 签发和解析](src/main/java/top/cch/security/security/JwtService.java)、[过滤器](src/main/java/top/cch/security/security/LessonJwtAuthenticationFilter.java) |
| 用户与权限来自数据库 | [用户加载](src/main/java/top/cch/security/service/DbUserDetailsService.java)、[建表](src/main/resources/schema.sql)、[演示数据](src/main/resources/data.sql) |
| 短信验证码认证 | [认证 Provider](src/main/java/top/cch/security/sms/SmsCodeAuthenticationProvider.java)、[内存实现](src/main/java/top/cch/security/sms/InMemorySmsCodeService.java)、[Redis 实现](src/main/java/top/cch/security/sms/RedisSmsCodeService.java) |
| CORS 与错误响应 | [CORS 配置](src/main/java/top/cch/security/config/LessonSecurityConfig.java)、[课程异常处理](src/main/java/top/cch/security/common/LessonExceptionHandler.java) |
| 兼容旧任务系统 | [/api/tasks 控制器](src/main/java/top/cch/security/controller/TaskController.java)、[新旧 JWT 路径隔离](src/main/java/top/cch/security/security/LessonPaths.java) |

## 权限验证示例

所有账号的课程演示密码为 `123456`。下表是代码定义的授权结果；本次实测响应及测试断言可通过页首链接核对。

| 请求 | 身份 | 结果 |
| --- | --- | --- |
| GET `/stage1/hello` | 未登录 | 401 |
| GET `/stage1/hello` | alice | 200 |
| GET `/stage3/audit/logs` | carol：仅 ADMIN | 403 |
| GET `/stage3/audit/logs` | bob：ADMIN + AUDITOR | 200 |
| DELETE `/stage3/articles/1` | alice：ARTICLE_DELETE | 200 |
| DELETE `/stage3/articles/1` | bob：只有角色，没有此权限 | 403 |
| POST `/api/auth/login` | dave：停用，或 frank：锁定 | 403 |

## 测试设计

- [课程集成测试](src/test/java/top/cch/security/LessonSecurityIntegrationTest.java)：认证、角色与权限、JWT 篡改、账户状态、表单 CSRF、CORS、新旧令牌隔离。
- [JWT 边界测试](src/test/java/top/cch/security/JwtServiceTest.java)：过期与不同签名密钥。
- [短信测试](src/test/java/top/cch/security/sms)：发送冷却、错误次数、一次性消费、Redis 并发消费。
- [旧系统 22 项回归](src/test/java/top/cch/security/SecurityCaseTests.java)：原账号、任务归属、提交批改、管理员操作。

## 需要运行时

Java 21、Maven 3.9+、MySQL；完整测试还需要 Redis。数据库通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 配置；Redis 通过 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 配置。开发初始化会重建演示表，请使用专用学习数据库。

```bash
# 仓库根目录执行；spec 对齐教程端口 8008，默认兼容端口为 8009
mvn -pl 08-security spring-boot:run -Dspring-boot.run.profiles=spec
mvn -pl 08-security -am test
```

浏览器表单地址为 `http://localhost:8008/stage4/login.html`。这是在本机启动后访问的地址，不是公网演示站。

短信默认采用内存模拟，设置 `SECURITY_SMS_STORE=redis` 可切换存储。验证码不会真实发送到手机；`prod` 关闭回显与自动注册。本文展示正文课程实现，生产网关等扩展范围见[重构记录](../docs/规范差异与重构记录.md)。
