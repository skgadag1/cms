package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PimppModel {

    @JsonProperty("name")
    @JacksonXmlProperty(isAttribute = true)
    private String name;

    @JsonProperty("variants")
    @JacksonXmlProperty(localName = "variant")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<PimppModelVariant> variants;

    @JsonProperty("status")
    @JacksonXmlProperty(isAttribute = true)
    private String status;

    @JsonProperty("type")
    private String type;

    @JsonProperty("id")
    private String id;
}
