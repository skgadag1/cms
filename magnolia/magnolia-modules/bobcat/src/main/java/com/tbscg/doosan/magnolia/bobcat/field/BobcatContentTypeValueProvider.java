package com.tbscg.doosan.magnolia.bobcat.field;

import com.vaadin.data.ValueProvider;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.RepositoryException;

/**
 * Value Provider for facet types used in column definition.
 */
@Slf4j
public class BobcatContentTypeValueProvider implements ValueProvider<JcrNodeWrapper, String> {

    @Override
    public String apply(final JcrNodeWrapper jcrNode) {
        try {
            final String jcrType = jcrNode.getPrimaryNodeType().getName();
            return BobcatContentType.getNameByType(jcrType);
        } catch (RepositoryException e) {
            log.error("Failed to map jcr:primaryType to its corresponding type in Facet Configuration app.", e);
            return "Unknown";
        }
    }
}
