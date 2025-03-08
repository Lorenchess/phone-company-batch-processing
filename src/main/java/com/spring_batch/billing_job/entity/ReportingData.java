package com.spring_batch.billing_job.entity;

public record ReportingData(BillingData billingData, double billingTotal) {
}
