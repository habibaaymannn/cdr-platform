package com.example.cdr.msbackend.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cdr", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"source"})
})
public class Cdr {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)

    private String source;

    @Column(nullable = false)
    private String destination;


    @Column(nullable = false)
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType service;

    @Column(nullable = false)
    private Double usageAmount;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public ServiceType getService() { return service; }
    public void setService(ServiceType service) { this.service = service; }
    public Double getUsageAmount() { return usageAmount; }
    public void setUsageAmount(Double usageAmount) { this.usageAmount = usageAmount; }

    @Override
    public String toString() {
        return "Cdr{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", startTime=" + startTime +
                ", service='" + service + '\'' +
                ", usageAmount=" + usageAmount +
                '}';
    }
}
