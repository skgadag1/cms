package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.rest.reference.ConfiguredReferenceResolverDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentParentResolverDefinition extends ConfiguredReferenceResolverDefinition {

    public AttachmentParentResolverDefinition() {
        setImplementationClass(AttachmentParentResolver.class);
    }
}
