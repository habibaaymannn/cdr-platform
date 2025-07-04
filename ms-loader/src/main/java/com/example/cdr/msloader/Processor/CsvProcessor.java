package com.example.cdr.msloader.Processor;

import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Model.ServiceTypeLoader;
import com.example.cdr.msloader.Service.CdrLoaderService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mysql.cj.conf.PropertyKey.logger;

@Component
public class CsvProcessor extends FileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CsvProcessor.class);

    @Autowired
    public CsvProcessor(@Lazy CdrLoaderService service) {
        super(service);
    }


    @Override
    protected boolean canHandle(Path file) {
        return file.getFileName().toString().toLowerCase().endsWith(".csv");
    }

    @Override
    protected List<CdrLoader> extractCdrs(Path file) {
        List<CdrLoader> cdrs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        try (CSVReader reader = new CSVReader(Files.newBufferedReader(Paths.get(file.toUri())))) {
            String[] line;
            reader.readNext(); // skip header

            while ((line = reader.readNext()) != null) {
                try {
                    CdrLoader cdr = new CdrLoader();
                    cdr.setSource(line[0].trim());
                    cdr.setDestination(line[1].trim());
                    cdr.setStartTime(LocalDateTime.parse(line[2].trim(), formatter));
                    cdr.setService(ServiceTypeLoader.valueOf(line[3].trim()));
                    cdr.setUsageAmount(Double.parseDouble(line[4].trim()));

                    cdrs.add(cdr);
                } catch (Exception e) {
                    logger.warn("Skipping invalid CSV line due to parse error: {}", Arrays.toString(line), e);
                }
            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Error reading CSV file {}: {}", file, e.getMessage(), e);
            service.moveToErrorDirectory(file);
        }
        return cdrs;
    }
}
