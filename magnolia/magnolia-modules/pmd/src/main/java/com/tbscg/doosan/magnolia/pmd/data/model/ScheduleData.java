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
public class ScheduleData {

    @JsonProperty("business_unit")
    private String businessUnit;

    @JsonProperty("maintenance_instruction_url")
    private final Map<String, String> instructionUrl = new HashMap<>();
    private final Map<String, String> name = new HashMap<>();

    public ScheduleData(Schedule schedule, List<ScheduleTranslation> translations) {
        this.businessUnit = schedule.getBusinessUnit();
        translations.forEach(t -> {
            this.instructionUrl.put(t.getLocale(), t.getInstructionUrl());
            this.name.put(t.getLocale(), t.getName());
        });
    }
}
