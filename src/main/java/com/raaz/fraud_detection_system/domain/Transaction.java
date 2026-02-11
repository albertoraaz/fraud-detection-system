package com.raaz.fraud_detection_system.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Callback {
    private String transactionId;
    private String userId;
    private double amount;
    private String timestamp;

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {

    }
}
