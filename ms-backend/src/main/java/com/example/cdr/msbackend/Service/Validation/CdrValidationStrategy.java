package com.example.cdr.msbackend.Service.Validation;

import com.example.cdr.msbackend.Model.Cdr;
import com.example.cdr.msbackend.Model.ServiceType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CdrValidationStrategy {
    private final Map<ServiceType, ServiceValidator>validators= new HashMap<>();
    public CdrValidationStrategy() {
        validators.put(ServiceType.SMS,new SMSValidator());
        validators.put(ServiceType.DATA,new DataValidator());
    }
    public void validateCdr(Cdr cdr){
        if (cdr.getSource() == null) {
            throw new IllegalArgumentException("Source must be a valid 10-digit phone number");
        }
        if (cdr.getDestination() == null) {
            throw new IllegalArgumentException("Destination is required");
        }
        // Validate startTime
        if (cdr.getStartTime() == null) {
            throw new IllegalArgumentException("StartTime is required");
        }
        // Validate service
        if (cdr.getService() == null) {
            throw new IllegalArgumentException("Service is required");
        }
        ServiceValidator validator = validators.get(cdr.getService());
        if (validator != null) {
            validator.validate(cdr); // Calls the specific validation logic
        } else {
            throw new IllegalArgumentException("No validator found for service: " + cdr.getService());
        }
    }
}
