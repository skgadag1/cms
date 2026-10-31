package com.tbscg.doosan.magnolia.product.catalog.field.product;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.tbscg.doosan.magnolia.product.catalog.field.ui.ProductSpecsComponentFactory.createAccordion;
import static com.tbscg.doosan.magnolia.product.catalog.field.ui.ProductSpecsComponentFactory.createTextField;

/**
 * Template for product specification tab used {@link com.tbscg.doosan.magnolia.product.catalog.field.ProductCompositeField}.
 */
public class ProductSpecificationTemplate {
    private final TextField name;
    private final TextField serial;
    private final TextField status;
    private final TextField modifiedDate;
    private final TextField imageUrl;
    private final TextField description;
    private final Accordion sections;

    public ProductSpecificationTemplate(final ProductSpecification productSpecs) {
        this.name = createTextField("Machine name", productSpecs.getName());
        this.serial = createTextField("Serial", productSpecs.getSerial());
        this.status = createTextField("Status", productSpecs.getStatus());
        this.modifiedDate = createTextField("Modified date", productSpecs.getModifiedDate());
        this.imageUrl = createTextField("Image URL", productSpecs.getImageUrl());
        this.description = createTextField("Description", productSpecs.getDescription());
        this.sections = createAccordion("Sections", productSpecs.getSections());
    }

    /**
     * Returns template components as list of Vaadin's Components.
     *
     * @return list of Vaadin's Components.
     */
    public Component[] getComponents() {
        final List<Component> components = Arrays.asList(
                this.name,
                this.serial,
                this.status,
                this.modifiedDate,
                this.imageUrl,
                this.description,
                this.sections
        );
        return components.stream().filter(Objects::nonNull).toArray(Component[]::new);
    }
}
