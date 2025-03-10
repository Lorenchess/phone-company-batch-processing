package com.spring_batch.billing_job.job;

import com.spring_batch.billing_job.error_handler.BillingFileNotFoundException;
import com.spring_batch.billing_job.error_handler.DirectoryNotFoundException;
import lombok.NonNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Stream;

public class FilePreparationTasklet implements Tasklet {

    private static final String INPUT_DIR = "input";
    private static final String OUTPUT_DIR = "staging";

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution,@NonNull ChunkContext chunkContext) throws Exception {

        YearMonth currentYearMonth = YearMonth.now();

        Path inputPath = Paths.get(INPUT_DIR);
        Path stagingPath = Paths.get(OUTPUT_DIR);

        checkExistanceOfFileDirPath(inputPath);

        if (!Files.exists(stagingPath)) {
            Files.createDirectories(stagingPath);
        }

        Path fileToProcess;

        try(Stream<Path> files = Files.list(inputPath)) {
            fileToProcess = files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().startsWith(currentYearMonth.toString())
                            && file.getFileName().toString().endsWith("csv"))
                    .findFirst().orElseThrow(() -> new BillingFileNotFoundException("No matching billing file found for: " + currentYearMonth));
        }


        Path target = stagingPath.resolve(fileToProcess.getFileName());
        Files.copy(fileToProcess, target, StandardCopyOption.REPLACE_EXISTING);

        //increment read count for logging
        contribution.incrementReadCount();

        //log job parameters
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        System.out.println("Processing file: " + fileToProcess.getFileName());
        System.out.println("Job parameters: " + jobParameters);


        return RepeatStatus.FINISHED;
    }

    private void checkExistanceOfFileDirPath(Path path) {
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new DirectoryNotFoundException("Directory not found" + path.toAbsolutePath());
        }
    }
}
