# ============================================================
# openCallHub 应用 Dockerfile (多阶段构建)
# ============================================================

# ─── 构建阶段 ───
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY och-common/pom.xml och-common/
COPY och-infra/pom.xml och-infra/
COPY och-security/pom.xml och-security/
COPY och-system/pom.xml och-system/
COPY och-resource/pom.xml och-resource/
COPY och-call/pom.xml och-call/
COPY och-acd/pom.xml och-acd/
COPY och-customer/pom.xml och-customer/
COPY och-report/pom.xml och-report/
COPY och-compliance/pom.xml och-compliance/
COPY och-ticket/pom.xml och-ticket/
COPY och-knowledge/pom.xml och-knowledge/
COPY och-quality/pom.xml och-quality/
COPY och-notice/pom.xml och-notice/
COPY och-batch/pom.xml och-batch/
COPY och-call-task/pom.xml och-call-task/
COPY och-esl/pom.xml och-esl/
COPY och-ivr/pom.xml och-ivr/
COPY och-ai/pom.xml och-ai/
COPY och-api/pom.xml och-api/
# 下载依赖(利用Docker层缓存)
RUN mvn dependency:go-offline -pl och-api -am -B -q
# 构建
COPY . .
RUN mvn package -pl och-api -am -DskipTests -B -q

# ─── 运行阶段 ───
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S och && adduser -S och -G och
USER och
WORKDIR /app
COPY --from=builder /app/och-api/target/och-api-*.jar app.jar
EXPOSE 4320
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
