package com.example.cdr.msloader.Processor;

import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Service.CdrLoaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mysql.cj.conf.PropertyKey.logger;
@Component
public class JsonProcessor extends FileProcessor{
    private static final Logger logger = LoggerFactory.getLogger(JsonProcessor.class);
    private final ObjectMapper objectMapper;
    public JsonProcessor(@Lazy CdrLoaderService service, ObjectMapper objectMapper) {
        super(service);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean canHandle(Path file) {
        return file.getFileName().toString().endsWith(".json");
    }

    @Override
    protected List<CdrLoader> extractCdrs(Path file) {
        logger.info("Processing JSON file: {}", file);
        List<CdrLoader> cdrs = new ArrayList<>();
        try {
            String jsonContent = Files.readString(file);
            logger.debug("JSON content: {}", jsonContent);

            try {
                cdrs = objectMapper.readValue(jsonContent, objectMapper.getTypeFactory().constructCollectionType(List.class, CdrLoader.class));
                logger.info("Deserialized {} CDRs as list from JSON file {}", cdrs.size(), file);
            } catch (Exception e) {
                logger.info("Failed to parse JSON as list: {}", e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            logger.error("Error processing JSON file {}: {}", file, e.getMessage(), e);
            service.moveToErrorDirectory(file);
        }
        return cdrs;
    }
}
