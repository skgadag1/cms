package com.tbscg.doosan.magnolia.dxp.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.Map;

@Getter
public abstract class ModelIndexedItem extends IndexedItem {
    @JsonIgnore
    final Map<String, String> links;
    final NodeDetails category;
    final NodeDetails subCategory;

    public ModelIndexedItem(String uuid, String path, long lastModified, Map<String, String> links,
                            NodeDetails category, NodeDetails subCategory) {
        super(uuid, path, lastModified);
        this.links = links;
        this.category = category;
        this.subCategory = subCategory;
    }
}
