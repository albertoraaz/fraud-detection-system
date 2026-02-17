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
                        echo "🧹 Pre-deployment cleanup: Clearing ports and stale containers..."

                        // 1. Force down any existing containers from this project
                        sh "docker-compose -f docker-compose.yml down -v --remove-orphans"

                        // 2. Senior safety check: Kill any process (Zombie containers or native services) holding the ports
                        // 2181 = Zookeeper, 9092 = Kafka
                        sh '''
                            for port in 2181 9092; do
                                if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
                                    echo "⚠️ Port $port is busy. Terminating process..."
                                    fuser -k $port/tcp || true
                                fi
                            done
                        '''

                        // 3. Deploy fresh
                        sh "docker-compose -f docker-compose.yml up -d --build"

                        echo "🚀 Deployment Successful!"
                    } catch (Exception e) {
                        echo "❌ Deployment failed! Initiating Automated Rollback..."
                        // Clean up the mess so the NEXT build doesn't fail with the same port error
                        sh "docker-compose -f docker-compose.yml down -v"
                        error("Stopping pipeline due to deployment failure: ${e.message}")
                    }
                }
            }
        }
    }
}