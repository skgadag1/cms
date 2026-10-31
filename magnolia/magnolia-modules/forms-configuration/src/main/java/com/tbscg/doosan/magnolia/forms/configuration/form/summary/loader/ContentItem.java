package com.tbscg.doosan.magnolia.forms.configuration.form.summary.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentItem {
    @JsonProperty("mappingId")
    private String mappingId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("value")
    private String value;
}
