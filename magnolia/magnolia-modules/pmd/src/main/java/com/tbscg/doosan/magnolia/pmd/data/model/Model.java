package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Model extends ConnectTarget {

    private String code;
    @JsonProperty("brand_id")
    private long brandId;
    @JsonProperty("brand_code")
    private String brandCode;
    @JsonProperty("family_id")
    private long familyId;
    @JsonProperty("family_code")
    private String familyCode;
    @JsonProperty("subfamily_id")
    private long subfamilyId;
    @JsonProperty("subfamily_code")
    private String subfamilyCode;

    @Override
    public String getLabel() {
        return getCode();
    }

    @Override
    public boolean hasChildren() {
        return true;
    }
}
