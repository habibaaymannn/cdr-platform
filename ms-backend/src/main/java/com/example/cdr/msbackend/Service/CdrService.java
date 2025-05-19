package com.example.cdr.msbackend.Service;

import com.example.cdr.msbackend.Model.Cdr;
import com.example.cdr.msbackend.Repository.CdrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CdrService {

    @Autowired
    private CdrRepository cdrRepository;

    public List<Cdr> getAllCDRs() {
        return cdrRepository.findAll();
    }

    public Optional<Cdr> getCDRById(Long id) {
        return cdrRepository.findById(id);
    }

    public Cdr createCDR(Cdr cdr) {
        return cdrRepository.save(cdr);
    }

//    public Cdr updateCDR(Long id, Cdr updatedCDR) {
//        return cdrRepository.findById(id)
//                .map(existingCDR -> {
//                    existingCDR.setSource(updatedCDR.getSource());
//                    existingCDR.setDestination(updatedCDR.getDestination());
//                    existingCDR.setStartTime(updatedCDR.getStartTime());
//                    return cdrRepository.save(existingCDR);
//                })
//                .orElseThrow(() -> new RuntimeException("CDR not found"));
//    }

    public void deleteCDR(Long id) {
        cdrRepository.deleteById(id);
    }
}

