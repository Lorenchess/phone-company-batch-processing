package com.spring_batch.billing_job.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class BillingJobScheduler {

    private final JobLauncher jobLauncher;

    private final Job billingJob;

    private static final String INPUT_DIR = "input/";
    private static final String OUTPUT_DIR = "staging/";

    public BillingJobScheduler(JobLauncher jobLauncher, Job billingJob) {
        this.jobLauncher = jobLauncher;
        this.billingJob = billingJob;
    }

    @Scheduled(cron = "0 0 0 L * ?")
    public void runScheduleBillingJob() {
        LocalDate today = getCurrentDate();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();


        File inputFile = getBillingFile(currentYear, currentMonth);

        if (inputFile.exists()) {
            try {
                processFile(inputFile, currentYear, currentMonth);
            } catch (JobExecutionException e) {
                System.out.println("Failed to execute the job " + e.getMessage());
            }
        } else {
            System.out.println("No matching file found for this month's billing cycle: " + getExpectedFileName(currentYear, currentMonth));
        }
    }

    private void processFile(File file, int currentYear, int currentMonth) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        System.out.println("Processing file: " + file.getName());

        String outputFile = OUTPUT_DIR + "billing-report-" + file.getName();
        String skipFile = OUTPUT_DIR + "billing-data-skip-" + file.getName().replace(".csv", ".psv");

        Map<String, JobParameter<?>> jobParams = new HashMap<>();
        jobParams.put("input.file", new JobParameter<>(file.getAbsolutePath(), String.class));
        jobParams.put("output.file", new JobParameter<>(outputFile, String.class));
        jobParams.put("skip.file", new JobParameter<>(skipFile, String.class));
        jobParams.put("data.year", new JobParameter<>((long)currentYear, Long.class));
        jobParams.put("data.month", new JobParameter<>((long)currentMonth, Long.class));
        jobParams.put("timestamp", new JobParameter<>(System.currentTimeMillis(), Long.class));

        JobParameters jobParameters = new JobParameters(jobParams);
        this.jobLauncher.run(this.billingJob, jobParameters);
    }

    public File getBillingFile(int year, int month) {
        String fileName = String.format("billing-%d-%02d.csv", year, month);
        return new File(INPUT_DIR + fileName);
    }

    private String getExpectedFileName(int year, int month) {
        String expectedFileName = String.format("billing-%d-%02d.csv", year, month);
        System.out.println("Processing file: " + expectedFileName);
        return expectedFileName;
    }

    protected LocalDate getCurrentDate() {
        return LocalDate.now();
    }


}
