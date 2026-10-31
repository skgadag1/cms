package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimCategory;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * GPIM category representation
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Jacksonized
public class GpimCategory implements PimCategory {

    private String name;

    @JsonProperty("@code")
    private String code;

    private String description;

    private List<GpimCategory> subCategories;

    private List<GpimProduct> products;

    private List<GpimProductGroup> productGroups;

}
