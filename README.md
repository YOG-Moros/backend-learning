# backend-learning

Spring Boot 后端学习父项目。当前仓库只负责统一 Maven、Java 和常用依赖配置，暂不包含业务源码，因此根目录没有 `src` 目录。

## 项目配置

- Spring Boot：`2.7.18`
- Java：`21`（由 `java.version` 统一声明）
- Maven：建议使用本机 Maven `3.9.x`；当前机器可用路径为
  `C:\\Users\\LOLI\\.m2\\wrapper\\dists\\apache-maven-3.9.11-bin\\6mqf5t809d9geo83kj4ttckcbc\\apache-maven-3.9.11`
- 打包类型：`pom`，可作为后续子模块的父项目
- 公共依赖：Spring Web、Lombok、Spring Boot Test

子模块可以通过 `<parent>` 继承本项目，从而复用版本、编码和公共依赖配置。

## 在 IntelliJ IDEA 中导入

1. 使用 IDEA 打开仓库根目录，并选择根目录的 `pom.xml` 作为 Maven 项目导入。
2. 打开 `Settings | Build, Execution, Deployment | Build Tools | Maven`：
   - `Maven home path` 选择本机已安装的 Maven；当前环境可用的是 `3.9.11`（路径见上方“项目配置”）。换到其他电脑时请选择该电脑自己的 Maven 安装目录。
   - `User settings file` 使用默认的 `%USERPROFILE%\\.m2\\settings.xml`（不存在时可保持未勾选）。
   - `Local repository` 使用默认的 `%USERPROFILE%\\.m2\\repository`。
3. 在 `Settings | Build, Execution, Deployment | Build Tools | Maven | Importing` 将 JDK 设为 JDK 21，并在 `Project Structure | Project` 中确认 Project SDK 和语言级别均为 21。
4. 点击 Maven 工具窗口的刷新按钮，确认 `backend-learning` 能被识别且依赖可以解析。

## 命令行验证

在仓库根目录执行：

```bash
mvn -version
mvn validate
```

`mvn validate` 成功即表示 `pom.xml` 和本地 Maven 配置可用。首次执行依赖解析时需要访问 Maven 仓库；依赖下载后会缓存到本地 `.m2/repository`。

## 后续添加模块

创建模块后，在模块 `pom.xml` 中继承本项目：

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>backend-learning</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

然后在根 `pom.xml` 的 `<modules>` 中加入模块目录，并按模块需要补充启动类和 `src` 目录。
