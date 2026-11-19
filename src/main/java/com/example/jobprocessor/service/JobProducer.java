package com.example.jobprocessor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobProducer {

    private static final Logger log = LoggerFactory.getLogger(JobProducer.class);
    private static final String JOB_TOPIC = "job-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public JobProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishJob(UUID jobId) {
        String message = jobId.toString();

        kafkaTemplate.send(JOB_TOPIC, message)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish job {} to Kafka topic {}", jobId, JOB_TOPIC, exception);
                        return;
                    }

                    log.info("Published job {} to Kafka topic {}", jobId, JOB_TOPIC);
                });
    }
}
