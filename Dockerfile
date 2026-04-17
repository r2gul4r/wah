# 1단계: 빌드
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY . .

# gradlew 대신 시스템 설치된 gradle 명령어로 빌드 (Wrapper 파일 필요 없음)
RUN gradle build -x test

# 2단계: 실행
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# build/libs 폴더 안의 jar 파일을 가져옴
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
