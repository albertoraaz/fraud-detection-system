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
                // Ensure your Dockerfile uses 'FROM eclipse-temurin:21-jre-alpine'
                // to match the Java 21 requirement!
                sh 'docker build -t yourdockerhubuser/fraud-detection:latest .'
            }
        }
    }
}