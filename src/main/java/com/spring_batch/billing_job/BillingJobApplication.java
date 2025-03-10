package com.spring_batch.billing_job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillingJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingJobApplication.class, args);
	}

}
