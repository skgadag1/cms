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
public class ScheduleConnection {

    @JsonProperty("connection_id")
    private long id;
    @JsonProperty("schedule_id")
    private long scheduleId;
    @JsonProperty("family_code")
    private String familyCode;
    @JsonProperty("family_id")
    private String familyId;
    @JsonProperty("subfamily_code")
    private String subfamilyCode;
    @JsonProperty("subfamily_id")
    private String subfamilyId;
    @JsonProperty("model_code")
    private String modelCode;
    @JsonProperty("model_id")
    private Long modelId;
    @JsonProperty("prefix_id")
    private Long prefixId;
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("suffix_start")
    private String suffixStart;
    @JsonProperty("suffix_end")
    private String suffixEnd;
    @JsonProperty("range_id")
    private Long rangeId;
    private String range;

    public String getType() {
        if (modelId != null) {
            if (prefixId != null) {
                if (rangeId != null) {
                    return "range";
                }
                return "prefix";
            }
            return "model";
        }
        throw new IllegalStateException("invalid data, connection type not determined");
    }

    public String getLabel() {
        String type = getType();
        if ("model".equals(type)) {
            return getModelCode();
        } else if ("prefix".equals(type)) {
            return getPrefix();
        } else if ("range".equals(type)) {
            return getSuffixStart() + "-" + getSuffixEnd();
        }
        return "";
    }
}
