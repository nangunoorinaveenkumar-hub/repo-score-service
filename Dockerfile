FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
ARG JAR_FILE=target/repo-score-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
