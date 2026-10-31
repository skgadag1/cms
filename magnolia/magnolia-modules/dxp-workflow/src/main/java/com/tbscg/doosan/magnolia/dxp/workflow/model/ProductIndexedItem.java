package com.tbscg.doosan.magnolia.dxp.workflow.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class ProductIndexedItem extends ModelIndexedItem {
    final String lifecycle;
    final String group;

    public ProductIndexedItem(String uuid, String path, String lifecycle, long lastModified, Map<String, String> links,
                              NodeDetails category, NodeDetails subCategory, String group) {
        super(uuid, path, lastModified, links, category, subCategory);
        this.lifecycle = lifecycle;
        this.group = group;
    }
}
