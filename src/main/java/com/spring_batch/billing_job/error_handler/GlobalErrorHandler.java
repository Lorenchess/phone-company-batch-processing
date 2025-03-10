package com.spring_batch.billing_job.error_handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<CustomErrorResponse> directoryNotFoundException(DirectoryNotFoundException exception) {
       return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BillingFileNotFoundException.class)
    public ResponseEntity<CustomErrorResponse> billingFileNotFoundException(BillingFileNotFoundException exception) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT);
    }

    private ResponseEntity<CustomErrorResponse> buildErrorResponse(Exception exception, HttpStatus status) {
        return buildErrorResponse(exception, status, List.of());
    }

    private ResponseEntity<CustomErrorResponse> buildErrorResponse(Exception exception, HttpStatus status, List<String> details) {
        CustomErrorResponse customErrorResponse = CustomErrorResponse.builder()
                .message(exception.getMessage() != null ? exception.getMessage() : "Unexpected Error occurred")
                .details(details != null && !details.isEmpty() ? details : null)
                .status(status.value())
                .build();
        return ResponseEntity.status(status).body(customErrorResponse);
    }
}
