package com.example.cdr.msloader.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.util.ArrayList;
import java.util.List;

public class CdrWrapper {
    @JsonProperty("cdrs")
    @JacksonXmlProperty(localName = "Cdr")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CdrLoader> cdrs = new ArrayList<>();

    public List<CdrLoader> getCdrs() {
        return cdrs;
    }

    public void setCdrs(List<CdrLoader> cdrs) {
        this.cdrs = cdrs;
    }
}