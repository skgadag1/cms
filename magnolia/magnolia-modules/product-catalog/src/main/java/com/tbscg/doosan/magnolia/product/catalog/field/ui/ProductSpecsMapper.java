package com.tbscg.doosan.magnolia.product.catalog.field.ui;

import com.tbscg.doosan.magnolia.product.catalog.field.product.ListProperty;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductProperty;
import com.tbscg.doosan.magnolia.product.catalog.field.product.PropertyValue;

import java.util.Arrays;
import java.util.List;

import static com.tbscg.doosan.magnolia.product.catalog.field.product.ProductPropertyType.NON_LIST_TYPES;
import static com.tbscg.doosan.magnolia.product.catalog.field.product.ProductPropertyType.NUMBER;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Maps various value types of @{@link com.tbscg.doosan.magnolia.product.catalog.field.product.PropertyValue} from product specification.
 */
public class ProductSpecsMapper {

    public static final String EMPTY_VALUE_REPLACEMENT_STRING = "--";
    public static final String TABLE_VALUES_JOIN_DELIMITER = ", ";

    /**
     * Maps ProductProperty to a TableCell used in tables represented by Vaadin's Grid.
     *
     * @param productProperties product properties.
     * @return TableCell used in tables represented by Vaadin's Grid.
     */
    public static TableCell mapProductPropertyToTableCell(final List<ProductProperty> productProperties) {
        final List<String> cells = productProperties.stream().map(property -> {
            if (property.getValue() == null) {
                return EMPTY_VALUE_REPLACEMENT_STRING;
            }
            if (NON_LIST_TYPES.contains(property.getType())) {
                return mapPropertyValueToString(property.getValue());
            } else {
                final Class<?> propertyType = property.getValue().getType();
                if (propertyType.equals(ProductProperty.class)) {
                    return property.getValue().getListValue()
                            .stream()
                            .map(p -> (List<?>) p)
                            .flatMap(p -> p
                                    .stream()
                                    .map(x -> (ProductProperty) x)
                                    .filter(x -> x.getId().contains("_field"))
                                    .map(x -> NON_LIST_TYPES.contains(x.getType()) ? x.getValue().getStringValue() : "")
                            ).collect(joining(TABLE_VALUES_JOIN_DELIMITER));
                } else if (propertyType.equals(ListProperty.class)) {
                    return property.getValue().getListValue()
                            .stream()
                            .map(p -> (ListProperty) p)
                            .map(p -> String.format("%s%s", p.getItem(), isBlank(p.getNote()) ? "" : " (Note: " + p.getNote() + ")"))
                            .collect(joining(", "));
                }
                return EMPTY_VALUE_REPLACEMENT_STRING;
            }
        }).collect(toList());
        return new TableCell(cells);
    }

    /**
     * Maps ListProperties to HTML list string.
     *
     * @param properties list of ListProperties.
     * @return HTML list string.
     */
    public static String mapListPropertiesToHtmlList(final List<ListProperty> properties) {
        return properties.stream()
                .map(p -> String.format("<li>%s%s</li>",
                        p.getItem(),
                        isNotBlank(p.getNote()) ? String.format("<ul><li><i>Note: %s</i></li></ul>", p.getNote()) : "")
                )
                .collect(joining("\n", "<ul>", "</ul>"));
    }

    /**
     * Maps ProductProperty to HTML list string.
     *
     * @param property ProductProperty object.
     * @return HTML list string.
     */
    public static String mapProductPropertyToHtmlList(final ProductProperty property) {
        if (property.getValue() == null) {
            return EMPTY_VALUE_REPLACEMENT_STRING;
        }
        if (NON_LIST_TYPES.contains(property.getType())) {
            return mapPropertyValueToString(property.getValue());
        } else {
            final Class<?> propertyType = property.getValue().getType();
            if (propertyType.equals(ProductProperty.class)) {
                return property.getValue().getListValue()
                        .stream()
                        .map(p -> (ProductProperty) p)
                        .map(p -> String.format("<li>%s%s</li>",
                                isBlank(p.getLabel()) ? "" : String.format("<b>%s</b>: ", mapProductPropertyToStringLabel(p)),
                                mapProductPropertyToHtmlList(p))
                        )
                        .collect(joining("\n", "<ul>", "</ul>"));
            } else if (propertyType.equals(ListProperty.class)) {
                return mapListPropertiesToHtmlList(property.getValue().getListValue().stream().map(p -> (ListProperty) p).collect(toList()));
            }
            return EMPTY_VALUE_REPLACEMENT_STRING;
        }
    }

    /**
     * Maps PropertyValue to a string.
     *
     * @param propertyValue PropertyValue object to be mapped.
     * @return string value from PropertyValue object.
     */
    public static String mapPropertyValueToString(final PropertyValue propertyValue) {
        return propertyValue == null || isBlank(propertyValue.getStringValue()) ? EMPTY_VALUE_REPLACEMENT_STRING : propertyValue.getStringValue();
    }

    /**
     * Maps ProductProperty to a string representing product specification label. Except label's value it also contains units.
     *
     * @param property ProductProperty object to be mapped.
     * @return string representing product specification label.
     */
    public static String mapProductPropertyToStringLabel(final ProductProperty property) {
        final String unit = isNotBlank(property.getUnit()) ? " (" + property.getUnit() + ")" : "";
        return isNotBlank(property.getLabel()) ? property.getLabel() + unit : "";
    }

    public static TableCell mapProductPropertyToTableRow(final ProductProperty property) {
        return new TableCell(Arrays.asList(
                mapProductPropertyToStringLabel(property),
                mapPropertyValueToString(property.getValue()),
                property.getType().equals(NUMBER) ? property.getId() : EMPTY_VALUE_REPLACEMENT_STRING));
    }
}
