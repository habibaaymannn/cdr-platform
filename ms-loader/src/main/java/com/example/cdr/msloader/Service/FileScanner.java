package com.example.cdr.msloader.Service;

import com.example.cdr.msloader.Processor.FileProcessorChain;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Service
public class FileScanner {
    private static final Logger logger = LoggerFactory.getLogger(FileScanner.class);
    @Value("${loader.directory.path}")
    private String directoryPath;
    private final @Lazy CdrLoaderService service;
    private final FileProcessorChain processorChain;


    @Scheduled(fixedRateString = "${loader.schedule.rate:60000}")
    public void scanFiles() {
        try {
            Files.list(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        logger.info("Found file: {}", file);
                        try {
                            processorChain.process(file);
                            Files.deleteIfExists(file);
                            logger.info("Deleted processed file: {}", file);
                        } catch (Exception e) {
                            service.moveToErrorDirectory(file);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error scanning directory {}: {}", directoryPath, e.getMessage(), e);
        }
    }
}
