# 🛡️ Real-Time Fraud Detection Engine

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Kafka Streams](https://img.shields.io/badge/Kafka%20Streams-3.x-blue)](https://kafka.apache.org/documentation/streams/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](https://opensource.org/licenses/MIT)

A production-ready event-driven system built with **Spring Boot 3.5** and **Kafka Streams**. This project demonstrates a high-throughput architecture for financial safety, focusing on **Exactly-Once Semantics (EOS)**, real-time transaction filtering, and automated documentation via OpenAPI 3.

---

## 🏛️ System Architecture

The system utilizes a stateless stream-processing model to monitor incoming financial transactions and identify suspicious activity with sub-millisecond latency.


### Core Components

* **Transaction Producer**: A RESTful gateway that simulates high-frequency financial events, producing them into the Kafka cluster using `KafkaTemplate`.
* **Fraud Detection Stream**: The core processing engine. It leverages the **Kafka Streams API** to process, filter, and analyze transaction payloads in real-time.
* **Infrastructure Layer**: A containerized environment featuring **Confluent Kafka (7.1.0)** and **Zookeeper**, optimized for single-node development with transactional support.

---

## 🛡️ Reliability & Advanced Features

This engine is engineered for banking-grade consistency and observability:

* **Exactly-Once Semantics (EOS)**: Configured with `exactly_once_v2` to ensure that every transaction is processed exactly once, even in the event of a broker or application failure.
* **Automatic Topic Management**: Uses Spring's `TopicBuilder` to programmatically provision the `transactions` and `fraud-alerts` topics.
* **Interactive API Documentation**: Fully integrated with **Swagger UI (SpringDoc)**, allowing developers to trigger and monitor transaction simulations visually.
* **Type-Safe Serialization**: Implements custom `JsonSerde` for seamless Java-to-JSON transitions within the Kafka pipeline.

---

## 🚀 Getting Started

### Prerequisites

* **Java 21** (LTS)
* **Maven 3.9+**
* **Docker & Docker Compose**

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

---

## 📊 Monitoring & API Verification

The system is instrumented for instant verification through the following endpoints:

| Interface | URL | Description |
| :--- | :--- | :--- |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Interactive API Playground |
| **OpenAPI Docs** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) | Raw JSON Documentation |
| **Kafka Broker** | `localhost:9092` | External Bootstrap Server |

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
* **Kafka UI**: If configured, visit `http://localhost:8081` to view the raw JSON payloads in the `fraud-alerts` topic.

### 3. Validating Exactly-Once Semantics (EOS)
Because the system is configured with `exactly_once_v2`, the **Kafka Transaction Manager** ensures that no partial or duplicate fraud alerts are committed to the `fraud-alerts` topic, even during unexpected service restarts.


---

## 👤 Author
**Alberto Raaz** *February, 2026*
