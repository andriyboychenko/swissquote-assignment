FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace

COPY settings.gradle build.gradle ./
COPY backend/build.gradle backend/build.gradle
COPY ai-service/build.gradle ai-service/build.gradle
RUN if [ "$(uname -m)" = "aarch64" ]; then JAVA_TOOL_OPTIONS="-XX:UseSVE=0" gradle :ai-service:dependencies --no-daemon; else gradle :ai-service:dependencies --no-daemon; fi

COPY backend/src backend/src
COPY ai-service/src ai-service/src
RUN if [ "$(uname -m)" = "aarch64" ]; then JAVA_TOOL_OPTIONS="-XX:UseSVE=0" gradle :ai-service:bootJar --no-daemon; else gradle :ai-service:bootJar --no-daemon; fi

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/ai-service/build/libs/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "if [ \"$(uname -m)\" = \"aarch64\" ]; then exec java -XX:UseSVE=0 -jar /app/app.jar; else exec java -jar /app/app.jar; fi"]
