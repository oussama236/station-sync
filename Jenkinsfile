pipeline {
  agent any

  environment {
    REGISTRY_CRED = 'dockerhub'
    IMAGE_NAME    = 'oussamamiladi123/stationsync-backend'
  }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '5'))
  }

  triggers {
    githubPush()
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build JAR (Maven)') {
      steps {
        sh 'mvn -q -DskipTests package'
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    stage('Docker Build') {
      steps {
        script {
          COMMIT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          TAG = COMMIT
        }
        sh """
          docker build -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${TAG} .
        """
      }
    }

    stage('Docker Login & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: env.REGISTRY_CRED, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push ${IMAGE_NAME}:latest
            docker push ${IMAGE_NAME}:${TAG}
            docker logout
          """
        }
      }
    }

    stage('Deploy to VM') {
      when { branch 'main' } // change if your default branch differs
      steps {
        sh '''
          cd /opt/stationsync
          docker compose pull
          docker compose up -d
        '''
      }
    }
  }

  post {
    success { echo "✅ Backend build, push & compose deploy OK: ${IMAGE_NAME}:latest" }
    failure { echo "❌ Erreur pipeline backend. Vérifie les logs Jenkins." }
  }
}
