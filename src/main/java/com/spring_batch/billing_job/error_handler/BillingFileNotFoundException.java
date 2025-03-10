package com.spring_batch.billing_job.error_handler;

public class BillingFileNotFoundException extends RuntimeException {
    public BillingFileNotFoundException(String message) {
        super(message);
    }
}
