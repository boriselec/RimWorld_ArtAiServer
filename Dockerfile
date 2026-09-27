FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:resolve dependency:resolve-plugins
COPY src src
RUN mvn -B package

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
EXPOSE 8080

COPY --from=build /build/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
