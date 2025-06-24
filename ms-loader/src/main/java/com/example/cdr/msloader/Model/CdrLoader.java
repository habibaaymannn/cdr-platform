package com.example.cdr.msloader.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "cdr")
public class CdrLoader {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @JacksonXmlProperty(localName = "id")
        private Long id;

        @Column(nullable = false)
        @JacksonXmlProperty(localName = "source")
        private String source;

        @Column(nullable = false)
        @JacksonXmlProperty(localName = "destination")
        private String destination;

        @Column(nullable = false)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        @JacksonXmlProperty(localName = "startTime")
        private LocalDateTime startTime;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        @JacksonXmlProperty(localName = "service")
        private ServiceTypeLoader service;

        @Column(nullable = false)
        @JacksonXmlProperty(localName = "usageAmount")
        private Double usageAmount;


        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public LocalDateTime getStartTime() { return startTime; }

        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public ServiceTypeLoader getService() { return service; }
        public void setService(ServiceTypeLoader service) { this.service = service; }
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
