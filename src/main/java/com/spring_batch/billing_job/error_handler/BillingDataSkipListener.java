package com.spring_batch.billing_job.error_handler;

import com.spring_batch.billing_job.entity.BillingData;
import lombok.NonNull;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BillingDataSkipListener implements SkipListener<BillingData, BillingData> {
    Path skippedItemsFile;

    public BillingDataSkipListener(String skippedItemsFile) {
        this.skippedItemsFile = Paths.get(skippedItemsFile);
    }

    @Override
    public void onSkipInRead(@NonNull Throwable throwable) {
        if (throwable instanceof FlatFileParseException exception) {
            String rawLine = exception.getInput();
            int lineNumber = exception.getLineNumber();
            String skippedLine = lineNumber + "|" + rawLine + System.lineSeparator();
            try {
                Files.writeString(this.skippedItemsFile, skippedLine, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new RuntimeException("Unable to write skipped item " + skippedLine);
            }
        }
    }
}
