package com.tbscg.doosan.magnolia.dxp.workflow.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Attachment;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.AttachmentCategory;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.AttachmentGroup;

public enum AttachmentType {
    ATTACHMENT(Attachment.NAME),
    GROUP(AttachmentGroup.NAME),
    CATEGORY(AttachmentCategory.NAME);

    private final String value;

    AttachmentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
