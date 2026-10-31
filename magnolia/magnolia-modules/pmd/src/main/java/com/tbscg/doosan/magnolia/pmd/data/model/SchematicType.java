package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchematicType {

    private long id;
    private String code;
    private String locale;
    private String name;
}
