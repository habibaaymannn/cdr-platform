package com.example.cdr.msloader.Service;

import com.example.cdr.msloader.DTO.CdrDTO;
import com.example.cdr.msloader.Mapper.CdrMapper;
import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Repository.CdrLoaderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;
import static com.example.cdr.msloader.Model.ServiceTypeLoader.*;
import com.example.cdr.msloader.Processor.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Service
public class CdrLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(CdrLoaderService.class);

    private final KafkaProducer kafkaProducer;
    private final CdrLoaderRepository cdrLoaderRepository;
    private final ObjectMapper objectMapper;
    private final CdrMapper cdrMapper;

    @Value("${kafka.topic}")
    private String kafkaTopic;

    @Transactional
    public void saveCdr(CdrLoader cdr) {
        try {
            CdrLoader savedCdr = cdrLoaderRepository.save(cdr);
            cdrLoaderRepository.flush();
            logger.info("Saved CDR with ID: {}", savedCdr.getId());

            // Convert to DTO
            CdrDTO dto = cdrMapper.toCdrDTO(savedCdr);
            String cdrJson = objectMapper.writeValueAsString(dto);
            logger.debug("Serialized CDR to JSON: {}", cdrJson);

            kafkaProducer.sendCdr(kafkaTopic, cdrJson);
            logger.info("Sent CDR {} to Kafka topic cdr-records", savedCdr.getId());
        } catch (Exception e) {
            logger.error("Failed to save or send CDR to Kafka: {}", e.getMessage(), e);
        }
    }

//    public boolean isValidCdr(CdrLoader cdr) {
//        if (cdr.getSource() == null ) {
//            throw new IllegalArgumentException("Source is required");
//        }
//        if (cdr.getDestination() == null) {
//            throw new IllegalArgumentException("Destination is required");
//        }
//        if (cdr.getService() == DATA) {
//            if (!URL_PATTERN.matcher(cdr.getDestination()).matches()) {
//                throw new IllegalArgumentException("Destination must be a valid URL for DATA");
//            }
//        }
//        // Validate startTime
//        if (cdr.getStartTime() == null) {
//            throw new IllegalArgumentException("StartTime is required");
//        }
//        // Validate service
//        if (cdr.getService() == null) {
//            throw new IllegalArgumentException("Service is required");
//        }
//        // Validate usageAmount
//        if (cdr.getService() == SMS) {
//            cdr.setUsageAmount(1.0);
//        } else if (cdr.getUsageAmount() == null || cdr.getUsageAmount() <= 0) {
//            throw new IllegalArgumentException("UsageAmount must be positive for VOICE/DATA");
//        }
//        return true;
//    }

    public void moveToErrorDirectory(Path filePath) {
        try {
            Path errorDir = Paths.get("input/error");
            Files.createDirectories(errorDir);
            Path target = errorDir.resolve(filePath.getFileName());
            Files.move(filePath, target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Moved invalid file to {}", target);
        } catch (IOException e) {
            logger.error("Error moving file {} to error directory: {}", filePath, e.getMessage(), e);
        }
    }

}