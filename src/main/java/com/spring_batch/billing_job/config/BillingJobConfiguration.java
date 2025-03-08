package com.spring_batch.billing_job.config;

import com.spring_batch.billing_job.entity.BillingData;
import com.spring_batch.billing_job.entity.ReportingData;
import com.spring_batch.billing_job.error_handler.BillingDataSkipListener;
import com.spring_batch.billing_job.job.BillingDataProcessor;
import com.spring_batch.billing_job.job.FilePreparationTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BillingJobConfiguration {

    @Bean
    public BillingDataProcessor billingDataProcessor() {
        return new BillingDataProcessor();
    }

    @Bean
    public Job job(JobRepository jobRepository, Step step1, Step step2, Step step3) {
        return new JobBuilder("BillingJob", jobRepository)
                .start(step1)
                .next(step2)
                .next(step3)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("filePreparation", jobRepository)
                .tasklet(new FilePreparationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step step2(
            JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<BillingData> billingDataFileReader, ItemWriter<BillingData>billingDataTableWriter,
            BillingDataSkipListener skipListener) {
        return new StepBuilder("fileIngestion", jobRepository)
                .<BillingData, BillingData>chunk(100, transactionManager)
                .reader(billingDataFileReader)
                .writer(billingDataTableWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skipLimit(10)
                .listener(skipListener)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<BillingData> billingDataFileReader(@Value("#{jobParameters['input.file']}") String inputFile) {
        return new FlatFileItemReaderBuilder<BillingData>()
                .name("billingDataFileReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .names("dataYear", "dataMonth", "accountId", "phoneNumber", "dataUsage", "callDuration", "smsCount")
                .targetType(BillingData.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<BillingData> billingDataTableWriter(DataSource dataSource) {
        String sql = "insert into BILLING_DATA values (:dataYear, :dataMonth, :accountId, :phoneNumber, :dataUsage, :callDuration, :smsCount)";
        return new JdbcBatchItemWriterBuilder<BillingData>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<BillingData> billingDataTableReader(
            DataSource dataSource,
            @Value("#{jobParameters['data.year']}") Integer year,
            @Value("#{jobParameters['data.month']}") Integer month
    ) {
        String sql = String.format("select * from BILLING_DATA where DATA_YEAR = %d and DATA_MONTH = %s", year, month);
        return new JdbcCursorItemReaderBuilder<BillingData>()
                .name("billingDataTableReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new DataClassRowMapper<>(BillingData.class))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ReportingData> billingDataFileWriter(@Value("#{jobParameters['outpu.file']}")String outputFile) {
        return new FlatFileItemWriterBuilder<ReportingData>()
                .resource(new FileSystemResource(outputFile))
                .name("billingDataFileWriter")
                .delimited()
                .names("billingData.dataYear", "billingData.dataMonth", "billingData.accountId", "billingData.phoneNumber",
                        "billingData.dataUsage", "billingData.callDuration", "billingData.smsCount", "billingTotal")
                .build();
    }

    @Bean
    public Step step3(
            JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<BillingData>billingDataTableReader,
            ItemProcessor<BillingData, ReportingData>billingDataProcessor,
            ItemWriter<ReportingData>billingDataFileWriter
    ) {
        return new StepBuilder("reportGeneration", jobRepository)
                .<BillingData, ReportingData>chunk(100, transactionManager)
                .reader(billingDataTableReader)
                .processor(billingDataProcessor)
                .writer(billingDataFileWriter)
                .build();
    }

    @Bean
    @StepScope
    public BillingDataSkipListener skipListener(@Value("#{jobParameters=['skip.file']}") String skippedFile) {
        return new BillingDataSkipListener(skippedFile);
    }

}
