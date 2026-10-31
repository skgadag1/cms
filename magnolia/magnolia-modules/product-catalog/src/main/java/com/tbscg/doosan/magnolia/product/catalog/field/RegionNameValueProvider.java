package com.tbscg.doosan.magnolia.product.catalog.field;

import com.vaadin.data.ValueProvider;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.RepositoryException;

/**
 * Value Provider for product and feature types used in column definition.
 */
@Slf4j
public class RegionNameValueProvider implements ValueProvider<JcrNodeWrapper, String> {

    @Override
    public String apply(final JcrNodeWrapper jcrNode) {
        try {
            final String[] nameParts = jcrNode.getPath().split("/");
            if (nameParts.length < 4) {
                return "";
            }

            return nameParts[2];
        } catch (RepositoryException e) {
            log.error("Failed to extract root region name.", e);
            return "Unknown";
        }
    }
}
