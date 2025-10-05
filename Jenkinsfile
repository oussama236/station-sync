pipeline {
  agent any

  environment {
    REGISTRY_CRED = 'dockerhub'                       // ID des credentials Docker Hub dans Jenkins
    IMAGE_NAME    = 'oussamamiladi123/stationsync-backend'  // ton image sur Docker Hub
  }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '5'))
  }

  triggers {
    // Le webhook GitHub que tu as déjà actif
    githubPush()
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
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
          sh """
            docker build -t ${IMAGE_NAME}:latest .
          """
        }
      }
    }

    stage('Docker Login & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: env.REGISTRY_CRED, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push ${IMAGE_NAME}:latest
          '''
        }
      }
    }

    stage('Deploy (Local VM)') {
      steps {
        script {
          sh """
            docker pull ${IMAGE_NAME}:latest
            docker rm -f stationsync-backend || true
            docker run -d --name stationsync-backend -p 8081:8080 ${IMAGE_NAME}:latest
          """
        }
      }
    }
  }

  post {
    success {
      echo "✅ Déploiement réussi : ${IMAGE_NAME}:latest"
    }
    failure {
      echo "❌ Erreur dans le pipeline. Vérifie les logs Jenkins."
    }
  }
}
