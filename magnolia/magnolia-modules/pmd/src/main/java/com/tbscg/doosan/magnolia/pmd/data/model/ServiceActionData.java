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
public class ServiceActionData {

    @JsonProperty("type_id")
    private long typeId;
    @JsonProperty("quantity")
    private double quantity;
    @JsonProperty("part_number")
    private String partNumber;
}
