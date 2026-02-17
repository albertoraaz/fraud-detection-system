package com.raaz.fraud_detection_system.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener;

@Configuration
public class MetricsConfig {
    @Bean
    public KafkaStreamsMicrometerListener kafkaStreamsMicrometerListener(MeterRegistry meterRegistry) {
        return new KafkaStreamsMicrometerListener(meterRegistry);
    }
}
