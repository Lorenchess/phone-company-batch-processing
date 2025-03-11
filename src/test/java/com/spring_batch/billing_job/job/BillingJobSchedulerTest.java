package com.spring_batch.billing_job.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingJobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job billingJob;

    @InjectMocks
    private BillingJobScheduler billingJobScheduler;

    @Captor
    private ArgumentCaptor<JobParameters> jobParametersCaptor;

    private final String INPUT_DIR = "input/";
    private final String OUTPUT_DIR = "staging/";

    private LocalDate today;
    private int currentYear;
    private int currentMonth;
    private String expectedFileName;
    private File testFile;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        currentYear = today.getYear();
        currentMonth = today.getMonthValue();
        expectedFileName = String.format("billing-%d-%02d.csv", currentYear, currentMonth);
        testFile = new File(INPUT_DIR + expectedFileName);
    }

    @Test
    void testRunScheduleBillingJob_WhenFileExists_ShouldProcessFile() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        //when
        when(jobLauncher.run(eq(billingJob), any(JobParameters.class))).thenReturn(mock(JobExecution.class));

        //then
        billingJobScheduler.runScheduleBillingJob();

        //verify
        verify(jobLauncher, times(1)).run(eq(billingJob), jobParametersCaptor.capture());

        JobParameters capturedParams = jobParametersCaptor.getValue();

        assertNotNull(capturedParams);
        assertEquals(testFile.getAbsolutePath(), capturedParams.getString("input.file"));
        assertEquals(OUTPUT_DIR + "billing-report-" + testFile.getName(), capturedParams.getString("output.file"));
        assertEquals(OUTPUT_DIR + "billing-data-skip-" + testFile.getName().replace(".csv", ".psv"), capturedParams.getString("skip.file"));
        assertEquals((long) currentYear, capturedParams.getLong("data.year"));
        assertEquals((long) currentMonth, capturedParams.getLong("data.month"));
    }

    @Test
    void testRunScheduleBillingJob_WhenFileDoesNotExist_ShouldNotProcessFile() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        //when
        LocalDate testDate = LocalDate.of(2024,1,1);
        int testYear = testDate.getYear();
        int testMonth = testDate.getMonthValue();
        String testFileName = String.format("billing-%d-%02d.csv", testYear, testMonth);
        File mockFile = mock(File.class);

        when(mockFile.exists()).thenReturn(false);

        BillingJobScheduler spyScheduler = spy(billingJobScheduler);
        doReturn(testDate).when(spyScheduler).getCurrentDate(); // Mock the date
        doReturn(mockFile).when(spyScheduler).getBillingFile(testYear, testMonth);

        // when
        spyScheduler.runScheduleBillingJob();

        // then
        verify(jobLauncher, never()).run(any(), any());
    }


}