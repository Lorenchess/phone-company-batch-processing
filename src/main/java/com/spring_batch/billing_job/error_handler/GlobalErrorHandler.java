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
        //some problems with lombok library with latest intellij
//        CustomErrorResponse customErrorResponse = CustomErrorResponse.builder()
//                .message(exception.getMessage() != null ? exception.getMessage() : "Unexpected Error occurred")
//                .details(details != null && !details.isEmpty() ? details : null)
//                .status(status.value())
//                .build();

        CustomErrorResponse customErrorResponse = new CustomErrorResponse();
        customErrorResponse.setMessage(exception.getMessage() != null ? exception.getMessage() : "Unexpected Error occurred");
        customErrorResponse.setDetails(details != null && !details.isEmpty() ? details : null);
        customErrorResponse.setStatus(status.value());
        return ResponseEntity.status(status).body(customErrorResponse);
    }
}
