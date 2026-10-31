package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {

    private long id;
    private String locale;
    private String name;
    @JsonProperty("business_unit")
    private String businessUnit;
    @JsonProperty("maintenance_instruction_url")
    private String maintenanceInstructionUrl;
}
