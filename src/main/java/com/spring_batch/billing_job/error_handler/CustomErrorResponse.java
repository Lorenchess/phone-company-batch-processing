package com.spring_batch.billing_job.error_handler;

import lombok.*;

import java.util.List;

//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class CustomErrorResponse {
    private String message;
    private List<String> details;
    private int status;


    public CustomErrorResponse() {
    }

    public CustomErrorResponse(String message, List<String> details, int status) {
        this.message = message;
        this.details = details;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
