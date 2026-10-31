package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.rest.reference.jcr.ConfiguredJcrReferenceResolverDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KeySelectionProviderDefinition extends ConfiguredJcrReferenceResolverDefinition {

    private List<String> itemsToResolve;
    public KeySelectionProviderDefinition() {
        setImplementationClass(KeySelectionResolver.class);
    }
}
