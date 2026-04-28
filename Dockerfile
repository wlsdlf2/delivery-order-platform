# JAR 실행을 위한 이미지
FROM eclipse-temurin:17-jre

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 복사
COPY build/libs/*.jar app.jar

# 포트 오픈
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]