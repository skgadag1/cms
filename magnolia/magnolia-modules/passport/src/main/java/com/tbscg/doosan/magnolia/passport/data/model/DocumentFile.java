package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class DocumentFile {

    private long id;
    @JsonProperty("document_id")
    private long documentId;
    @JsonProperty("storage_reference")
    private String fileName;
    private String locale;
    @JsonProperty("content_type")
    private String contentType;

}
