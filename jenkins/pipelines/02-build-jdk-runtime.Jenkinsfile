pipeline {
  agent any

  environment {
    REPO_PATH = '/var/jenkins_home/repo'
    REGISTRY = 'localhost:5000'
    IMAGE_NAME = 'java-jdk-runtime'
    IMAGE_TAG = 'latest'
    BASE_IMAGE = 'registry.access.redhat.com/ubi9/openjdk-21-runtime'
    COSIGN_PRIVATE_KEY = credentials('cosign-private-key')
    COSIGN_PASSWORD = credentials('cosign-password')
    EVIDENCE_DIR = 'evidence'
  }

  stages {
    stage('Build jdk-runtime image') {
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
              -f projects/jdk-runtime/Dockerfile \
              projects/jdk-runtime
          '''
        }
      }
    }

    stage('Push jdk-runtime image') {
      steps {
        sh 'docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
      }
    }

    stage('Validate runtime hardening') {
      steps {
        sh '''
          set -e
          IMAGE_REF="${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
          docker run --rm --user 12345 --entrypoint java ${IMAGE_REF} -version
          docker run --rm --read-only --tmpfs /tmp --entrypoint java ${IMAGE_REF} -version
        '''
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