package com.devpulse.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ErrorLogRequest {

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String environment= "production";

    @NotBlank(message = "Error msg is required")
    private String errorMessage;

    private String stackTrace;

    private String severity = "ERROR";
}
