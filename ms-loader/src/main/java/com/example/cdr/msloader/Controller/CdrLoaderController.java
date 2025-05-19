package com.example.cdr.msloader.Controller;

import com.example.cdr.msloader.Service.CdrLoaderService;
import com.example.cdr.msloader.Service.KafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class CdrLoaderController {
    private final KafkaProducer kafkaProducer;
    private static final Logger logger = LoggerFactory.getLogger(CdrLoaderController.class);
    private final CdrLoaderService cdrLoaderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public CdrLoaderController(KafkaProducer kafkaProducer, CdrLoaderService cdrLoaderService, ObjectMapper objectMapper) {
        this.kafkaProducer = kafkaProducer;
        this.cdrLoaderService = cdrLoaderService;
    }

    @GetMapping("/load-cdr")
    public ResponseEntity<String> loadCdr(@RequestParam("filePath") String filePath) {
        logger.info("Received request to load CDR from file: {}", filePath);
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                logger.error("File does not exist or is not a regular file: {}", filePath);
                return ResponseEntity.badRequest().body("File does not exist: " + filePath);
            }
            cdrLoaderService.processFile(path);
            return ResponseEntity.ok("File processed successfully: " + filePath);
        } catch (Exception e) {
            logger.error("Error processing file {}: {}", filePath, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/send")
    public String sendCdr(@RequestParam("message") String message) {
        try {
            String decodedMessage = UriUtils.decode(message, StandardCharsets.UTF_8);
            logger.info("Received message for /send: {}", decodedMessage);
            objectMapper.readTree(decodedMessage);
            kafkaProducer.sendCdr("cdr-records", decodedMessage);
            return "Sent message: " + decodedMessage;
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
        }
    }
}