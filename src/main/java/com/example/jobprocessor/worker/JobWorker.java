package com.example.jobprocessor.worker;

import com.example.jobprocessor.entity.Job;
import com.example.jobprocessor.entity.JobStatus;
import com.example.jobprocessor.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class JobWorker {

    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);
    private static final String JOB_TOPIC = "job-topic";

    private final JobRepository jobRepository;

    public JobWorker(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @KafkaListener(topics = JOB_TOPIC, groupId = "job-worker-group")
    public void processJob(String jobIdMessage) {
        log.info("Received job message: {}", jobIdMessage);

        final UUID jobId;
        try {
            jobId = UUID.fromString(jobIdMessage);
        } catch (IllegalArgumentException exception) {
            log.error("Received invalid job UUID from Kafka: {}", jobIdMessage, exception);
            return;
        }

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Job not found for Kafka message: {}", jobId);
            return;
        }

        try {
            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Job {} status changed to RUNNING", jobId);

            process(job);

            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Job {} status changed to COMPLETED", jobId);
        } catch (Exception exception) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Job processing failed: " + exception.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.error("Job {} processing failed and status changed to FAILED", jobId, exception);
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Job processing failed: " + jobId, exception);
        }
    }

    private void process(Job job) throws InterruptedException {
        log.info("Processing job {} with payload: {}", job.getId(), job.getPayload());
        if ("FAIL".equalsIgnoreCase(job.getPayload())) {
            throw new RuntimeException("Simulated processing failure");
        }
        Thread.sleep(2000);
    }
}
