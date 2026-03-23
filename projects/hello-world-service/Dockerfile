ARG REGISTRY=localhost:5000
ARG BUILDER_IMAGE=java-maven-builder:latest
ARG RUNTIME_IMAGE=java-jdk-runtime:latest

FROM ${REGISTRY}/${BUILDER_IMAGE} AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM ${REGISTRY}/${RUNTIME_IMAGE}
WORKDIR /app
COPY --from=build /workspace/target/hello-world-1.0.0.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]