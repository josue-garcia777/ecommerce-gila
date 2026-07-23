FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S ecommerce && adduser -S ecommerce -G ecommerce

WORKDIR /app

ARG JAR_FILE=target/ecommerce-api-0.0.1-SNAPSHOT.jar
COPY --from=build --chown=ecommerce:ecommerce /workspace/${JAR_FILE} app.jar

USER ecommerce

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]