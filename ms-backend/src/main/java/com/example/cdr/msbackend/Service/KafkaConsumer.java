package com.example.cdr.msbackend.Service;

import com.example.cdr.msbackend.Model.Cdr;
import com.example.cdr.msbackend.Repository.CdrRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private final CdrRepository cdrRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumer(CdrRepository cdrRepository, ObjectMapper objectMapper) {
        this.cdrRepository = cdrRepository;
        this.objectMapper = objectMapper;
        logger.info("✅Using ObjectMapper: {}", objectMapper.getClass());

    }

    @KafkaListener(topics = "cdr-records", groupId = "cdr-consumer-group")
    public void consume(String message) {
        logger.info("Received message from cdr-records: {}", message);
        try {
            Cdr cdr = objectMapper.readValue(message, Cdr.class);
            logger.debug("Deserialized CDR: {}", cdr);
            cdrRepository.save(cdr);
            logger.info("CDR saved successfully to MySQL: {}", cdr);
        } catch (Exception e) {
            logger.error("Error processing CDR message: {}", e.getMessage(), e);
        }
    }
}