package com.raaz.fraud_detection_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.raaz.fraud_detection_system")
public class FraudDetectionSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectionSystemApplication.class, args);
	}

}
