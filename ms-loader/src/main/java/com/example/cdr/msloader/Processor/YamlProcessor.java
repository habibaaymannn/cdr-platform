package com.example.cdr.msloader.Processor;

import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Model.CdrWrapper;
import com.example.cdr.msloader.Service.CdrLoaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
@Component
public class YamlProcessor extends FileProcessor{
    private static final Logger logger = LoggerFactory.getLogger(YamlProcessor.class);

    private final YAMLMapper yamlMapper;

    public YamlProcessor(@Lazy CdrLoaderService service, YAMLMapper yamlMapper) {
        super(service);
        this.yamlMapper = yamlMapper;
    }

    @Override
    protected boolean canHandle(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
    }

    @Override
    protected List<CdrLoader> extractCdrs(Path file) throws IOException {
        logger.info("Processing YAML file: {}", file);
            String yamlContent = Files.readString(file);

            List<CdrLoader> cdrs;
            try {
                CdrWrapper wrapper = yamlMapper.readValue(yamlContent, CdrWrapper.class);
                cdrs = wrapper.getCdrs();
                logger.info("Deserialized {} CDRs from YAML file {} (wrapper)", cdrs.size(), file);
            } catch (Exception e1) {
                logger.info("Failed to parse YAML as wrapper: {}", e1.getMessage());
                // list parsing
                cdrs = Arrays.asList(yamlMapper.readValue(yamlContent, CdrLoader[].class));
                logger.info("failed as list");
                logger.info("Deserialized {} CDRs from YAML file {} (list)", cdrs.size(), file);
            }
        return cdrs;
    }
}
