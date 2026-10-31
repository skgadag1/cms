package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class IntervalData {

    @JsonProperty("labour_hours")
    private String labourHours;

    private final Map<String, String> name = new HashMap<>();

    public IntervalData(Interval interval, List<NameTranslation> translations) {
        this.labourHours = interval.getLabourHours();
        translations.forEach(t -> {
            this.name.put(t.getLocale(), t.getName());
        });
    }
}
