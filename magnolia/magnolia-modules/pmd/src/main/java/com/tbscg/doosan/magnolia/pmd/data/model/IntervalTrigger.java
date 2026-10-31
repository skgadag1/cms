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
public class IntervalTrigger {

    private long id;
    @JsonProperty("interval_id")
    private long intervalId;
    @JsonProperty("type_id")
    private long typeId;
    @JsonProperty("type_code")
    private String typeCode;
    private double interval;
}
