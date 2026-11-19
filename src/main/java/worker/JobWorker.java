package worker;

import com.example.jobprocessor.entity.Job;
import com.example.jobprocessor.entity.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.example.jobprocessor.repository.JobRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobWorker {
    private final JobRepository jobRepository;

    @KafkaListener(
            topics = "job-processing",
            groupId = "job-worker-group"
    )
    public void processJob(String jobId) {

        log.info("Received job: {}", jobId);

        UUID id = UUID.fromString(jobId);

        Job job = jobRepository.findById(id)
                .orElse(null);

        if (job == null) {

            log.error("Job not found: {}", jobId);

            return;
        }

        try {

            job.setStatus(JobStatus.RUNNING);
            job.setStartedAt(LocalDateTime.now());

            jobRepository.save(job);

            log.info(
                    "Processing job {} - {}",
                    job.getId(),
                    job.getName()
            );

            executeJob(job);

            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

            jobRepository.save(job);

            log.info(
                    "Job completed successfully: {}",
                    job.getId()
            );

        } catch (Exception exception) {

            log.error(
                    "Job failed: {}",
                    job.getId(),
                    exception
            );

            handleFailure(job, exception);
        }
    }

    private void executeJob(Job job) throws InterruptedException {

        // Actual business logic yahan hoga.

        log.info(
                "Executing payload: {}",
                job.getPayload()
        );

        Thread.sleep(3000);

        // Testing ke liye:
        // throw new RuntimeException("Something went wrong");
    }

    private void handleFailure(
            Job job,
            Exception exception
    ) {

        job.setRetryCount(
                job.getRetryCount() + 1
        );

        job.setErrorMessage(
                exception.getMessage()
        );

        if (job.getRetryCount() < job.getMaxRetries()) {

            job.setStatus(JobStatus.QUEUED);

            jobRepository.save(job);

            log.info(
                    "Job {} will be retried. Attempt {}/{}",
                    job.getId(),
                    job.getRetryCount(),
                    job.getMaxRetries()
            );

        } else {

            job.setStatus(JobStatus.FAILED);

            job.setCompletedAt(
                    LocalDateTime.now()
            );

            jobRepository.save(job);

            log.error(
                    "Job permanently failed: {}",
                    job.getId()
            );
        }
    }

}
