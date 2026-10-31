
package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
public class SubfamilyData extends PmiData {

    @JsonProperty("family_id")
    private long familyId;

    public SubfamilyData(Subfamily subfamily, List<NameTranslation> translations) {
        super(subfamily.getCode(), translations);
        this.familyId = subfamily.getFamilyId();
    }
}
