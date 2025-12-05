# build environment
FROM eclipse-temurin:25-jdk-alpine as build
COPY . /home
RUN cd /home && \
    sed -i 's/\r$//' mvnw && \
    ./mvnw clean package

# production environment
FROM eclipse-temurin:25-jre-alpine
COPY --from=build /home/target/rimworld-aiart*jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]