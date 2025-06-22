package com.example.cdr.msbackend.Controller;

import com.example.cdr.msbackend.Model.Cdr;
import com.example.cdr.msbackend.Repository.CdrRepository;
import com.example.cdr.msbackend.Service.CdrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.regex.Pattern;

import static com.example.cdr.msbackend.Model.ServiceType.*;

@RestController
@RequestMapping("/cdrs")
public class CdrController {
    private CdrRepository repository;
    private final CdrService service;

    @Autowired
    public CdrController(CdrRepository repository,CdrService service) {
        this.service = service;
        this.repository = repository;
    }

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://).+");

    // Create a CDR
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('cdr-write')")
    public Cdr create(@RequestBody Cdr cdr) {
        validateCdr(cdr);
        return service.createCDR(cdr);
    }

    // Read all CDRs
    @GetMapping
    @PreAuthorize("hasRole('cdr-read')")
    public List<Cdr> getAll() {
        return service.getAllCDRs();
    }

    // Read a single CDR by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('cdr-read')")
    public ResponseEntity<Cdr> getById(@PathVariable Long id) {
        return service.getCDRById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("CDR not found with id: " + id));
    }

    // Update a CDR
    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('cdr-write')")
    public ResponseEntity<Cdr> update(@PathVariable Long id, @RequestBody Cdr updatedCdr) {
        return service.getCDRById(id)
                .map(existingCdr -> {
                    validateCdr(updatedCdr);
                    existingCdr.setSource(updatedCdr.getSource());
                    existingCdr.setDestination(updatedCdr.getDestination());
                    existingCdr.setStartTime(updatedCdr.getStartTime());
                    existingCdr.setService(updatedCdr.getService());
                    existingCdr.setUsageAmount(updatedCdr.getUsageAmount());
                    System.out.println("Updating CDR: " + existingCdr);
                    return ResponseEntity.ok(repository.save(existingCdr));
                })
                .orElseThrow(() -> new EntityNotFoundException("CDR not found with id: " + id));
    }

    // Delete a CDR
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('cdr-write')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("CDR not found with id: " + id);
        }
        service.deleteCDR(id);
        System.out.println("Deleted CDR with id: " + id);
        return ResponseEntity.noContent().build();
    }

    private void validateCdr(Cdr cdr) {
        // Validate source
        if (cdr.getSource() == null || !PHONE_NUMBER_PATTERN.matcher(cdr.getSource()).matches()) {
            throw new IllegalArgumentException("Source must be a valid 10-digit phone number");
        }

        // Validate destination
        if (cdr.getDestination() == null) {
            throw new IllegalArgumentException("Destination is required");
        }
        if (cdr.getService() == VOICE || cdr.getService() == SMS) {
            if (!PHONE_NUMBER_PATTERN.matcher(cdr.getDestination()).matches()) {
                throw new IllegalArgumentException("Destination must be a valid 10-digit phone number for VOICE/SMS");
            }
        } else if (cdr.getService() == DATA) {
            if (!URL_PATTERN.matcher(cdr.getDestination()).matches()) {
                throw new IllegalArgumentException("Destination must be a valid URL for DATA");
            }
        }

        // Validate startTime
        if (cdr.getStartTime() == null) {
            throw new IllegalArgumentException("StartTime is required");
        }

        // Validate service
        if (cdr.getService() == null) {
            throw new IllegalArgumentException("Service is required");
        }

        // Validate usageAmount
        if (cdr.getService() == SMS) {
            cdr.setUsageAmount(1.0);
        } else if (cdr.getUsageAmount() == null || cdr.getUsageAmount() <= 0) {
            throw new IllegalArgumentException("UsageAmount must be positive for VOICE/DATA");
        }
    }
    @GetMapping("/cdrs/test")
    public ResponseEntity<?> testAuthentication(Authentication authentication) {
        System.out.println("Authenticated user: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        return ResponseEntity.ok("Authenticated");
    }

}
