
package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
public class FamilyData extends PmiData {

    @JsonProperty("brand_id")
    private long brandId;

    public FamilyData(Family family, List<NameTranslation> translations) {
        super(family.getCode(), translations);
        this.brandId = family.getBrandId();
    }
}
