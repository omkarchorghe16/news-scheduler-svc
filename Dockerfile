FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY target/news-scheduler-svc-*.jar app.jar
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
