package com.example.cdr.msbackend.Service.Validation;

import com.example.cdr.msbackend.Model.Cdr;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DataValidator implements ServiceValidator {
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://).+");

    @Override
    public void validate(Cdr cdr) {
        if (!URL_PATTERN.matcher(cdr.getDestination()).matches()) {
            throw new IllegalArgumentException("Destination must be a valid URL for DATA");
        }
    }
}
