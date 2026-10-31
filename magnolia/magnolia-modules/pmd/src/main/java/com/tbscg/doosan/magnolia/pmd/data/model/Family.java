package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Family extends ConnectTarget {

    private String code;
    @JsonProperty("brand_id")
    private long brandId;
    @JsonProperty("brand_code")
    private String brandCode;
    private String locale;
    private String name;

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public boolean hasChildren() {
        return true;
    }
}
