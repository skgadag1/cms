package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Prefix extends ConnectTarget {

    private String prefix;
    @JsonProperty("model_id")
    private long modelId;
    @JsonProperty("model_code")
    private String modelCode;
    @JsonProperty("family_id")
    private long familyId;
    @JsonProperty("family_code")
    private String familyCode;
    @JsonProperty("subfamily_id")
    private long subfamilyId;
    @JsonProperty("subfamily_code")
    private String subfamilyCode;
    @JsonProperty("brand_id")
    private long brandId;
    @JsonProperty("brand_code")
    private String brandCode;

    @Override
    public String getLabel() {
        return getPrefix();
    }

    @Override
    public boolean hasChildren() {
        return true;
    }
}
