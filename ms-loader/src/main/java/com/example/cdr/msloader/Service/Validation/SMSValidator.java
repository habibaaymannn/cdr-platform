package com.example.cdr.msloader.Service.Validation;

import com.example.cdr.msloader.Model.CdrLoader;
import org.springframework.stereotype.Component;

@Component
public class SMSValidator implements ServiceValidator {
    @Override
    public void validate(CdrLoader cdrLoader) {
        cdrLoader.setUsageAmount(1.0);
        if (cdrLoader.getUsageAmount() == null || cdrLoader.getUsageAmount() <= 0) {
            throw new IllegalArgumentException("UsageAmount must be positive for VOICE/DATA");
        }
    }
}
