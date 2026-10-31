package com.tbscg.doosan.magnolia.product.catalog.field.ui;

import lombok.Getter;

import java.util.List;

/**
 * Table cell used in Vaadin's @{@link com.vaadin.ui.Grid}.
 */
@Getter
public class TableCell {
    private final List<String> values;

    public TableCell(final List<String> values) {
        this.values = values;
    }
}
