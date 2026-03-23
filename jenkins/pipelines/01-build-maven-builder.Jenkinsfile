pipeline {
  agent any

  environment {
    REPO_PATH = '/var/jenkins_home/repo'
    REGISTRY = 'localhost:5000'
    IMAGE_NAME = 'java-maven-builder'
    IMAGE_TAG = 'latest'
  }

  stages {
    stage('Build maven-builder image') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            docker build \
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
  }
}