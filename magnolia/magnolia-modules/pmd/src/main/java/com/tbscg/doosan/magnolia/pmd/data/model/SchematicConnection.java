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
public class SchematicConnection {

    @JsonProperty("connection_id")
    private long id;
    @JsonProperty("schematic_id")
    private long schematicId;
    private String prefix;
    @JsonProperty("suffix_start")
    private String suffixStart;
    @JsonProperty("suffix_end")
    private String suffixEnd;
    private String range;
    @JsonProperty("model_code")
    private String modelCode;
    @JsonProperty("model_id")
    private String modelId;
    @JsonProperty("family_code")
    private String familyCode;
    @JsonProperty("family_id")
    private String familyId;
    @JsonProperty("subfamily_code")
    private String subfamilyCode;
    @JsonProperty("subfamily_id")
    private String subfamilyId;
}
