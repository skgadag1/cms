
package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
public class ModelData {

    private String code;
    @JsonProperty("subfamily_id")
    private long subfamilyId;

    public ModelData(Model model) {
        this.subfamilyId = model.getSubfamilyId();
        this.code = model.getCode();
    }
}
