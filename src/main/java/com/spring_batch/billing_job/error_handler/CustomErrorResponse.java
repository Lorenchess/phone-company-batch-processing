package com.spring_batch.billing_job.error_handler;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomErrorResponse {
    private String message;
    private List<String> details;
    private int status;
}
