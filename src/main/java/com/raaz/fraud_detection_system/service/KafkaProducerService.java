package com.raaz.fraud_detection_system.service;

import com.raaz.fraud_detection_system.domain.Transaction;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    // This annotation MUST be here, on the Service method
    @CircuitBreaker(name = "kafkaProducer")
    public void send(String id, Transaction transaction) throws Exception {
        try {
            // Ensure the exception is thrown out of the method for the Aspect to see
            kafkaTemplate.send("transactions", id, transaction).get();
        } catch (Exception e) {
            log.error("Circuit Breaker capturing failure for ID: {}", id);
            throw e; // RE-THROW IS MANDATORY
        }
    }
}