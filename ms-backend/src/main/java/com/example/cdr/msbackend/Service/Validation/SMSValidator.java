package com.example.cdr.msbackend.Service.Validation;

import com.example.cdr.msbackend.Model.Cdr;
import org.springframework.stereotype.Component;

@Component
public class SMSValidator implements ServiceValidator {
    @Override
    public void validate(Cdr cdr) {
        cdr.setUsageAmount(1.0);
        if (cdr.getUsageAmount() == null || cdr.getUsageAmount() <= 0) {
            throw new IllegalArgumentException("UsageAmount must be positive for VOICE/DATA");
        }
    }
}
