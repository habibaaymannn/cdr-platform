package com.example.cdr.msbackend.Controller;

import com.example.cdr.msbackend.Model.Cdr;
import com.example.cdr.msbackend.DTO.CdrDTO;
import com.example.cdr.msbackend.Model.ServiceType;
import com.example.cdr.msbackend.Repository.CdrRepository;
import com.example.cdr.msbackend.Service.CdrService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import static com.example.cdr.msbackend.Model.ServiceType.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/cdrs")
public class CdrController {
    private final CdrService service;


    @PostMapping
    @Transactional
    @PreAuthorize("hasAuthority('cdr-write')")
    public Cdr create(@RequestBody CdrDTO dto) {
        return service.validateAndCreate(dto);
    }


    // get all CDRs
    @GetMapping
    @PreAuthorize("hasAuthority('cdr-read')")
    public List<Cdr> getAll() {
        return service.getAllCDRs();
    }

    // Read CDR by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cdr-read')")
    public ResponseEntity<Cdr> getById(@PathVariable Long id) {
        return service.getCDRById(id);
    }

    // Delete a CDR
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('cdr-write')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.deleteCDR(id);
    }
}
