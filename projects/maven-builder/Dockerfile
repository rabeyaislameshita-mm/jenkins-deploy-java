ARG BASE_IMAGE=registry.access.redhat.com/ubi9/openjdk-21
FROM ${BASE_IMAGE}

USER 0
RUN microdnf install -y maven \
    && microdnf clean all

ENV HOME=/home/builder \
    MAVEN_CONFIG=/home/builder/.m2

RUN mkdir -p /workspace /home/builder/.m2 \
    && chgrp -R 0 /workspace /home/builder \
    && chmod -R g=u /workspace /home/builder

WORKDIR /workspace
USER 1001