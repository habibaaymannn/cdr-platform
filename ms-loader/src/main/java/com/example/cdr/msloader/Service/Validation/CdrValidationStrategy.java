package com.example.cdr.msloader.Service.Validation;

import com.example.cdr.msloader.Model.CdrLoader;
import com.example.cdr.msloader.Model.ServiceTypeLoader;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class CdrValidationStrategy {
    private final Map<ServiceTypeLoader, ServiceValidator> validators= new HashMap<>();
    public CdrValidationStrategy() {
        validators.put(ServiceTypeLoader.SMS,new SMSValidator());
        validators.put(ServiceTypeLoader.DATA,new DataValidator());
    }
    public boolean validateCdr(CdrLoader cdrLoader){
        if (cdrLoader.getSource() == null) {
            throw new IllegalArgumentException("Source must be a valid 10-digit phone number");
        }
        if (cdrLoader.getDestination() == null) {
            throw new IllegalArgumentException("Destination is required");
        }
        // Validate startTime
        if (cdrLoader.getStartTime() == null) {
            throw new IllegalArgumentException("StartTime is required");
        }
        // Validate service
        if (cdrLoader.getService() == null) {
            throw new IllegalArgumentException("Service is required");
        }
        ServiceValidator validator = validators.get(cdrLoader.getService());
        if (validator != null) {
            validator.validate(cdrLoader); // Calls the specific validation logic
        } else {
            throw new IllegalArgumentException("No validator found for service: " + cdrLoader.getService());
        }
        return true;
    }
}
