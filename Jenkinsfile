pipeline {
    agent any

    tools {
            // This MUST match the name you gave in Step 2
            jdk 'JAVA_21'
            maven '3.9.6' // Ensure you have a Maven tool configured as well
        }

    stages {
        stage('Checkout') {
            steps {
                // This uses the GitHub App connection to pull the code
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                // Compiles your Java code and runs tests
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                // Creates the image using the Dockerfile you made earlier
                sh 'docker build -t yourdockerhubuser/fraud-detection:latest .'
            }
        }
    }

    post {
        success {
            echo "Successfully built Alberto's Fraud Detection System!"
        }
    }
}