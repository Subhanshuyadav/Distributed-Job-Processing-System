package com.example.jobprocessor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    public static final String JOB_TOPIC = "job-topic";
    public static final String JOB_DLQ_TOPIC = "job-topic-dlq";

    @Bean
    public NewTopic jobTopic() {
        return new NewTopic(JOB_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic jobDlqTopic() {
        return new NewTopic(JOB_DLQ_TOPIC, 3, (short) 1);
    }
}
