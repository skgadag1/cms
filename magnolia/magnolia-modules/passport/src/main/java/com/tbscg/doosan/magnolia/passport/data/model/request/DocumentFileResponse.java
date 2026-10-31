package com.tbscg.doosan.magnolia.passport.data.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentFileResponse {

    @JsonProperty("document_file_id")
    private long id;
    @JsonProperty("upload_url")
    private String uploadUrl;
}
