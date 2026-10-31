package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentFileData {

    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("locale")
    private String locale;
}
