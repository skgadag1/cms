
package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@With
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RangeData {

    @JsonProperty("prefix_id")
    private long prefixId;
    @JsonProperty("engine_id")
    private Long engineId;
    @JsonProperty("suffix_start")
    private String suffixStart;
    @JsonProperty("suffix_end")
    private String suffixEnd;

    public RangeData(Range range) {
        this.prefixId = range.getPrefixId();
        this.suffixStart = range.getSuffixStart();
        this.suffixEnd = range.getSuffixEnd();
        this.engineId = range.getEngineId();
    }
}
