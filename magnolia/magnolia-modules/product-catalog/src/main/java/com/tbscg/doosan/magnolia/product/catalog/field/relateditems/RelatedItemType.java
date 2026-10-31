package com.tbscg.doosan.magnolia.product.catalog.field.relateditems;

import lombok.Getter;

import java.util.Arrays;

/**
 * Class to aggregate some related item type properties.
 */
@Getter
public enum RelatedItemType {

    PRODUCT("product", "dxp:product", "products", "productHierarchy"),
    ATTACHMENT("attachment", "dxp:attachment", "attachments", "attachments"),
    PAGE("page", "mgnl:page", "website", "browser");

    private String name;
    private String nodeType;
    private String workspace;
    private String subAppName;

    RelatedItemType(String name, String nodeType, String workspace, String subAppName) {
        this.name = name;
        this.nodeType = nodeType;
        this.workspace = workspace;
        this.subAppName = subAppName;
    }

    /**
     * Returns proper RelatedItemType base on provided name.
     *
     * @param name name of RelatedItemType to return
     * @return matching RelatedItemType or null
     */
    public static RelatedItemType fromString(String name) {
        return Arrays.stream(RelatedItemType.values())
                .filter(item -> item.getName().equals(name))
                .findFirst().orElse(null);
    }

}
