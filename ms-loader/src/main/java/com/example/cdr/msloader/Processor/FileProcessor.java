package com.example.cdr.msloader.Processor;

import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Service.CdrLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public abstract class FileProcessor {
    protected final CdrLoaderService service;
    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    @Setter
    private FileProcessor next;


    protected abstract boolean canHandle(Path file);
    protected abstract List<CdrLoader> extractCdrs(Path file) throws IOException;

    public void handle(Path file) {
        if(canHandle(file))
            process(file);
        else if(next != null)
            next.handle(file);
    }
    protected void process(Path file) {
        try {
            List<CdrLoader> cdrs = extractCdrs(file);

            if (cdrs == null || cdrs.isEmpty()) {
                service.moveToErrorDirectory(file);
                return;
            }

            for (CdrLoader cdr : cdrs) {
                if (service.isValidCdr(cdr)) {
                    try {
                        service.saveCdr(cdr);
                    } catch (DataIntegrityViolationException e) {
                        logger.error("Constraint violation for CDR in file {}: {}", file, e.getMessage(), e);
                    }
                } else {
                    logger.warn("Skipping invalid CDR: {}", cdr);
                }
            }

        } catch (Exception e) {
            service.moveToErrorDirectory(file);
        }
    }


}
