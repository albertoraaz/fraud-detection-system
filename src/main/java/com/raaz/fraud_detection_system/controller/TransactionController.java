package com.raaz.fraud_detection_system.controller;

import com.raaz.fraud_detection_system.domain.Transaction;
import com.raaz.fraud_detection_system.service.KafkaProducerService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            }

        }
        return String.format("Results - Success: %d, Failures: %d", successCount, failureCount);
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