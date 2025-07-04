package com.example.cdr.msbackend.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Entity
@Table(name = "cdr", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"source"})
})
public class Cdr {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  @Getter @Setter Long id;

    @Column(nullable = false)

    private  @Getter @Setter String source;

    @Column(nullable = false)
    private  @Getter @Setter String destination;


    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private  @Getter @Setter LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private  @Getter @Setter ServiceType service;

    @Column(nullable = false)
    private  @Getter @Setter Double usageAmount;

}
