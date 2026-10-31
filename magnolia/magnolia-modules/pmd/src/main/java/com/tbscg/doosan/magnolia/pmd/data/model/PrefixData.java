
package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
public class PrefixData {

    private String prefix;
    @JsonProperty("model_id")
    private long modelId;

    public PrefixData(Prefix prefix) {
        this.modelId = prefix.getModelId();
        this.prefix  = prefix.getPrefix();
    }
}
