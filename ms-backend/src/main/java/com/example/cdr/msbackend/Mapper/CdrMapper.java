package com.example.cdr.msbackend.Mapper;

import com.example.cdr.msbackend.DTO.CdrDTO;
import com.example.cdr.msbackend.Model.Cdr;
import com.example.cdr.msbackend.Model.ServiceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface CdrMapper {

    @Mapping(target = "service", qualifiedByName = "toServiceType")
    Cdr toCdr(CdrDTO CdrDto);

    default LocalDateTime map(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }
    @Named("toServiceType")
    default ServiceType toServiceType(String service) {
        return ServiceType.valueOf(service);
    }

}
