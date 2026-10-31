package com.tbscg.doosan.magnolia.cloudinary.dam;

import info.magnolia.dam.api.metadata.AssetMetadata;

import java.util.Map;

/**
 * Implementation of {@link AssetMetadata} to hold metadata coming from Cloudinary.
 */
public class CloudinaryAssetMetadata implements AssetMetadata {

    private final Map<String, Object> contextualMetadata;
    private final Map<String, Object> structuredMetadata;

    public CloudinaryAssetMetadata(Map<String, Object> contextualMetadata, Map<String, Object> structuredMetadata) {
        this.contextualMetadata = contextualMetadata;
        this.structuredMetadata = structuredMetadata;
    }

    @Override
    public boolean isSortable(String propertyId) {
        return false;
    }

    public Map<String, Object> getContextualMetadata() {
        return contextualMetadata;
    }

    public Map<String, Object> getStructuredMetadata() {
        return structuredMetadata;
    }
}
