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

    // 1️⃣ Build + Sonar before deploy to Nexus
    stage('Build (Maven)') {
      steps {
        sh 'mvn -B -U -DskipTests clean package'

      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('local-sonarqube') {
          sh 'mvn -B -DskipTests sonar:sonar -Dsonar.projectKey=stationsync-backend'
        }
      }
    }

    // 2️⃣ Deploy artifact to Nexus
    stage('Deploy JAR to Nexus') {
      steps {
        sh 'mvn -B -DskipTests deploy'
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    // 3️⃣ Fetch JAR from Nexus to build Docker image
    stage('Fetch JAR from Nexus') {
      steps {
        sh '''
          echo "🧹 Cleaning old JAR..."
          rm -f app.jar

          echo "⬇️ Downloading artifact from Nexus..."
          mvn -B dependency:copy \
            -Dartifact=tn.spring:Station-Sync:0.0.1-SNAPSHOT:jar \
            -DoutputDirectory=. \
            -Dtransitive=false

          mv Station-Sync-0.0.1-SNAPSHOT.jar app.jar
          ls -lh app.jar
        '''
      }
    }

    // 4️⃣ Docker build, push, deploy to VM
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
      when { branch 'main' }
      steps {
        sh '''
          cd /opt/stationsync
          echo "🧹 Cleaning old backend container (if exists)..."
          docker compose down backend || true

          echo "⬇️ Pulling latest backend image..."
          docker compose pull backend

          echo "🚀 Starting new backend container..."
          docker compose up -d backend
        '''
      }
    }
  }

  post {
    success { echo "✅ Backend pipeline OK — SonarQube + Nexus + Docker deploy successful!" }
    failure { echo "❌ Pipeline failed. Check Jenkins logs for errors." }
  }
}
