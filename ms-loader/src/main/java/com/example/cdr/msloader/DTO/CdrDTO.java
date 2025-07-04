package com.example.cdr.msloader.DTO;

import lombok.Getter;
import lombok.Setter;

public class CdrDTO {
    private @Setter @Getter String source;
    private @Setter @Getter String destination;
    private @Setter @Getter String startTime;
    private @Setter @Getter String service;
    private @Setter @Getter Double usageAmount;

}
