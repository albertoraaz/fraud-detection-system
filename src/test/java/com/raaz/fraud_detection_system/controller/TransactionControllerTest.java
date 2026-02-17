package com.raaz.fraud_detection_system.controller;


import com.raaz.fraud_detection_system.domain.Transaction;
import com.raaz.fraud_detection_system.streams.FraudDetectionStream;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.test.context.ActiveProfiles;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionControllerTest {

    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<String, Transaction> testInputTopic;
    private TestOutputTopic<String, Transaction> testOutputTopic;


    @BeforeEach
    void setUp() {

        // initialize the stream topology

        StreamsBuilder builder = new StreamsBuilder();

        FraudDetectionStream fraudDetectionConfig = new FraudDetectionStream();

        fraudDetectionConfig.fraudDetectStream(builder);

        Topology topology = builder.build();

        // set the properties

        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-fraud-detector");

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "example:1234");

        JsonSerde<Transaction> stringSerde = new JsonSerde<>(Transaction.class);

        topologyTestDriver = new TopologyTestDriver(topology, props);

        // the input and output topics

        testInputTopic = topologyTestDriver.createInputTopic("transactions", Serdes.String().serializer(), stringSerde.serializer());

        testOutputTopic = topologyTestDriver.createOutputTopic("fraud-alerts", Serdes.String().deserializer(), stringSerde.deserializer());


    }


    @AfterEach
    void tearDown() {
        topologyTestDriver.close();
    }


    @Test
    void shouldFilterHighValueTransactionsAsFraud() {

        Transaction normalTxn = new Transaction();
        normalTxn.setTransactionId("txn-1");
        normalTxn.setUserId("user-1");
        normalTxn.setAmount(10);
        normalTxn.setTimestamp("now");

        Transaction suspiciousTxn = new Transaction();
        suspiciousTxn.setTransactionId("txn-2");
        suspiciousTxn.setUserId("user-2");
        suspiciousTxn.setAmount(20000.0);
        suspiciousTxn.setTimestamp("now");

        testInputTopic.pipeInput("key1", normalTxn);
        testInputTopic.pipeInput("key2", suspiciousTxn);

        assertFalse(testOutputTopic.isEmpty(), "Output topic should contain the fraud alert");
        Transaction alertedTxn = testOutputTopic.readValue();

        assertEquals("txn-2", alertedTxn.getTransactionId());
        assertEquals(20000.0, alertedTxn.getAmount());
        assertTrue(testOutputTopic.isEmpty(), "Normal transaction should not have triggered an alert");

    }

    @Test
    void shouldHandleMalformedTransactionsGracefully() {
        // Arrange
        Transaction malformedTxn = new Transaction("txn-3", "user-3", -50.0, "now");

        // Act
        testInputTopic.pipeInput("key3", malformedTxn);

        // Assert
        assertTrue(testOutputTopic.isEmpty(), "Malformed transactions should be ignored by the filter");
    }


}
