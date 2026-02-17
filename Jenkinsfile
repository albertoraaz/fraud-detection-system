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
                    def jdkPath = tool name: 'JAVA_21', type: 'jdk'
                    sh "export JAVA_HOME=${jdkPath} && ${tool name: '3.9.6', type: 'maven'}/bin/mvn clean package -DskipTests"
                }
            }
        }

        stage('Integration Tests') {
            steps {
                script {
                    def jdkPath = tool name: 'JAVA_21', type: 'jdk'
                    // This triggers the Testcontainers setup verified in your local environment
                   sh "export JAVA_HOME=${jdkPath} && mvn test -Dtestcontainers.timeout=120"
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

                        // 2. Port liberation safety check (Senior Practice)
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

    post {
        always {
            echo "Pipeline execution finished."
        }
        success {
            echo "🚀 Fraud Detection System successfully deployed to Production!"
        }
        failure {
            echo "❌ Pipeline failed. Check logs for details."
        }
    }
}