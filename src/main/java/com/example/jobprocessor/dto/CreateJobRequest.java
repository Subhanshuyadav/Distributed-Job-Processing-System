package com.example.jobprocessor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(
        @NotBlank(message = "Job name is required")
        @Size(max = 255, message = "Job name must not exceed 255 characters")
        String name,
        String payload
) {
}
