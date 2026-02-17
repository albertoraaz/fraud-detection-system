pipeline {
    agent any

    tools {
        jdk 'JAVA_21'
        maven '3.9.6'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Package') {
            steps {
                script {
                    // 1. Get the path Jenkins assigned to the JAVA_21 tool
                    def jdkPath = tool name: 'JAVA_21', type: 'jdk'

                    // 2. Execute Maven while explicitly setting the JAVA_HOME for this process
                    sh "export JAVA_HOME=${jdkPath} && ${tool name: '3.9.6', type: 'maven'}/bin/mvn clean package -DskipTests"
                }
            }
        }

        stage('Docker Build') {
    steps {
        script {
            try {
                sh 'docker build -t yourdockerhubuser/fraud-detection:latest .'
            } catch (Exception e) {
                echo "⚠️ Build failed due to cache corruption. Pruning and retrying..."
                sh 'docker builder prune -f'
                sh 'docker build --no-cache -t yourdockerhubuser/fraud-detection:latest .'
            }
        }
    }
}

        stage('Deploy') {
    steps {
        script {
            try {
                echo "🧹 Cleaning up infrastructure and ports..."

                // 1. Standard docker-compose cleanup
                sh "docker-compose -f docker-compose.yml down -v --remove-orphans"

                // 2. Force liberation of Kafka/Zookeeper ports (2181, 9092)
                // This kills any process currently listening on those ports
                sh '''
                    for port in 2181 9092; do
                        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
                            echo "⚠️ Port $port is busy. Killing process..."
                            fuser -k $port/tcp || true
                        fi
                    done
                '''

                // 3. Fresh deployment
                sh "docker-compose -f docker-compose.yml up -d --build"

            } catch (Exception e) {
                echo "❌ Deployment failed! Rollback initiated..."
                sh "docker-compose -f docker-compose.yml down -v"
                error("Build failed: ${e.message}")
            }
        }
    }
}
    }
}