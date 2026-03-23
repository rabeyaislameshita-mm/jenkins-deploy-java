ARG REGISTRY=localhost:5000
ARG BUILDER_IMAGE=java-maven-builder:latest
ARG RUNTIME_IMAGE=java-jdk-runtime:latest
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION=latest

FROM ${REGISTRY}/${BUILDER_IMAGE} AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM ${REGISTRY}/${RUNTIME_IMAGE}
LABEL org.opencontainers.image.title="hello-world-service" \
	org.opencontainers.image.description="Sample Java service built with hardened builder/runtime images" \
	org.opencontainers.image.version="${VERSION}" \
	org.opencontainers.image.created="${BUILD_DATE}" \
	org.opencontainers.image.revision="${VCS_REF}"

ENV SERVER_PORT=8080

WORKDIR /app
COPY --from=build --chown=1001:0 /workspace/target/hello-world-1.0.0.jar /app/app.jar

EXPOSE 8080
VOLUME ["/tmp"]
USER 1001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]