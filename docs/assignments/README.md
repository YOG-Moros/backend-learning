# 四份作业批改入口

每份作业页面包含：完成内容 → 对应源码 → 实测响应 → 测试结果 → 复现方法。点击下表可直接阅读，不需要下载或解压项目。

| 作业 | 展示页 | 本地测试 |
| --- | --- | ---: |
| 模块 08：Spring Security 综合练习 | [打开 Security 作业](../../08-security/README.md) | 39 项通过 |
| 模块 10：Spring Boot 接口文档综合练习 | [打开接口文档作业](../../10-doc/README.md) | 19 项通过 |
| 模块 11：Spring Boot 测试综合练习 | [打开测试作业](../../11-test/README.md) | 28 项通过 |
| 模块 12：Spring Boot 应用监控综合练习 | [打开应用监控作业](../../12-actuator/README.md) | 17 项通过 |

合计 **103 项本地测试通过，0 失败、0 错误、0 跳过**。这是本地构建结果，不等同于 GitHub Actions 已通过。

- [逐项测试结果和 JaCoCo 覆盖率](verification.md)：由实际 Surefire / JaCoCo XML 提取，标明对应源码版本。
- [接口实测记录](runtime.md)：本地启动后采集的响应，不包含登录令牌或本地凭据。
- [GitHub Actions](https://github.com/YOG-Moros/backend-learning/actions/workflows/ci.yml)：查看云端执行状态；新构建会在 Summary 中直接展示测试明细，无需下载报告。
- [14 份教程规范](../../docx)与[项目总说明](../../README.md)。

## 提交到作业平台

四份作业分别粘贴以下链接，不要四份都填写仓库首页：

| 作业 | 提交链接 |
| --- | --- |
| Security | https://github.com/YOG-Moros/backend-learning/tree/master/08-security |
| 接口文档 | https://github.com/YOG-Moros/backend-learning/tree/master/10-doc |
| 测试 | https://github.com/YOG-Moros/backend-learning/tree/master/11-test |
| 应用监控 | https://github.com/YOG-Moros/backend-learning/tree/master/12-actuator |

提交说明示例：

> 本次作业已整理为在线展示页，打开链接即可查看完成情况、接口运行结果、测试明细及对应源码。页面底部附有运行与测试方法。

提交前用未登录的浏览器打开链接，确认老师可以访问。展示页中的 localhost 地址属于本机复现说明，GitHub 源码页不会运行后端服务。
