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
public class SchematicData {

    @JsonProperty("category_id")
    private long typeId;

    @JsonProperty("part_numbers")
    private String partNumbers;

    private final Map<String, String> name = new HashMap<>();

    public SchematicData(Schematic schematic, List<SchematicTranslation> translations) {
        this.typeId = schematic.getTypeId();
        this.partNumbers = schematic.getPartNumbers();
        translations.forEach(t -> this.name.put(t.getLocale(), t.getName()));
    }
}
