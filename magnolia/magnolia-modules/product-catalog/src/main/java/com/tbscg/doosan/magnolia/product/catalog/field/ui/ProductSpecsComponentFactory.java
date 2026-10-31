package com.tbscg.doosan.magnolia.product.catalog.field.ui;

import com.tbscg.doosan.magnolia.product.catalog.field.product.ListProperty;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductProperty;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static com.tbscg.doosan.magnolia.product.catalog.field.product.ProductPropertyType.*;
import static com.tbscg.doosan.magnolia.product.catalog.field.ui.ProductSpecsMapper.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Factory producing Vaadin UI components using product specification data.
 */
public class ProductSpecsComponentFactory {

    public static final String UNPARSED_VALUE_REPLACEMENT_STRING = "(property cannot be parsed)";

    private ProductSpecsComponentFactory() {
    }

    /**
     * Creates Vaadin's TextField.
     *
     * @param title title of the field
     * @param value value of the field
     * @return Vaadin's TextField
     */
    public static TextField createTextField(final String title, final String value) {
        final TextField textField = new TextField();
        textField.setCaption(title);
        textField.setReadOnly(true);
        textField.setValue(isNotBlank(value) ? value : EMPTY_VALUE_REPLACEMENT_STRING);
        return textField;
    }

    /**
     * Creates HTML list with Vaadin's Label.
     *
     * @param property Product Property to build list from
     * @param label    label of the list
     * @return HTML list with Vaadin's Label.
     */
    public static Label createList(final ProductProperty property, final String label) {
        final Class<?> propertyType = property.getValue().getType();
        String htmlList = "";
        if (propertyType.equals(ListProperty.class)) {
            htmlList = mapListPropertiesToHtmlList(property.getValue().getListValue().stream().map(p -> (ListProperty) p).collect(toList()));
        } else if (propertyType.equals(ProductProperty.class)) {
            htmlList = mapProductPropertyToHtmlList(property);
        }
        final Label list = new Label(htmlList, ContentMode.HTML);
        list.setCaption(label);
        return list;
    }

    /**
     * Creates a table with Vaadin's Grid.
     *
     * @param rows   rows of the table
     * @param header header of the table
     * @return Vaadin's Grid table.
     */
    public static Grid<TableCell> createTable(final List<TableCell> rows, final List<String> header) {
        final Grid<TableCell> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeightByRows(rows.size());
        grid.setItems(rows);
        IntStream.range(0, header.size()).forEach(i -> grid.addColumn(c -> c.getValues().get(i)).setCaption(header.get(i)));
        return grid;
    }

    /**
     * Creates a accordion menu with Vaadin's Accordion.
     *
     * @param title    title of the accordion
     * @param sections section of the accordion
     * @return Vaadin's Accordion.
     */
    public static Accordion createAccordion(final String title, final List<ProductSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return null;
        }
        final Accordion accordion = new Accordion();
        accordion.setHeightUndefined();
        accordion.setCaption(title);
        sections.forEach(section -> {
            final Layout tab = createLayoutFromProperties(section.getProperties(), "");
            accordion.addTab(tab, section.getTitle());
        });
        return accordion;
    }

    private static Layout createLayoutFromProperties(final List<ProductProperty> properties, final String parentCaption) {
        final Layout layout = new FormLayout();
        final List<TableCell> rows = new ArrayList<>();
        layout.setStyleName("v-gridlayout-margin-left");
        if (isNotBlank(parentCaption)) {
            layout.setCaption(parentCaption);
        }

        properties.forEach(property -> {
            final String label = mapProductPropertyToStringLabel(property);
            if (TEXT_TYPES.contains(property.getType())) {
                addRowsToTheTableComponent(rows, property);
            } else if (LIST_TYPES.contains(property.getType())) {
                addListComponent(layout, property, label);
            } else if (REFERENCES.equals(property.getType())) {
                addReferencesComponent(layout, property);
            } else if (TABLE.equals(property.getType())) {
                addTableComponent(layout, property);
            } else if (UNPARSED.equals(property.getType())) {
                addUnparsedTextComponent(layout, label);
            }
        });
        if (!rows.isEmpty()) {
            addTableWithIdsComponent(layout,rows);
        }
        return layout;
    }

    private static void addUnparsedTextComponent(final Layout layout, final String label) {
        layout.addComponent(createTextField(label, ProductSpecsComponentFactory.UNPARSED_VALUE_REPLACEMENT_STRING));
    }

    private static void addListComponent(final Layout layout, final ProductProperty property, final String label) {
        final Label htmlLabel = createList(property, label);
        layout.addComponent(htmlLabel);
    }

    private static void addReferencesComponent(final Layout layout, final ProductProperty property) {
        property.getValue().getListValue()
                .stream()
                .map(ref -> (ProductProperty) ref)
                .forEach(ref -> {
                    if (NON_LIST_TYPES.contains(ref.getType())) {
                        layout.addComponent(createTextField(mapProductPropertyToStringLabel(ref), mapPropertyValueToString(ref.getValue())));
                    } else {
                        final Layout subLevelLayout = createLayoutFromProperties(
                                ref.getValue().getListValue().stream().map(p -> (ProductProperty) p).collect(toList()),
                                mapProductPropertyToStringLabel(ref) + ":");
                        layout.addComponent(subLevelLayout);
                    }
                });
    }

    private static void addTableComponent(final Layout layout, final ProductProperty property) {
        final List<TableCell> rows = property.getValue().getTableValue().getRows()
                .stream()
                .map(ProductSpecsMapper::mapProductPropertyToTableCell)
                .collect(toList());
        final List<String> header = property.getValue().getTableValue().getHeader();
        layout.addComponent(createTable(rows, header));
    }

    private static void addRowsToTheTableComponent(final List<TableCell> rows, final ProductProperty property) {
        rows.add(mapProductPropertyToTableRow(property));
    }

    private static void addTableWithIdsComponent(final Layout layout, final List<TableCell> rows) {
        List<String> header = Arrays.asList("Spec Label", "Spec Value", "Filtering ID");
        layout.addComponent(createTable(rows, header));
    }
}
