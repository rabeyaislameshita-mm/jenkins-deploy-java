ARG BASE_IMAGE=registry.access.redhat.com/ubi9/openjdk-21-runtime
FROM ${BASE_IMAGE}

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION=latest

LABEL org.opencontainers.image.title="java-jdk-runtime" \
	  org.opencontainers.image.description="Hardened Java runtime base image" \
	  org.opencontainers.image.version="${VERSION}" \
	  org.opencontainers.image.created="${BUILD_DATE}" \
	  org.opencontainers.image.revision="${VCS_REF}"

ENV APP_HOME=/app \
	JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp"

USER 0
RUN mkdir -p ${APP_HOME} /tmp \
	&& chgrp -R 0 ${APP_HOME} /tmp \
	&& chmod -R g=u ${APP_HOME} /tmp

WORKDIR ${APP_HOME}
USER 1001