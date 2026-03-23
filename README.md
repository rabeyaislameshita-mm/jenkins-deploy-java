# jenkins-deploy-java

Complete project to run a CI/CD Java microservice flow with Jenkins using `docker-compose`, with the following goals:

- Jenkins starts with the `Blue Ocean` plugin.
- Default pipelines are created automatically at Jenkins startup.
- One pipeline builds a Java `maven-builder` base image.
- Another pipeline builds a Java `jdk-runtime` base image.
- A final pipeline builds a Java hello-world service using both base images and pushes the image to a registry.
- Security evidence (SBOM, scan report, and signature record) is generated for each image.

## Structure

```
.
├── docker-compose.yml
├── jenkins
│   ├── Dockerfile
│   ├── plugins.txt
│   ├── init.groovy.d
│   │   └── 01-create-pipelines.groovy
│   └── pipelines
│       ├── 00-bootstrap-all.Jenkinsfile
│       ├── 01-build-maven-builder.Jenkinsfile
│       ├── 02-build-jdk-runtime.Jenkinsfile
│       └── 03-build-hello-world.Jenkinsfile
└── projects
	├── maven-builder
	│   └── Dockerfile
	├── jdk-runtime
	│   └── Dockerfile
	└── hello-world-service
		├── Dockerfile
		├── pom.xml
		└── src/main/java/com/example/App.java
```

## Requirements

- Docker
- Docker Compose v2
- Jenkins credentials for image signing:
	- `cosign-private-key` (PEM private key as secret text)
	- `cosign-password` (secret text)

## Start Jenkins + Registry

```bash
docker compose up -d --build
```

Services:

- Jenkins: `http://localhost:8080`
- Local registry: `localhost:5000`

## Jenkins Login

The configuration disables the setup wizard to simplify local startup.

- User: `admin`
- Password: `admin`

If a previous Jenkins volume already exists, you can recreate it:

```bash
docker compose down -v
docker compose up -d --build
```

## Default Pipelines

At Jenkins startup, these jobs are created automatically:

1. `01-build-maven-builder`
2. `02-build-jdk-runtime`
3. `03-build-hello-world`
4. `00-bootstrap-all` (orchestrates the previous three)

On first startup, the `00-bootstrap-all` job is triggered automatically.

## Generated Images

- `localhost:5000/java-maven-builder:latest`
- `localhost:5000/java-jdk-runtime:latest`
- `localhost:5000/hello-world-service:latest`

## Image Hardening (build and runtime)

A documented hardening standard is defined for container images:

- See: [docs/hardening-standard.md](docs/hardening-standard.md)
- Scope: build image (maven-builder) and runtime images (jdk-runtime, hello-world-service)
- Includes: mandatory baseline, controls by image type, exception handling, and verification checklist
- Compliance requirement: OpenShift Restricted SCC compatibility must be validated for runtime images

## SBOM

Each pipeline generates and publishes evidence as Jenkins artifacts:

- `evidence/sbom-<image>.cdx.json` (CycloneDX, `anchore/syft`)
- `evidence/trivy-<image>.json` (vulnerability scan, blocks on `CRITICAL`)
- `evidence/cosign-sign-<image>.txt` (signature record)

In `03-build-hello-world`, the following is also kept:

- `sbom-hello-world.cdx.json`

## Quick Verification

```bash
docker pull localhost:5000/hello-world-service:latest
docker run --rm -p 8080:8080 localhost:5000/hello-world-service:latest
curl -s http://localhost:8080/
curl -s http://localhost:8080/actuator/health/liveness
curl -s http://localhost:8080/actuator/health/readiness
```

Expected output:

```text
Hello world from Java microservice!
```

## Stop

```bash
docker compose down
```