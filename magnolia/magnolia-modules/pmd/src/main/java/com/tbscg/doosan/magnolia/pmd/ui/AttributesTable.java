package com.tbscg.doosan.magnolia.pmd.ui;

import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class AttributesTable extends Grid<AttributesTable.Attribute> {

    @Getter
    @AllArgsConstructor
    public static class Attribute {
        private String name;
        private Component value;
    }

    public AttributesTable(Attribute... values) {
        setHeaderVisible(false);
        addColumn(Attribute::getName).setStyleGenerator(a -> "");
        addComponentColumn(Attribute::getValue);
        setItems(values);
        setHeightByRows(values.length > 0 ? values.length : 1);
    }
}
