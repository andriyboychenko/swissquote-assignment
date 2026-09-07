FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"

COPY settings.gradle build.gradle ./
COPY backend/build.gradle backend/build.gradle
COPY ai-service/build.gradle ai-service/build.gradle
RUN gradle :ai-service:dependencies --no-daemon

COPY backend/src backend/src
COPY ai-service/src ai-service/src
RUN gradle :ai-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/ai-service/build/libs/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
