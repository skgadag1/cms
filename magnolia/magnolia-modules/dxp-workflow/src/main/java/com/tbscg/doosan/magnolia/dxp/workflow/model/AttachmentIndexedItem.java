package com.tbscg.doosan.magnolia.dxp.workflow.model;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class AttachmentIndexedItem extends ModelIndexedItem {
    final List<ModelIndexedItem> products;
    final List<ModelIndexedItem> relatedAttachments;
    final AttachmentType attachmentType;

    public AttachmentIndexedItem(String uuid, String path, long lastModified, Map<String, String> links, List<ModelIndexedItem> products, List<ModelIndexedItem> relatedAttachments, NodeDetails category, NodeDetails subCategory, AttachmentType attachmentType) {
        super(uuid, path, lastModified, links, category, subCategory);
        this.products = products;
        this.relatedAttachments = relatedAttachments;
        this.attachmentType = attachmentType;
    }
}
