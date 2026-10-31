package com.tbscg.doosan.magnolia.passport.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class PassportFileTranslation {

    private long id;
    @JsonProperty("document_id")
    private long documentId;
    private String locale;
    @JsonProperty("content_type")
    private String contentType;
    @JsonProperty("storage_reference")
    private String storageReference;
}
