# Documented Hardening Standard

## 1. Objective

Define a container image hardening baseline for:

- Build image: java-maven-builder
- Runtime images: java-jdk-runtime and hello-world-service

This standard reduces attack surface, improves traceability, and supports auditability.

## 2. Scope

Applies to:

- [projects/maven-builder/Dockerfile](../projects/maven-builder/Dockerfile)
- [projects/jdk-runtime/Dockerfile](../projects/jdk-runtime/Dockerfile)
- [projects/hello-world-service/Dockerfile](../projects/hello-world-service/Dockerfile)

## 3. Mandatory baseline (all images)

1. Trusted base image
- Use only official images or team-approved images.
- Keep explicit major/minor tags (for example: 21-jdk-jammy, 21-jre-jammy).

2. Minimal installed software
- Install only required dependencies.
- Use --no-install-recommends where applicable.
- Clean package manager cache after install.

3. Reproducible build and no secrets
- Never copy secrets into build context or image layers.
- Keep .dockerignore aligned to exclude sensitive files.

4. Traceability
- Keep SBOM for final image (sbom-hello-world.cdx.json).
- Record image tag, build date, and source pipeline.

5. Vulnerability scanning
- Run vulnerability scanning on every build.
- Block promotion when critical findings exist without an approved exception.

6. OpenShift Restricted SCC compatibility
- Runtime images must be validated as compatible with OpenShift Restricted SCC.
- Container must run without privileged mode, host namespaces, or elevated capabilities.
- Image filesystem permissions must allow execution under an arbitrary non-root UID.
- Validation evidence must be captured in CI logs for each runtime image release.

## 4. Controls by image type

### 4.1 Build image (java-maven-builder)

Mandatory controls:

- Include only required build tooling (Maven, JDK).
- Never reuse the build image as runtime image.
- Minimize layers and avoid unnecessary admin utilities.

Recommended controls:

- Run builds as non-root inside the build container when feasible.
- Pin Maven version for stronger reproducibility.

### 4.2 Runtime images (java-jdk-runtime, hello-world-service)

Mandatory controls:

- Use runtime image (JRE) instead of JDK when possible.
- Run process as non-root.
- Copy only final artifact (JAR) and strictly required runtime files.
- Do not include build tooling in runtime images.
- Ensure OpenShift Restricted SCC compatibility is validated and documented.

Recommended controls:

- Define HEALTHCHECK for HTTP services when applicable.
- Set safe JVM defaults for memory and runtime behavior.

## 5. Exceptions

Every exception must document:

- Violated control
- Technical justification
- Accepted risk
- Exception expiration date
- Remediation owner

Exceptions are invalid without all fields above.

## 6. Pipeline verification checklist

Minimum checklist per image:

1. Dockerfile validated against baseline.
2. Build verified without embedded secrets.
3. Vulnerability scan executed.
4. SBOM generated (final image).
5. Runtime image validated as non-root execution.
6. OpenShift Restricted SCC compatibility validated.

Suggested verification commands:

docker inspect --format='{{.Config.User}}' localhost:5000/hello-world-service:latest
docker run --rm localhost:5000/hello-world-service:latest
oc run scc-check --rm -i --restart=Never --image=localhost:5000/hello-world-service:latest --command -- id

Expected result:

- Config.User is non-root or image is compatible with arbitrary non-root UID.
- OpenShift test pod starts and runs under Restricted SCC constraints.

## 7. Current repository status

- The project uses build/runtime separation through multi-stage build in [projects/hello-world-service/Dockerfile](../projects/hello-world-service/Dockerfile).
- Runtime images are configured for non-root execution and arbitrary UID-compatible filesystem permissions.
- Pipelines now run Trivy scans and block on CRITICAL findings before promotion.
- Pipelines generate SBOM evidence and archive evidence artifacts per image.
- Pipelines sign images using Cosign with Jenkins-managed credentials.
