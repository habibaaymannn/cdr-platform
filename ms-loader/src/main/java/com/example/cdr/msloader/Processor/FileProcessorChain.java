package com.example.cdr.msloader.Processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FileProcessorChain {
    private final FileProcessor chain;

    @Autowired
    public FileProcessorChain(FileProcessor csvProcessor, FileProcessor jsonProcessor, FileProcessor yamlProcessor, FileProcessor xmlProcessor) {
        csvProcessor.setNext(jsonProcessor);
        jsonProcessor.setNext(yamlProcessor);
        yamlProcessor.setNext(xmlProcessor);

        this.chain = csvProcessor; // Start of the chain
    }
    public void process(Path file){
        chain.handle(file);
    }
}