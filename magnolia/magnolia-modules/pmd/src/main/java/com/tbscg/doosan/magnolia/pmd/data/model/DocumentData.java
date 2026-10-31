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
public class DocumentData {

    @JsonProperty("type_id")
    private long typeId;
    private final Map<String, String> title = new HashMap<>();
    private final Map<String, String> description = new HashMap<>();

    public DocumentData(Document document, List<DocumentTranslation> translations) {
        this.typeId = document.getTypeId();
        translations.forEach(t -> {
            this.title.put(t.getLocale(), t.getTitle());
            this.description.put(t.getLocale(), t.getDescription());
        });
    }
}
