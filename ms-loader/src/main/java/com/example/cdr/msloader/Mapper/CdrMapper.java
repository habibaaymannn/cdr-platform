package com.example.cdr.msloader.Mapper;

import com.example.cdr.msloader.DTO.CdrDTO;
import com.example.cdr.msloader.Model.CdrLoader;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Mapper(componentModel = "spring")
public interface CdrMapper {
    @Mapping(source = "startTime" , target = "startTime", qualifiedByName = "formatDate")
    @Mapping(target = "service", expression = "java(cdr.getService().name())")
    CdrDTO toCdrDTO(CdrLoader cdr);

    @Named("formatDate")
    default String formatDate(LocalDateTime localDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return localDate.format(formatter);
    };
}
