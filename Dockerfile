FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn package -B -DskipTests -q

FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL org.opencontainers.image.title="undercontroll-notification" \
      org.opencontainers.image.description="Undercontroll notification microservice" \
      org.opencontainers.image.version="0.0.1"

RUN addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=build --chown=app:app /build/target/notification-service-0.0.1-SNAPSHOT.jar app.jar

USER app

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
