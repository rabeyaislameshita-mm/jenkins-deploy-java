# jenkins-deploy-java

Proyecto completo para levantar un microservicio de CI/CD con Jenkins usando `docker-compose`, con los siguientes objetivos:

- Jenkins arranca con plugin `Blue Ocean`.
- Se crean pipelines por defecto al iniciar Jenkins.
- Un pipeline construye una imagen base `maven-builder` para Java.
- Otro pipeline construye una imagen base `jdk-runtime` para Java.
- Un pipeline final construye un `hola mundo` Java usando ambos base images y publica la imagen en un registry.
- Se genera SBOM (CycloneDX JSON) de la imagen final.

## Estructura

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

## Requisitos

- Docker
- Docker Compose v2

## Levantar Jenkins + Registry

```bash
docker compose up -d --build
```

Servicios:

- Jenkins: `http://localhost:8080`
- Registry local: `localhost:5000`

## Login Jenkins

La configuración desactiva el setup wizard para facilitar el arranque en local.

- Usuario: `admin`
- Password: `admin`

Si ya existía un volumen de Jenkins anterior, puedes recrearlo:

```bash
docker compose down -v
docker compose up -d --build
```

## Pipelines por defecto

Al iniciar Jenkins se crean automáticamente estos jobs:

1. `01-build-maven-builder`
2. `02-build-jdk-runtime`
3. `03-build-hello-world`
4. `00-bootstrap-all` (orquesta los tres anteriores)

La primera vez, el job `00-bootstrap-all` se dispara automáticamente.

## Imágenes generadas

- `localhost:5000/java-maven-builder:latest`
- `localhost:5000/java-jdk-runtime:latest`
- `localhost:5000/hello-world-service:latest`

## SBOM

El pipeline `03-build-hello-world` genera un artefacto:

- `sbom-hello-world.cdx.json`

Formato: CycloneDX JSON, generado con `anchore/syft`.

## Verificación rápida

```bash
docker pull localhost:5000/hello-world-service:latest
docker run --rm localhost:5000/hello-world-service:latest
```

Salida esperada:

```text
Hola mundo desde microservicio Java!
```

## Apagar

```bash
docker compose down
```