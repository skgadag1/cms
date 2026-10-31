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
public class DocumentFile {

    private long id;
    private String locale;
    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("file_name")
    private String fileName;
    @JsonProperty("download_url")
    private String downloadUrl;
}
