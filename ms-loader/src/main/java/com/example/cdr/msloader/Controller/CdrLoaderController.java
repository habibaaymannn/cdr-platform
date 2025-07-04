package com.example.cdr.msloader.Controller;
import com.example.cdr.msloader.Service.CdrLoaderService;
import com.example.cdr.msloader.Service.KafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class CdrLoaderController {
    private final CdrLoaderService cdrLoaderService;
    @Autowired
    public CdrLoaderController(CdrLoaderService cdrLoaderService) {
        this.cdrLoaderService = cdrLoaderService;
    }
}