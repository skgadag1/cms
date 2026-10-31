package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimCategory;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * PIM++ implementation for {@link PimCategory}
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class PimppCategory implements PimCategory {

    public static final String CURRENT_STATUS_VALUE = "current";

    @JsonProperty("name")
    @JacksonXmlProperty(isAttribute = true)
    private String name;

    @JsonProperty("status")
    @JacksonXmlProperty(isAttribute = true)
    private String status;

    @JsonProperty("categories")
    @JacksonXmlProperty(localName = "category")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<PimppCategory> subCategories;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    @JsonProperty("models")
    @JacksonXmlProperty(localName = "model")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<PimppModel> models;

    @Override
    public String getCode() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    private List<PimppProduct> products;

    private List<PimppProductGroup> productGroups;

}
