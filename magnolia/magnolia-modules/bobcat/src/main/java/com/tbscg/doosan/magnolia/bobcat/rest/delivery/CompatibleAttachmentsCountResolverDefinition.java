package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.rest.reference.ConfiguredReferenceResolverDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompatibleAttachmentsCountResolverDefinition extends ConfiguredReferenceResolverDefinition {

    public CompatibleAttachmentsCountResolverDefinition() {
        setImplementationClass(CompatibleAttachmentsCountResolver.class);
    }
}

