ARG BUILD_IMAGE=maven:3.8.6-openjdk-8
ARG RUNTIME_IMAGE=eclipse-temurin:8-jre
FROM ${BUILD_IMAGE} AS build

WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM ${RUNTIME_IMAGE} AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]
