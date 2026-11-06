# syntax=docker/dockerfile:1-labs
# ---------- 第一阶段：编译打包 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 先只复制 POM，让依赖下载单独成层。父 POM 的 <modules> 要求全部子模块目录存在，
# 用 --parents 保留目录结构一次放好；源码只带 13-deploy 的。
COPY --parents pom.xml ./*/pom.xml ./15-cases/*/pom.xml ./
RUN mvn -B -q -pl 13-deploy dependency:go-offline

# 依赖层已缓存，这一步只编译打包
COPY 13-deploy/src 13-deploy/src
RUN mvn -B -q -pl 13-deploy package -DskipTests

# ---------- 第二阶段：运行 ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 非 root 运行；顺手把 /app 属主给 app，prod profile 要往 logs/ 写文件
RUN addgroup -S app && adduser -S app -G app && chown app:app /app

COPY --chown=app:app --from=build /build/13-deploy/target/app.jar app.jar
USER app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8014

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8014/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
