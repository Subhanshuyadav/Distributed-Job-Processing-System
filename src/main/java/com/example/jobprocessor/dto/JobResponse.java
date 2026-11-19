package com.example.jobprocessor.dto;

import com.example.jobprocessor.entity.JobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobResponse(
        UUID id,
        String name,
        String payload,
        JobStatus status,
        Integer retryCount,
        Integer maxRetries,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
}
