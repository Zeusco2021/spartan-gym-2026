# Multi-stage Dockerfile for Spring Boot microservices (Java 8)
# Usage: docker build --build-arg MODULE=servicio-usuarios -t spartan-gym/servicio-usuarios .

FROM maven:3.8-openjdk-8-slim AS builder

WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml

ARG MODULE
COPY ${MODULE}/pom.xml ${MODULE}/pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl common-lib,${MODULE} -am -B -q || true

COPY common-lib/src common-lib/src
COPY ${MODULE}/src ${MODULE}/src

RUN mvn package -pl common-lib,${MODULE} -am -DskipTests -B -q

FROM openjdk:8-jre-slim

RUN groupadd -r appuser && useradd -r -g appuser appuser

ARG MODULE
ENV MODULE=${MODULE}

WORKDIR /app
COPY --from=builder /app/${MODULE}/target/*.jar app.jar

RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
