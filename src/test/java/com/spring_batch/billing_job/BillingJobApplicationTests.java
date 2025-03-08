package com.spring_batch.billing_job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class BillingJobApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void setup() {
		this.jobRepositoryTestUtils.removeJobExecutions();
		JdbcTestUtils.deleteFromTables(this.jdbcTemplate, "BILLING_DATA");
	}

	@Test
	void testJobExecution() throws Exception {
		//given
		/*JobParameters jobParameters = new JobParametersBuilder()
				.addString("input.file", "/some/input/file")
				.toJobParameters(); */

		JobParameters jobParameters = this.jobLauncherTestUtils.getUniqueJobParametersBuilder()
				.addString("input.file", "src/main/resources/billing-2025-01.csv")
				.toJobParameters();

		//when
		JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);

		//then
		assertTrue(Files.exists(Paths.get("staging", "billing-2025-01.csv")));

		assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

		assertEquals(1000, JdbcTestUtils.countRowsInTable(jdbcTemplate, "BILLING_DATA"));

		Path billingReport = Paths.get("staging", "billing-report-2025-01.csv");

		assertTrue(Files.exists(billingReport));
//		assertEquals(, Files.lines(billingReport).count());
	}

}
