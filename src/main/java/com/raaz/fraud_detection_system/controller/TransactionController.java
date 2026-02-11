package com.raaz.fraud_detection_system.controller;

import com.raaz.fraud_detection_system.domain.Transaction;
import com.raaz.fraud_detection_system.service.KafkaProducerService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    // Senior Tip: Use the POJO directly in the template
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final KafkaProducerService producerService;

    @PostMapping
    @RateLimiter(name = "transactionRateLimiter")
    @Operation(summary = "Generate simulated transactions", description = "Injects 50 random transactions into the 'transactions' topic")
    public String sendTransactions() {
        int successCount = 0;
        int failureCount = 0;
        for (int i = 0; i < 50; i++) {
            String transactionId = "txn-" + UUID.randomUUID().toString().substring(0, 8);

            // Random amount between 8000 and 9100
            double amount = ThreadLocalRandom.current().nextDouble(8000, 9100);

            // Create POJO
            Transaction transaction = new Transaction(transactionId, "USER_" + i, amount, LocalDateTime.now().toString());

            try {
                producerService.send(transactionId, transaction);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                // LOG IT, but DO NOT THROW.
                // This lets the loop continue so the Circuit Breaker counts the hits.
                log.error("Iteration {} failed: {}", i, e.getMessage());
            }

        }
        return "50 Transactions Sent to Kafka";
    }

    /**
     *
     * @param e
     * @return
     */
    public String fallbackKafka(Exception e) {

        log.error("Unable to send message", e);
        return "Service temporarily unavailable. Please try again later.";

    }
}