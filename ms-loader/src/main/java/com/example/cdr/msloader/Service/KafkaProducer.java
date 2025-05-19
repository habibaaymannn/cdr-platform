package com.example.cdr.msloader.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCdr(String topic, String message) {
        logger.info("Sending message to topic {}: {}", topic, message);
        try {
            kafkaTemplate.send(topic, message).get(10, TimeUnit.SECONDS); // Synchronous send with timeout
            logger.info("Message sent successfully to topic {}: {}", topic, message);
        } catch (Exception ex) {
            logger.error("Failed to send message to topic {}: {}", topic, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send to Kafka", ex);
        }
    }
}
