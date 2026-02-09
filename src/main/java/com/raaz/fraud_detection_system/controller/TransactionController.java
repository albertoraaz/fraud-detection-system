package com.raaz.fraud_detection_system.controller;

import com.raaz.fraud_detection_system.domain.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequiredArgsConstructor // Automatically generates constructor for the KafkaTemplate
@Slf4j
@Tag(name = "Transactions", description = "Endpoint for simulated transaction injection")
public class TransactionController {

    // Senior Tip: Use the POJO directly in the template
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    @PostMapping
    @Operation(summary = "Generate simulated transactions", description = "Injects 50 random transactions into the 'transactions' topic")
    public String sendTransactions() {
        for (int i = 0; i < 50; i++) {
            String transactionId = "txn-" + UUID.randomUUID().toString().substring(0, 8);

            // Random amount between 8000 and 9100
            double amount = ThreadLocalRandom.current().nextDouble(8000, 9100);

            // Create POJO
            Transaction transaction = new Transaction(
                    transactionId,
                    "USER_" + i,
                    amount,
                    LocalDateTime.now().toString()
            );

            // No manual ObjectMapper! Spring handles the conversion.
            kafkaTemplate.send("transactions", transactionId, transaction)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Sent message=[{}] with offset=[{}]", transactionId, result.getRecordMetadata().offset());
                        } else {
                            log.error("Unable to send message=[{}] due to : {}", transactionId, ex.getMessage());
                        }
                    });
        }
        return "50 Transactions Sent to Kafka";
    }
}