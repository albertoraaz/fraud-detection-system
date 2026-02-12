# 🛡️ Real-Time Fraud Detection Engine

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Kafka Streams](https://img.shields.io/badge/Kafka%20Streams-3.x-blue)](https://kafka.apache.org/documentation/streams/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](https://opensource.org/licenses/MIT)

A production-ready event-driven system built with **Spring Boot 3.5** and **Kafka Streams**. This project demonstrates a high-throughput architecture for financial safety, focusing on **Exactly-Once Semantics (EOS)**, real-time transaction filtering, and automated documentation via OpenAPI 3.

## 🚀 Key Features

- **Real-time Ingestion:** REST API for high-throughput transaction simulation.
- **Distributed Streaming:** Leverages Kafka for asynchronous message processing.
- **Fault Tolerance:** Implements Circuit Breaker and Retry patterns.
- **Automated CI/CD:** Fully orchestrated lifecycle managed by Jenkins and GitHub Apps.
- **Containerization:** Multi-stage Docker builds for minimal, hardened production images.
- **Automated Rollback:** Smart deployment logic that reverts to the last stable version if a build or health check fails.
- **Observability:** Health monitoring and metrics exposure via Spring Boot Actuator.
- **API Documentation:** Interactive documentation via Swagger/OpenAPI.

---

## 🏛️ System Architecture

The system utilizes a stateless stream-processing model to monitor incoming financial transactions and identify suspicious activity with sub-millisecond latency.

### The Architecture Diagram

Data Flow & CI/CD Lifecycle

### System Architecture

```mermaid
graph TD
    subgraph External_Sources
        T[Transaction Source] --> K_In((Kafka Topic: transactions-input))
    end

    subgraph Spring_Boot_Application
        K_In --> KS[Kafka Streams Processor]
        KS --> State[(RocksDB Local State)]
        KS --> Logic{Fraud Logic / Resilience4j}
        Logic -->|Legit| K_Out((Kafka Topic: processed))
        Logic -->|Fraud| K_Alert((Kafka Topic: fraud-alerts))
    end

    subgraph CI_CD_Infrastructure
        GH[GitHub Repo] -->|Webhook| J[Jenkins Server]
        J -->|Build| D[Docker Hub]
        D -->|Pull| PROD[Ubuntu Deployment]
    end

    subgraph Security
        Logic -.-> KC[Keycloak / OAuth2]
    end
   ```

### The Distributed System Diagram (Physical View)

```mermaid
graph LR
subgraph Docker_Network
App[Spring Boot App]
K[Kafka Broker]
Z[Zookeeper]
KC[Keycloak]
end

    User((User/Driver)) -->|REST/HTTPS| App
    App <--> K
    K <--> Z
    App <--> KC
```

### Core Components

* **Transaction Producer**: A RESTful gateway that simulates high-frequency financial events, producing them into the Kafka cluster using `KafkaTemplate`.
* **Fraud Detection Stream**: The core processing engine. It leverages the **Kafka Streams API** to process, filter, and analyze transaction payloads in real-time.
* **Infrastructure Layer**: A containerized environment featuring **Confluent Kafka (7.1.0)** and **Zookeeper**, optimized for single-node development with transactional support.

---

## 🏗️ CI/CD Pipeline & DevOps Lifecycle
This project utilizes a "Commit-to-Cloud" workflow. Every push to the repository triggers an automated pipeline:

* **Secure Authentication:** Jenkins connects to GitHub via GitHub App Credentials (using PKCS#8 RSA keys).
* **Standardized Build:** Compilation occurs in a controlled environment using Maven 3.9.6 and OpenJDK 21.
* **Hardened Dockerization:** * Build Stage: Uses maven:3.9.6-eclipse-temurin-21-alpine.
* **Runtime Stage:** Uses eclipse-temurin:21-jre-alpine with a non-root user for enhanced security.
* **Automated Registry Push:** Verified images are versioned and pushed to Docker Hub.
* **Rollback Strategy:** The pipeline includes a failure-handling block. If the deployment or integration tests fail, the system automatically triggers a rollback to the previous stable Docker image tag, ensuring zero-downtime and system reliability.

---

## 🛡️ Reliability & Advanced Features

This engine is engineered for banking-grade consistency and observability:

* **Exactly-Once Semantics (EOS)**: Configured with `exactly_once_v2` to ensure that every transaction is processed exactly once, even in the event of a broker or application failure.
* **Automatic Topic Management**: Uses Spring's `TopicBuilder` to programmatically provision the `transactions` and `fraud-alerts` topics.
* **Interactive API Documentation**: Fully integrated with **Swagger UI (SpringDoc)**, allowing developers to trigger and monitor transaction simulations visually.
* **Type-Safe Serialization**: Implements custom `JsonSerde` for seamless Java-to-JSON transitions within the Kafka pipeline.

---
## 🏗️ Technical Hardening: Resiliency & Security

This project follows standard hardening (**OWASP**, **NIST**) to ensure data protection, failure isolation, and infrastructure integrity through a **Zero-Trust** approach.

| Feature | Implementation Status | Tools / Technology |
| :--- | :--- | :--- |
| **Resiliency** | Failure Isolation & Retries | **Resilience4j** |
| **Security Scanning** | Automated Vulnerability Audits | **GitHub Actions, Snyk, Trivy** |
| **Static Analysis** | SAST for Logic Flaws | **GitHub CodeQL** |
| **API Protection** | Hardening & Rate Limiting | **Spring Security, Rate Limiter** |
| **Secret Management** | Zero-Leak Policy | GitHub Secrets, `.gitignore` |

### 1. Failure Isolation & Resiliency
* **Circuit Breakers**: Implemented via **Resilience4j** to prevent cascading failures. If the Kafka broker or downstream services are slow, the application "fails fast" to remain responsive and protect system resources.
* **Graceful Degradation**: Configured retries with exponential backoff for transient network issues, ensuring data delivery without overwhelming the infrastructure.

### 2. Zero-Trust Identity & Access Management
* **Rate Limiting**: Protects the ingestion API from DDoS and brute-force attacks by limiting the number of requests per second per client, ensuring fair resource distribution.

### 3. Automated DevSecOps & Scanning
Every push and Pull Request triggers a CI/CD pipeline via **GitHub Actions** that automates:
* **Dependency Scanning (Snyk)**: Detects and alerts on vulnerabilities (**SCA**) within the Maven dependency tree.
* **Container Scanning (Trivy)**: Audits Docker images (Kafka, Zookeeper, and the App) for OS-level vulnerabilities.
* **CodeQL (SAST)**: Performs semantic analysis of the source code to find security flaws like SQL injection or insecure cryptography.

### 4. Data Integrity & Statelessness
* **Stateless Design**: The service is strictly stateless, allowing for horizontal scaling across multiple pods without session stickiness issues.
* **Encryption**: Supports encryption in transit (**TLS**) for Kafka communication and secure handling of sensitive transaction payloads.
* **Secrets Management**: Credentials and Client Secrets are managed via environment variables and **GitHub Secrets**. A template-based configuration (`application.yml.example`) is used to ensure no sensitive data is ever committed to version control.

---
## 📊 Monitoring, API Verification & Observability

The system is instrumented for instant verification through the following endpoints:

| Interface | URL | Description |
| :--- | :--- | :--- |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Interactive API Playground |
| **OpenAPI Docs** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) | Raw JSON Documentation |
| **Kafka Broker** | `localhost:9092` | External Bootstrap Server |

Monitoring: Spring Boot Actuator
This project implements **Spring Boot Actuator** to provide deep visibility into the system's operational health and the state of our resiliency patterns.

**Key Features:**
* **Health Endpoint:** Exposed at `/actuator/health` to provide real-time status of the application, disk space, and Kafka connectivity.
* **Circuit Breaker Metrics:** The health check specifically includes the `circuitBreakers` component, showing whether the state is `CLOSED`, `OPEN`, or `HALF_OPEN`.
* **Detailed Analytics:** Tracks `bufferedCalls`, `failedCalls`, and `notPermittedCalls` (requests blocked by the open circuit).

**How to verify:**
1. Stop the Kafka broker: `docker-compose stop broker`.
2. Send transactions via Swagger UI.
3. Check `http://localhost:8080/actuator/health` to see the state transition to `CIRCUIT_OPEN`.

---

## 🧪 Manual Testing & Simulation

The system provides an interactive simulation interface via Swagger UI, allowing you to validate the end-to-end data pipeline without writing external scripts.


### 1. Triggering Transactions
1. Access the **Swagger UI** at `http://localhost:8080/swagger-ui.html`.
2. Locate the `Transactions` section and the `POST /api/transactions` endpoint.
3. Click **"Try it out"** and then **"Execute"**.
4. The system will automatically generate **50 simulated transactions** with randomized IDs and amounts.

### 2. Verifying the Logic
To verify that the Fraud Detection engine is correctly filtering high-value events:
* **Terminal Logs**: Monitor the application console. You will see `DEBUG` logs for every incoming transaction and `WARN` alerts for those flagged as suspicious (e.g., > $10,000).
* **Kafka UI**: If configured, visit `http://localhost:8080` to view the raw JSON payloads in the `fraud-alerts` topic.

### 3. Validating Exactly-Once Semantics (EOS)
Because the system is configured with `exactly_once_v2`, the **Kafka Transaction Manager** ensures that no partial or duplicate fraud alerts are committed to the `fraud-alerts` topic, even during unexpected service restarts.

---

## 🚀 Getting Started

### Prerequisites

* **Java 21** (LTS)
* **Maven 3.9+**
* **Docker & Docker Compose**

## 🛠️ Configuration & Setup

This project follows best practices by using a template-based configuration system. This ensures that local environment settings and potential secrets remain outside of version control.

### Local Environment Setup
To get the application running on your local machine, follow these steps:

1. **Create your local configuration file** Copy the example template to create your actual `application.yml` file:
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application.yml

### ⚙️ Jenkins Environment Setup

To replicate this pipeline, configure your Jenkins instance with:

JDK Tool: Name: JAVA_21 | Path: /usr/lib/jvm/java-21-openjdk-amd64

Maven Tool: Name: 3.9.6 | Version: 3.9.6

Credentials:

github-app-creds: GitHub App Private Key for secure repository access.

dockerhub-credentials: Username/Password for image distribution.

### The Execution Workflow

To launch the detection system on your local machine:

1.  **Start the Infrastructure**:
    ```bash
    docker-compose up -d
    ```
2.  **Compile & Package**:
    ```bash
    mvn clean package -DskipTests
    ```
3.  **Run the Application**:
    ```bash
    mvn spring-boot:run
    ```



## 👤 Author
**Alberto Raaz** *February, 2026*
