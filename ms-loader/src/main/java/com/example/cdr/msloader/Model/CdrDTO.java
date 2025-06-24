package com.example.cdr.msloader.Model;

public class CdrDTO {
    private String source;
    private String destination;
    private String startTime;
    private String service;
    private Double usageAmount;


    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public Double getUsageAmount() { return usageAmount; }
    public void setUsageAmount(Double usageAmount) { this.usageAmount = usageAmount; }
}
