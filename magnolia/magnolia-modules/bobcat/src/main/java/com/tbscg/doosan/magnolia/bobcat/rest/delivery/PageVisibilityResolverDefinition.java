package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.rest.reference.jcr.ConfiguredJcrReferenceResolverDefinition;
import info.magnolia.rest.reference.jcr.JcrReferenceResolverDefinition;

/**
 * Custom implementation for {@link JcrReferenceResolverDefinition}.
 */
@SuppressWarnings("unused")
public class PageVisibilityResolverDefinition extends ConfiguredJcrReferenceResolverDefinition {
        public PageVisibilityResolverDefinition() {
            setImplementationClass(PageVisibilityResolver.class);
        }
}
