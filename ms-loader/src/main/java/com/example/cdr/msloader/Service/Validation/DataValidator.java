package com.example.cdr.msloader.Service.Validation;

import com.example.cdr.msloader.Model.CdrLoader;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
@Component
public class DataValidator implements ServiceValidator {
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://).+");

    @Override
    public void validate(CdrLoader cdrLoader) {
        if (!URL_PATTERN.matcher(cdrLoader.getDestination()).matches()) {
            throw new IllegalArgumentException("Destination must be a valid URL for DATA");
        }
    }
}
