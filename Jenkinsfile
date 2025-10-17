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

  triggers { githubPush() }

  stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build (Maven)') {
      steps { sh 'set -e; mvn -B -U -DskipTests clean package' }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('local-sonarqube') {
          sh 'set -e; mvn -B -DskipTests sonar:sonar -Dsonar.projectKey=stationsync-backend'
        }
      }
    }

    stage('Deploy JAR to Nexus') {
      steps {
        sh 'set -e; mvn -B -DskipTests deploy'
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    stage('Fetch JAR from Nexus') {
      steps {
        sh '''
          set -e
          rm -f app.jar
          mvn -B dependency:copy \
            -Dartifact=tn.spring:Station-Sync:0.0.1-SNAPSHOT:jar \
            -DoutputDirectory=. \
            -Dtransitive=false
          mv Station-Sync-0.0.1-SNAPSHOT.jar app.jar
          ls -lh app.jar
        '''
      }
    }

    stage('Docker Build & Tag') {
      steps {
        script {
          COMMIT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
          TAG = COMMIT
        }
        sh "set -e; docker build -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${TAG} ."
      }
    }

    stage('Docker Login & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: env.REGISTRY_CRED, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh """
            set -e
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker push ${IMAGE_NAME}:${TAG}
            docker push ${IMAGE_NAME}:latest
            docker logout
          """
        }
      }
    }

    stage('Deploy to VM') {
      steps {
        sh """
          set -e
          cd /opt/stationsync

          # Use the freshly built tag WITHOUT touching .env
          BACKEND_IMAGE=${IMAGE_NAME}:${TAG} docker compose pull backend
          BACKEND_IMAGE=${IMAGE_NAME}:${TAG} docker compose up -d --no-deps --force-recreate --pull always backend

          echo '--- Running image ---'
          docker inspect -f '{{.Config.Image}}' stationsync-backend
        """
      }
    }

  }

  post {
    success { echo "✅ Backend pipeline OK — deployed ${IMAGE_NAME}:${TAG}" }
    failure { echo "❌ Backend pipeline failed. Check logs." }
  }
}
