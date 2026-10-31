package com.tbscg.doosan.magnolia.product.catalog.field;

import com.vaadin.data.ValueProvider;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.RepositoryException;

/**
 * Value Provider for product and feature types used in column definition.
 */
@Slf4j
public class ProductTypeValueProvider implements ValueProvider<JcrNodeWrapper, String> {

    @Override
    public String apply(final JcrNodeWrapper jcrNode) {
        try {
            final String jcrType = jcrNode.getPrimaryNodeType().getName();
            return ProductType.getNameByType(jcrType);
        } catch (RepositoryException e) {
            log.error("Failed to map jcr:primaryType to its corresponding type in Product Catalog app.", e);
            return "Unknown";
        }
    }
}
