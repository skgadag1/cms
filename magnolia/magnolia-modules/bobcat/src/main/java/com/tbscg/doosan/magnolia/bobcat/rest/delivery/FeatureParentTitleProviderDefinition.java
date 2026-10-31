package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.rest.reference.jcr.ConfiguredJcrReferenceResolverDefinition;

public class FeatureParentTitleProviderDefinition extends ConfiguredJcrReferenceResolverDefinition {
    public FeatureParentTitleProviderDefinition() {
        setImplementationClass(FeatureParentTitleResolver.class);
    }
}
