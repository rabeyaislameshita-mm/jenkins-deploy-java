pipeline {
  agent any

  environment {
    REPO_PATH = '/var/jenkins_home/repo'
    REGISTRY = 'localhost:5000'
    IMAGE_NAME = 'java-maven-builder'
    IMAGE_TAG = 'latest'
    BASE_IMAGE = 'registry.access.redhat.com/ubi9/openjdk-21'
    COSIGN_PRIVATE_KEY = credentials('cosign-private-key')
    COSIGN_PASSWORD = credentials('cosign-password')
    EVIDENCE_DIR = 'evidence'
  }

  stages {
    stage('Build maven-builder image') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            BUILD_DATE="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
            VCS_REF="$(git rev-parse --short=12 HEAD || echo unknown)"
            docker build \
              --build-arg BASE_IMAGE=${BASE_IMAGE} \
              --build-arg BUILD_DATE=${BUILD_DATE} \
              --build-arg VCS_REF=${VCS_REF} \
              --build-arg VERSION=${IMAGE_TAG} \
              -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
              -f projects/maven-builder/Dockerfile \
              projects/maven-builder
          '''
        }
      }
    }

    stage('Push maven-builder image') {
      steps {
        sh 'docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
      }
    }

    stage('Security scan (Trivy - block on CRITICAL)') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            mkdir -p ${EVIDENCE_DIR}
            docker run --rm \
              -v /var/run/docker.sock:/var/run/docker.sock \
              -v $(pwd)/${EVIDENCE_DIR}:/evidence \
              aquasec/trivy:0.57.1 \
              image \
              --severity CRITICAL \
              --ignore-unfixed \
              --exit-code 1 \
              --format json \
              --output /evidence/trivy-${IMAGE_NAME}.json \
              ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
          '''
        }
      }
    }

    stage('Generate SBOM (CycloneDX)') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            mkdir -p ${EVIDENCE_DIR}
            docker run --rm \
              -v /var/run/docker.sock:/var/run/docker.sock \
              anchore/syft:latest \
              ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
              -o cyclonedx-json > ${EVIDENCE_DIR}/sbom-${IMAGE_NAME}.cdx.json
          '''
        }
      }
    }

    stage('Sign image (Cosign)') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            mkdir -p ${EVIDENCE_DIR}
            IMAGE_REF="${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            docker run --rm \
              -e COSIGN_PRIVATE_KEY="${COSIGN_PRIVATE_KEY}" \
              -e COSIGN_PASSWORD="${COSIGN_PASSWORD}" \
              ghcr.io/sigstore/cosign/cosign:v2.4.1 \
              sign --yes --key env://COSIGN_PRIVATE_KEY ${IMAGE_REF}
            echo "signed=${IMAGE_REF}" > ${EVIDENCE_DIR}/cosign-sign-${IMAGE_NAME}.txt
          '''
        }
      }
    }
  }

  post {
    always {
      dir("${env.REPO_PATH}") {
        archiveArtifacts artifacts: 'evidence/*', onlyIfSuccessful: false
      }
    }
  }
}