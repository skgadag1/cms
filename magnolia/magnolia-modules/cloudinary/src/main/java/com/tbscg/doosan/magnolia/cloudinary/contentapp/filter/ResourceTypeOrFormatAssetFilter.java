package com.tbscg.doosan.magnolia.cloudinary.contentapp.filter;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ResourceTypeOrFormatAssetFilter implements AssetFilterDefinition {


    private String[] resource_types_and_formats;
    private final List<String> valid_resource_types = Arrays.asList("image", "video", "raw");
    @Override
    public String getAssetQueryExpression() {
        String[] queryParts = new String[resource_types_and_formats.length];
        for (int i = 0; i < queryParts.length; i++) {
            boolean isResourceType = valid_resource_types.contains(resource_types_and_formats[i]);
            StringBuilder sb = new StringBuilder();
            sb.append(isResourceType ? "resource_type=" : "format=");
            sb.append(resource_types_and_formats[i]);
            queryParts[i] = sb.toString();
        }
        return "(" + String.join(" OR ", queryParts) + ")";
    }

    @Override
    public boolean matches(CloudinaryAsset asset) {
        String assetFormat = asset.getProperty("format");
        String assetResourceType = asset.getProperty("resource_type");
        return Arrays.stream(this.resource_types_and_formats).anyMatch(key -> key.equals(assetFormat) || key.equals(assetResourceType));
    }

    public String[] getResource_types_and_formats() {
        return resource_types_and_formats;
    }

    public void setResource_types_and_formats(String[] resource_types_and_formats) {
        this.resource_types_and_formats = resource_types_and_formats;
    }

}
