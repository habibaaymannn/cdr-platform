package com.example.cdr.msbackend.Service;

import com.example.cdr.msbackend.DTO.CdrDTO;
import com.example.cdr.msbackend.Mapper.CdrMapper;
import com.example.cdr.msbackend.Model.Cdr;
import com.example.cdr.msbackend.Repository.CdrRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.regex.Pattern;

import static com.example.cdr.msbackend.Model.ServiceType.*;


@Service
public class CdrService {

    @Autowired
    private CdrRepository cdrRepository;
    private CdrMapper cdrMapper;

    public CdrService(CdrMapper cdrMapper) {
        this.cdrMapper = cdrMapper;
    }

    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://).+");


    public List<Cdr> getAllCDRs() {
        return cdrRepository.findAll();
    }

    public ResponseEntity<Cdr> getCDRById(Long id) {

        return cdrRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("CDR not found with id: " + id));
    }

    public Cdr createCDR(Cdr cdr) {
        return cdrRepository.save(cdr);
    }
    public Cdr validateAndCreate(CdrDTO dto) {
        Cdr cdr = cdrMapper.toCdr(dto);
        validateCdr(cdr);
        return createCDR(cdr);
    }

    public ResponseEntity<Void> deleteCDR(Long id) {
        if (!cdrRepository.existsById(id)) {
            throw new EntityNotFoundException("CDR not found with id: " + id);
        }
        cdrRepository.deleteById(id);
        System.out.println("Deleted CDR with id: " + id);
        return ResponseEntity.noContent().build();
    }
    public void validateCdr(Cdr cdr) {

        if (cdr.getSource() == null) {
            throw new IllegalArgumentException("Source must be a valid 10-digit phone number");
        }
        if (cdr.getDestination() == null) {
            throw new IllegalArgumentException("Destination is required");
        }
        if (cdr.getService() == DATA) {
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

}

