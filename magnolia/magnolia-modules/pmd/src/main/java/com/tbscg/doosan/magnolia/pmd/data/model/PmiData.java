package com.tbscg.doosan.magnolia.pmd.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmiData {

    private String code;
    private final Map<String, String> name = new HashMap<>();

    public PmiData(String code, List<NameTranslation> translations) {
        this.code = code;
        translations.forEach(t -> this.name.put(t.getLocale(), t.getName()));
    }
}
