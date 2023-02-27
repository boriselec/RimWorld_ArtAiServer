# build environment
FROM eclipse-temurin:17-jdk-alpine as build
COPY . /home
RUN cd /home && \
    ./mvnw clean package

# production environment
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /home/target/rimworld-aiart*jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]