# Builder stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여
RUN chmod +x gradlew

# 의존성 캐싱 (소스코드 복사 전 실행)
RUN ./gradlew dependencies --no-daemon

# 소스코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# Runner stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Builder 스테이지에서 빌드된 Jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 (프로파일 및 옵션 설정)
CMD ["java", "-jar", "/app.jar", "-web", "-webAllowOthers", "-tcp", "-tcpAllowOthers", "-browser"]

