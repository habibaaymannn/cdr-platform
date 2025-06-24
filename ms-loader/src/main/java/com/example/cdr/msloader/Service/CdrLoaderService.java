package com.example.cdr.msloader.Service;

import com.example.cdr.msloader.Model.CdrDTO;
import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Model.CdrWrapper;
import com.example.cdr.msloader.Model.ServiceTypeLoader;
import com.example.cdr.msloader.Repository.CdrLoaderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class CdrLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(CdrLoaderService.class);
    @Value("${loader.directory.path}")
    private String directoryPath;
    private final KafkaProducer kafkaProducer;
    private final CdrLoaderRepository cdrLoaderRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CdrLoaderService(KafkaProducer kafkaProducer, CdrLoaderRepository cdrLoaderRepository, ObjectMapper objectMapper) {
        this.kafkaProducer = kafkaProducer;
        this.cdrLoaderRepository = cdrLoaderRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRateString = "${loader.schedule.rate:60000}")
    @Transactional
    public void loadFiles() {
        logger.info("Scanning directory: {}", directoryPath);
        try {
            Files.list(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        logger.info("Found file: {}", file);
                        processFile(file);
                    });
        } catch (IOException e) {
            logger.error("Error scanning directory {}: {}", directoryPath, e.getMessage(), e);
        }
    }

    @Transactional
    public void processFile(Path filePath) {
        logger.info("Processing file: {}", filePath);
        String fileName = filePath.getFileName().toString().toLowerCase();
        boolean isMoved = false;

        try {
            if (fileName.endsWith(".csv")) {
                processCsvFile(filePath);
            } else if (fileName.endsWith(".json")) {
                processJsonFile(filePath);
            } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                processYamlFile(filePath);
            } else if (fileName.endsWith(".xml")) {
                processXmlFile(filePath);
            } else {
                logger.warn("Skipping unsupported file: {}", filePath);
                moveToErrorDirectory(filePath);
                isMoved = true;
            }
        } catch (Exception e) {
            logger.error("Error processing file {}: {}", filePath, e.getMessage(), e);
            moveToErrorDirectory(filePath);
            isMoved = true;
        }

        if (!isMoved) {
            try {
                Files.deleteIfExists(filePath);
                logger.info("Deleted processed file: {}", filePath);
            } catch (IOException e) {
                logger.error("Failed to delete processed file {}: {}", filePath, e.getMessage(), e);
            }
        }
    }

    @Transactional
    protected void processCsvFile(Path filePath) {
        logger.info("Processing CSV file: {}", filePath);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        try (CSVReader reader = new CSVReader(new FileReader(filePath.toFile()))) {
            String[] line;
            reader.readNext(); // Skip header

            while ((line = reader.readNext()) != null) {
                if (line.length < 5) {
                    logger.warn("Skipping invalid CSV line: {}", Arrays.toString(line));
                    continue;
                }

                try {
                    CdrLoader cdr = new CdrLoader();
                    cdr.setSource(line[0].trim());
                    cdr.setDestination(line[1].trim());
                    cdr.setStartTime(LocalDateTime.parse(line[2].trim(), formatter));
                    cdr.setService(ServiceTypeLoader.valueOf(line[3].trim()));
                    cdr.setUsageAmount(Double.parseDouble(line[4].trim()));

                    if (isValidCdr(cdr)) {
                        saveCdr(cdr);
                    } else {
                        logger.warn("Invalid CDR from CSV: {}", cdr);
                    }
                } catch (Exception e) {
                    logger.error("Error processing CSV line {}: {}", Arrays.toString(line), e.getMessage(), e);
                }
            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Error reading CSV file {}: {}", filePath, e.getMessage(), e);
            moveToErrorDirectory(filePath);
        }
    }

    @Transactional
    public void processJsonFile(Path filePath) {
        logger.info("Processing JSON file: {}", filePath);
        try {
            String jsonContent = Files.readString(filePath);
            logger.debug("JSON content: {}", jsonContent);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "[yyyy-MM-dd'T'HH:mm:ss][yyyy-MM-dd HH:mm:ss][dd-MM-yyyy HH:mm:ss]"
            );
            JavaTimeModule module = new JavaTimeModule();
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter) {
                @Override
                public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser p,
                                                 com.fasterxml.jackson.databind.DeserializationContext ctxt)
                        throws IOException {
                    String dateStr = p.getText();
                    try {
                        if (dateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
                            return LocalDateTime.parse(dateStr + " 00:00:00",
                                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                        }
                        return super.deserialize(p, ctxt);
                    } catch (Exception e) {
                        logger.error("Failed to parse date {} in file {}", dateStr, filePath, e);
                        throw e;
                    }
                }
            });

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(module);
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<CdrLoader> cdrs;
            try {
                cdrs = mapper.readValue(jsonContent, mapper.getTypeFactory().constructCollectionType(List.class, CdrLoader.class));
                logger.info("Deserialized {} CDRs as list from JSON file {}", cdrs.size(), filePath);
            } catch (Exception e) {
                logger.info("Failed to parse JSON as list: {}", e.getMessage());
                try {
                    CdrLoader cdr = mapper.readValue(jsonContent, CdrLoader.class);
                    cdrs = Arrays.asList(cdr);
                    logger.info("Deserialized single CDR: {}", cdr);
                } catch (Exception singleEx) {
                    logger.error("Failed to parse JSON as single CDR in file {}: {}", filePath, singleEx.getMessage(), singleEx);
                    throw singleEx;
                }
            }

            if (cdrs == null || cdrs.isEmpty()) {
                logger.warn("No valid CDRs extracted from JSON file {}", filePath);
                return;
            }

            for (CdrLoader cdr : cdrs) {
                if (isValidCdr(cdr)) {
                    try {
                        saveCdr(cdr);
                    } catch (DataIntegrityViolationException e) {
                        logger.error("Constraint violation for CDR in file {}: {}", filePath, e.getMessage(), e);
                    } catch (Exception e) {
                        logger.error("Error saving CDR in file {}: {}", filePath, e.getMessage(), e);
                    }
                } else {
                    logger.warn("Skipping invalid CDR: {}", cdr);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing JSON file {}: {}", filePath, e.getMessage(), e);
            moveToErrorDirectory(filePath);
        }
    }

    @Transactional
    protected void processYamlFile(Path filePath) {
        logger.info("Processing YAML file: {}", filePath);
        try {
            String yamlContent = Files.readString(filePath);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            JavaTimeModule module = new JavaTimeModule();
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.registerModule(module);
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<CdrLoader> cdrs;
            try {
                // Try parsing as a single CdrLoader (no wrapper)
                CdrLoader cdr = mapper.readValue(yamlContent, CdrLoader.class);
                cdrs = Arrays.asList(cdr);
                logger.info("Deserialized single CDR: {}", cdr);
            } catch (Exception e1) {
                logger.info("Failed to parse YAML as single CDR: {}", e1.getMessage());
                try {

                    CdrWrapper wrapper = mapper.readValue(yamlContent, CdrWrapper.class);
                    cdrs = wrapper.getCdrs();
                    logger.info("Deserialized {} CDRs from YAML file {} (wrapper)", cdrs.size(), filePath);
                } catch (Exception e2) {
                    logger.info("Failed to parse YAML as wrapper: {}", e2.getMessage());
                    // Fallback to list parsing
                    cdrs = Arrays.asList(mapper.readValue(yamlContent, CdrLoader[].class));
                    logger.info("Deserialized {} CDRs from YAML file {} (list)", cdrs.size(), filePath);
                }
            }

            if (cdrs == null || cdrs.isEmpty()) {
                logger.warn("No valid CDRs extracted from YAML file {}", filePath);
                moveToErrorDirectory(filePath);
                return;
            }

            for (CdrLoader cdr : cdrs) {
                if (isValidCdr(cdr)) {
                    try {
                        saveCdr(cdr);
                    } catch (DataIntegrityViolationException e) {
                        logger.error("Constraint violation for CDR in YAML file {}: {}", filePath, e.getMessage(), e);
                    } catch (Exception e) {
                        logger.error("Error saving CDR in YAML file {}: {}", filePath, e.getMessage(), e);
                    }
                } else {
                    logger.warn("Skipping invalid CDR: {}", cdr);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing YAML file {}: {}", filePath, e.getMessage(), e);
            moveToErrorDirectory(filePath);
        }
    }

    @Transactional
    protected void processXmlFile(Path filePath) {
        logger.info("Processing XML file: {}", filePath);
        try {
            String xmlContent = Files.readString(filePath);
            XmlMapper xmlMapper = new XmlMapper();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            JavaTimeModule module = new JavaTimeModule();
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            xmlMapper.registerModule(module);

            CdrWrapper cdrList = xmlMapper.readValue(xmlContent, CdrWrapper.class);
            logger.info("Deserialized {} CDRs from XML file {}", cdrList.getCdrs().size(), filePath);

            for (CdrLoader cdr : cdrList.getCdrs()) {
                if (isValidCdr(cdr)) {
                    try {
                        saveCdr(cdr);
                    } catch (DataIntegrityViolationException e) {
                        logger.error("Constraint violation for CDR in XML file {}: {}", filePath, e.getMessage(), e);
                    } catch (Exception e) {
                        logger.error("Error saving CDR in XML file {}: {}", filePath, e.getMessage(), e);
                    }
                } else {
                    logger.warn("Skipping invalid CDR: {}", cdr);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing XML file {}: {}", filePath, e.getMessage(), e);
            moveToErrorDirectory(filePath);
        }
    }

    @Transactional
    public void saveCdr(CdrLoader cdr) {
        logger.debug("Attempting to save CDR: {}", cdr);
        try {
            CdrLoader savedCdr = cdrLoaderRepository.save(cdr);
            cdrLoaderRepository.flush();
            logger.info("Saved CDR with ID: {}", savedCdr.getId());

            // Convert to DTO
            CdrDTO dto = toCdrDTO(savedCdr);
            String cdrJson = objectMapper.writeValueAsString(dto);
            logger.debug("Serialized CDR to JSON: {}", cdrJson);

            kafkaProducer.sendCdr("cdr-records", cdrJson);
            logger.info("Sent CDR {} to Kafka topic cdr-records", savedCdr.getId());
        } catch (Exception e) {
            logger.error("Failed to save or send CDR to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Kafka or database error", e);
        }
    }

    private boolean isValidCdr(CdrLoader cdr) {
        if (cdr == null) {
            logger.warn("CDR is null");
            return false;
        }
        if (cdr.getSource() == null || cdr.getDestination() == null || cdr.getStartTime() == null ||
                cdr.getService() == null || cdr.getUsageAmount() == null) {
            logger.warn("CDR missing required fields: source={}, destination={}, startTime={}, service={}, usageAmount={}",
                    cdr.getSource(), cdr.getDestination(), cdr.getStartTime(), cdr.getService(), cdr.getUsageAmount());
            return false;
        }
        return true;
    }

    private void moveToErrorDirectory(Path filePath) {
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
    private CdrDTO toCdrDTO(CdrLoader cdr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        CdrDTO dto = new CdrDTO();
        dto.setSource(cdr.getSource());
        dto.setDestination(cdr.getDestination());
        dto.setStartTime(cdr.getStartTime().format(formatter)); // Convert LocalDateTime to string
        dto.setService(cdr.getService().name()); // Enum to string
        dto.setUsageAmount(cdr.getUsageAmount());
        return dto;
    }

}