package com.tbscg.doosan.magnolia.product.catalog.field;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum representing types available in Products, Features, and Attachments workspace.
 */
public enum ProductType {

    REGION("mgnl:folder", "Region"),
    PRODUCT("dxp:product", "Product"),
    PRODUCT_GROUP("dxp:productGroup", "Product Group"),
    CATEGORY("dxp:category", "Category"),
    SUBCATEGORY("dxp:subcategory", "Subcategory"),
    FEATURE("dxp:feature", "Feature"),
    FEATURE_CATEGORY("dxp:featureCategory", "Feature Category"),
    FEATURE_GROUP("dxp:featureGroup", "Feature Group"),
    ATTACHMENT("dxp:attachment", "Attachment"),
    ATTACHMENT_GROUP("dxp:attachmentGroup", "Attachment Subcategory"),
    ATTACHMENT_CATEGORY("dxp:attachmentCategory", "Attachment Category");

    private final String type;
    private final String name;
    private static final Map<String, String> typesMap;

    static {
        typesMap = new HashMap<>();
        for (ProductType productType : values()) {
            typesMap.put(productType.type, productType.name);
        }
    }

    ProductType(final String type, final String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Returns Product Type name from JCR type.
     *
     * @param type JCR Node Type
     * @return Product Type name
     */
    public static String getNameByType(final String type) {
        return typesMap.get(type);
    }

    /**
     * Returns Product Type from JCR type
     *
     * @param type JCR Node Type
     * @return Product
     */
    public static ProductType getProductByType(final String type) {
        return Arrays.stream(values())
                .filter(productType -> productType.type.equals(type))
                .findFirst().orElse(null);
    }

    public static Set<String> findMatchedNodesByText(final String searchingText) {
        final String transformedSearchingText = searchingText.toLowerCase();

        return Arrays.stream(values())
                .filter(productType -> productType.name.toLowerCase().contains(transformedSearchingText))
                .map(productType -> productType.type)
                .collect(Collectors.toSet());
    }
}
