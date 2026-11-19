package com.example.jobprocessor.service;

import com.example.jobprocessor.dto.CreateJobRequest;
import com.example.jobprocessor.dto.JobResponse;
import com.example.jobprocessor.entity.Job;
import com.example.jobprocessor.entity.JobStatus;
import com.example.jobprocessor.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobProducer jobProducer;

    public JobService(JobRepository jobRepository, JobProducer jobProducer) {
        this.jobRepository = jobRepository;
        this.jobProducer = jobProducer;
    }

    public JobResponse createJob(CreateJobRequest request) {
        Job job = Job.builder()
                .name(request.name())
                .payload(request.payload())
                .status(JobStatus.PENDING)
                .build();

        Job savedJob = jobRepository.save(job);
        jobProducer.publishJob(savedJob.getId());

        return toResponse(savedJob);
    }

    public JobResponse getJob(UUID id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));

        return toResponse(job);
    }

    private JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getName(),
                job.getPayload(),
                job.getStatus(),
                job.getRetryCount(),
                job.getMaxRetries(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getErrorMessage()
        );
    }
}
