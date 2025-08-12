# ---------- Build stage ----------
FROM gradle:8.7-jdk17 AS build
WORKDIR /app

# 캐시 최적화: 설정 먼저 복사
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle gradle

COPY src src

# Spring Boot fat jar 빌드 (테스트 제외)
RUN gradle clean bootJar -x test --no-daemon

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8080

# 프로필 기본값은 prod로
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -jar app.jar"]
