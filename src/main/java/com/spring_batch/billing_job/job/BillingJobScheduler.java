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
@RequiredArgsConstructor
public class BillingJobScheduler {

    private final JobLauncher jobLauncher;

    private final Job billingJob;

    private static final String INPUT_DIR = "input/";
    private static final String OUTPUT_DIR = "staging/";

    @Scheduled(cron = "0 0 0 L * ?")
    public void runScheduleBillingJob() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        String expectedFileName = String.format("billing-%d-%02d.csv", currentYear, currentMonth);
        File inputFile = new File(INPUT_DIR + expectedFileName);

        if (inputFile.exists()) {
            try {
                processFile(inputFile, currentYear, currentMonth);
            } catch (JobExecutionException e) {
                System.out.println("Failed to execute the job " + e.getMessage());
            }
        } else {
            System.out.println("No matching file found for this month's billing cycle: " + expectedFileName);
        }
    }

    private void processFile(File file, int currentYear, int currentMonth) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        System.out.println("Processing file: " + file.getName());

        String outputFile = OUTPUT_DIR + "billing-report-" + file.getName();
        String skipFile = OUTPUT_DIR + "billing-data-skip" + file.getName().replace(".csv", ".psv");

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


}
