package com.raaz.fraud_detection_system.integration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Slf4j
@TestPropertySource(properties = {"spring.kafka.admin.fail-fast=false", "spring.kafka.admin.auto-create=false", "spring.kafka.streams.auto-startup=false"})
public class KafkaProducerIntegrationTest {

    // 1. Set up a real Kafka container for the test

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withEmbeddedZookeeper().waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*] [Ss]tarted.*", 1));

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CircuitBreakerRegistry registry;


    // 2. Dinamically point Sprint to the TestContainer's random port
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        if (!kafka.isRunning()) {
            kafka.start();
        }

        CircuitBreaker breaker = registry.circuitBreaker("kafkaProducer");
        breaker.reset();

        // WARM-UP: The first connection to Kafka is always the slowest.
        // Try one POST to "wake up" the producer before the actual test count starts.
        testRestTemplate.postForEntity("/api/transactions", null, String.class);

        log.info("🛡️ Infrastructure warmed up. Circuit Breaker: {}", breaker.getState());
    }

    @Test
    public void testFullTransactionFlow_AndCircuitBreakerState() {

        // ARRANGE: Verify Circuit Breaker is initially CLOSED
        CircuitBreaker breaker = registry.circuitBreaker("kafkaProducer");
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());

        // call the endpoint that triggers 50 transactions
        ResponseEntity<String> response = testRestTemplate.postForEntity("/api/transactions", null, String.class);        // ASSERT: Check API response
        System.out.println("DEBUG API RESPONSE: " + response.getBody());
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Success: 50") || response.getBody().contains("Success: 49"));

        // ASSERT: Verify Circuit Breaker stayed CLOSED because Kafka was healthy
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());

    }


    @Test
    @org.springframework.test.annotation.DirtiesContext
    public void testCircuitBreakerTrips_WhenKafkaGoesDown() {
        CircuitBreaker breaker = registry.circuitBreaker("kafkaProducer");
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());

        // 1. Kill Kafka
        kafka.stop();

        // 2. We need enough calls to cross the 'minimumNumberOfCalls' threshold
        // If your config is 5, call it 6-7 times.
        for (int i = 0; i < 10; i++) {
            try {
                testRestTemplate.postForEntity("/api/transactions", null, String.class);
            } catch (Exception e) {
                // We expect exceptions here because Kafka is gone
            }
        }

        // 3. The Circuit Breaker should now be OPEN because the calls failed fast (within 2s each)
        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());
    }

    @AfterEach
    void cleanup() {
        // If we stopped Kafka during a chaos test, restart it for the next one
        if (!kafka.isRunning()) {
            kafka.start();
        }
    }

}