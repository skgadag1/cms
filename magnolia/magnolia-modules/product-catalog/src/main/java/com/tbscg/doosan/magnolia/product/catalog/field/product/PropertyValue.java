package com.tbscg.doosan.magnolia.product.catalog.field.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Property value of a product used in Product API.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class PropertyValue {

    private String stringValue;
    private List<?> listValue;
    private TableProperty tableValue;
    private Class<?> type;

    public PropertyValue(final String stringValue) {
        this.stringValue = stringValue;
        this.type = String.class;
    }

    public PropertyValue(final List<?> listValue, Class<?> type) {
        this.listValue = listValue;
        this.type = type;
    }

    public PropertyValue(final TableProperty tableValue) {
        this.tableValue = tableValue;
        this.type = TableProperty.class;
    }

    public Class<?> getType() {
        return this.type;
    }

}
