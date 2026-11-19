package com.example.jobprocessor.worker;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

@Component
public class JobDlqWorker {

    private static final Logger log = LoggerFactory.getLogger(JobDlqWorker.class);

    @KafkaListener(
            topics = "job-topic-dlq",
            groupId = "job-dlq-worker-group"
    )
    public void processDeadLetter(ConsumerRecord<String, String> record) {
        log.info("Received DLQ message: {}", record.value());
        log.info(
                "DLQ job {} original topic={}, partition={}, offset={}",
                record.value(),
                headerValue(record, "kafka_dlt-original-topic"),
                numericHeaderValue(record, "kafka_dlt-original-partition"),
                numericHeaderValue(record, "kafka_dlt-original-offset")
        );
        log.info(
                "DLQ job {} exception type={}, message={}",
                record.value(),
                headerValue(record, "kafka_dlt-exception-fqcn"),
                headerValue(record, "kafka_dlt-exception-message")
        );
    }

    private String headerValue(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null
                ? "unavailable"
                : new String(header.value(), StandardCharsets.UTF_8);
    }

    private String numericHeaderValue(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        if (header == null) {
            return "unavailable";
        }

        byte[] value = header.value();
        if (value.length != Integer.BYTES && value.length != Long.BYTES) {
            return headerValue(record, name);
        }

        return value.length == Integer.BYTES
                ? Integer.toString(ByteBuffer.wrap(value).getInt())
                : Long.toString(ByteBuffer.wrap(value).getLong());
    }
}
