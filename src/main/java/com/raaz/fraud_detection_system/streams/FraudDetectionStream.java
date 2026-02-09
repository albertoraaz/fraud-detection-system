package com.raaz.fraud_detection_system.streams;

import com.raaz.fraud_detection_system.domain.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@EnableKafkaStreams
@Slf4j
public class FraudDetectionStream {

    // create bean
    @Bean
    public KStream<String, Transaction> fraudDetectStream(StreamsBuilder streamsBuilder) {

        // define the Serdes for custom POJO -> turn java object into JSON
        JsonSerde<Transaction> jsonSerde = new JsonSerde<>(Transaction.class);

        // -> READ TOPIC -> consumed the transaction topic
        KStream<String, Transaction> transactionStream = streamsBuilder.stream("transactions", Consumed.with(Serdes.String(), jsonSerde)).peek((k, v) -> log.info("👀 Incoming Transaction: ID={}, Amt={}", v.getTransactionId(), v.getAmount()));

        // -> PROCESS FILTER
        KStream<String, Transaction> fraudStream = transactionStream.filter((key, value) -> isSuspicious(value));
        // -> WRITE DESTINATION
        fraudStream.to("fraud-alerts", Produced.with(Serdes.String(), jsonSerde));

        return transactionStream;
    }

    private static boolean isSuspicious(Transaction transaction) {
        // Always guard against nulls in streams to prevent thread death
        if (transaction == null || transaction.getAmount() <= 0) {
            log.error("Received malformed or empty transaction. Skipping.");
            return false;
        }

        boolean isHighValue = transaction.getAmount() > 10000;
        if (isHighValue) {
            log.warn("🚨 FRAUD ALERT: High-Value | ID: {} | Amt: {}", transaction.getTransactionId(), transaction.getAmount());
        }
        return isHighValue;
    }

}