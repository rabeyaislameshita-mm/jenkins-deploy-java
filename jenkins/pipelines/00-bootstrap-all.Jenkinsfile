pipeline {
  agent any

  stages {
    stage('Build maven base') {
      steps {
        build job: '01-build-maven-builder', wait: true
      }
    }

    stage('Build jdk runtime base') {
      steps {
        build job: '02-build-jdk-runtime', wait: true
      }
    }

    stage('Build app + SBOM') {
      steps {
        build job: '03-build-hello-world', wait: true
      }
    }
  }
}