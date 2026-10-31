package com.tbscg.doosan.magnolia.product.catalog.field.product;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;

/**
 * Product property type enum used in Product API.
 */
public enum ProductPropertyType {
    TEXT("text"),
    NUMBER("number"),
    IMAGE("image"),
    LIST("list"),
    NESTED_LIST("nested_list"),
    REFERENCES("references"),
    TABLE("table"),
    OPTION("option"),
    UNPARSED("unparsed");

    public static final List<ProductPropertyType> TEXT_TYPES = Arrays.asList(TEXT, NUMBER, IMAGE, OPTION, UNPARSED);
    public static final List<ProductPropertyType> LIST_TYPES = Arrays.asList(LIST, NESTED_LIST);
    public static final List<ProductPropertyType> NON_LIST_TYPES = Arrays.asList(TEXT, NUMBER, IMAGE, OPTION, UNPARSED);

    private final String value;

    ProductPropertyType(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}
