package com.example.cdr.msloader.Processor;

import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Model.CdrWrapper;
import com.example.cdr.msloader.Service.CdrLoaderService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@Component
public class XmlProcessor extends FileProcessor{
    private static final Logger logger = LoggerFactory.getLogger(XmlProcessor.class);
    private final XmlMapper xmlMapper;
    public XmlProcessor(@Lazy CdrLoaderService service, XmlMapper xmlMapper) {
        super(service);
        this.xmlMapper = xmlMapper;
    }


    @Override
    protected boolean canHandle(Path file) {
        return file.getFileName().toString().endsWith(".xml");
    }

    @Override
    protected List<CdrLoader> extractCdrs(Path file) throws IOException {
        logger.info("Processing XML file: {}", file);
        String xmlContent = Files.readString(file);
        try {
            CdrWrapper cdrList = xmlMapper.readValue(xmlContent, CdrWrapper.class);
            return cdrList.getCdrs();
        } catch (Exception e) {
            logger.warn("Wrapper failed, trying direct list: {}", e.getMessage());
            try {
                return Arrays.asList(xmlMapper.readValue(xmlContent, CdrLoader[].class));
            } catch (Exception inner) {
                logger.error("Both XML deserialization attempts failed: {}", inner.getMessage(), inner);
                service.moveToErrorDirectory(file);
                return Collections.emptyList();
            }
        }

    }
}
