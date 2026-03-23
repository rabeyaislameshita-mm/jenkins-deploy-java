pipeline {
  agent any

  environment {
    REPO_PATH = '/var/jenkins_home/repo'
    REGISTRY = 'localhost:5000'
    IMAGE_NAME = 'hello-world-service'
    IMAGE_TAG = 'latest'
  }

  stages {
    stage('Build hello-world image') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            docker build \
              --build-arg REGISTRY=${REGISTRY} \
              --build-arg BUILDER_IMAGE=java-maven-builder:latest \
              --build-arg RUNTIME_IMAGE=java-jdk-runtime:latest \
              -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
              -f projects/hello-world-service/Dockerfile \
              projects/hello-world-service
          '''
        }
      }
    }

    stage('Push hello-world image') {
      steps {
        sh 'docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}'
      }
    }

    stage('Generate SBOM (CycloneDX)') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            docker run --rm \
              -v /var/run/docker.sock:/var/run/docker.sock \
              anchore/syft:latest \
              ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
              -o cyclonedx-json > sbom-hello-world.cdx.json
          '''
        }
      }
    }
  }

  post {
    always {
      dir("${env.REPO_PATH}") {
        archiveArtifacts artifacts: 'sbom-hello-world.cdx.json', onlyIfSuccessful: false
      }
    }
  }
}