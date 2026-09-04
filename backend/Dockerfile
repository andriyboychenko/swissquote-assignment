FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"

COPY settings.gradle build.gradle ./
COPY backend/build.gradle backend/build.gradle
RUN gradle :backend:dependencies --no-daemon

COPY backend/src backend/src
RUN gradle :backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/backend/build/libs/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
