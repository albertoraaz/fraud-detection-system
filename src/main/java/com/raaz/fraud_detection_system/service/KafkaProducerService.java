package com.raaz.fraud_detection_system.service;

import com.raaz.fraud_detection_system.domain.Transaction;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    // This annotation MUST be here, on the Service method
    @CircuitBreaker(name = "kafkaProducer")
    public void send(String id, Transaction transaction) throws Exception {
        // .get() is vital to "throw" the error back to the Proxy
        kafkaTemplate.send("transactions", id, transaction).get();
    }
}