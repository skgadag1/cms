package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PimppModelVariant {

    @JsonProperty("id")
    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JsonProperty("name")
    @JacksonXmlProperty(isAttribute = true)
    private String name;

    @JsonProperty("status")
    @JacksonXmlProperty(isAttribute = true)
    private String status;
}
