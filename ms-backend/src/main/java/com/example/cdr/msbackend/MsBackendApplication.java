package com.example.cdr.msbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
@EnableAspectJAutoProxy
public class MsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsBackendApplication.class, args);
    }

}
