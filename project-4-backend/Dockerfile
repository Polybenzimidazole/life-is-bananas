FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app app
COPY --from=build --chown=app:app /app/target/app.jar ./app.jar

USER app
EXPOSE 8080
HEALTHCHECK --interval=5s --timeout=5s --start-period=30s --retries=30 \
    CMD curl --fail --silent http://localhost:8080/login > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
