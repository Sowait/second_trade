FROM eclipse-temurin:8-jre

WORKDIR /app

ARG JAR_FILE=target/backend-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]
