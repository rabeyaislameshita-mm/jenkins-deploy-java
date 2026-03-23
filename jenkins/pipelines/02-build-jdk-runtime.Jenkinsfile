pipeline {
  agent any

  environment {
    REPO_PATH = '/var/jenkins_home/repo'
    REGISTRY = 'localhost:5000'
    IMAGE_NAME = 'java-jdk-runtime'
    IMAGE_TAG = 'latest'
  }

  stages {
    stage('Build jdk-runtime image') {
      steps {
        dir("${env.REPO_PATH}") {
          sh '''
            set -e
            docker build \
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
  }
}