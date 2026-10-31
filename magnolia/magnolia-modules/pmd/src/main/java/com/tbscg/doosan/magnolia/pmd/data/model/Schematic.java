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
public class Schematic {

    private long id;
    @JsonProperty("part_numbers")
    private String partNumbers;
    private String locale;
    private String name;
    private String type;
    @JsonProperty("type_id")
    private long typeId;
    @JsonProperty("download_link")
    private String downloadLink;
}
