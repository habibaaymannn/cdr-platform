package com.example.cdr.msloader.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
@ToString
@Entity
@Table(name = "cdr",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"source"})
})
public class CdrLoader {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @JacksonXmlProperty(localName = "id")
        private @Setter @Getter Long id;

        @Column(nullable = false)
        @JacksonXmlProperty(localName = "source")
        private @Setter @Getter String source;

        @Column(nullable = false)
        @JacksonXmlProperty(localName = "destination")
        private @Setter @Getter String destination;

        @Column(nullable = false)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        @JacksonXmlProperty(localName = "startTime")
        private @Setter @Getter LocalDateTime startTime;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        @JacksonXmlProperty(localName = "service")
        private @Setter @Getter ServiceTypeLoader service;

        @Column(nullable = false)
        @JacksonXmlProperty(localName = "usageAmount")
        private @Setter @Getter Double usageAmount;

}
