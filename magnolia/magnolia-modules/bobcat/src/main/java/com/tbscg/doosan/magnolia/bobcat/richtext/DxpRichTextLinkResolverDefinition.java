package com.tbscg.doosan.magnolia.bobcat.richtext;

import info.magnolia.rest.reference.ConfiguredReferenceResolverDefinition;

public class DxpRichTextLinkResolverDefinition extends ConfiguredReferenceResolverDefinition {
    private boolean completeUrl = true;

    public DxpRichTextLinkResolverDefinition() {
        setImplementationClass(DxpRichTextLinkResolver.class);
    }

    public boolean isCompleteUrl() {
        return completeUrl;
    }

    public void setCompleteUrl(boolean completeUrl) {
        this.completeUrl = completeUrl;
    }
}
