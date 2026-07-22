FROM node:24-alpine AS frontend-build
WORKDIR /workspace
COPY ecommerce-client/package.json ecommerce-client/package-lock.json ./ecommerce-client/
RUN npm ci --prefix ecommerce-client
COPY ecommerce-client ./ecommerce-client
RUN mkdir -p ecommerce-api/src/main/resources/static
RUN npm run build --prefix ecommerce-client

FROM maven:3.9.11-eclipse-temurin-21 AS backend-build
WORKDIR /workspace/ecommerce-api
COPY ecommerce-api/pom.xml ./
RUN mvn dependency:go-offline -DskipTests
COPY ecommerce-api/src ./src
COPY --from=frontend-build /workspace/ecommerce-api/src/main/resources/static ./src/main/resources/static
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S ecommerce && adduser -S ecommerce -G ecommerce
WORKDIR /app
COPY --from=backend-build --chown=ecommerce:ecommerce /workspace/ecommerce-api/target/ecommerce-api-0.0.1-SNAPSHOT.jar app.jar
USER ecommerce
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=6 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
