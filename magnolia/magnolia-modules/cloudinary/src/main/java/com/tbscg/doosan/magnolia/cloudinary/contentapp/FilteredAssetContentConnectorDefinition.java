package com.tbscg.doosan.magnolia.cloudinary.contentapp;

import com.tbscg.doosan.magnolia.cloudinary.contentapp.filter.AssetFilterDefinition;
import info.magnolia.dam.external.app.contentconnector.ConfiguredAssetContentConnectorDefinition;

import java.util.Collections;
import java.util.List;

public class FilteredAssetContentConnectorDefinition extends ConfiguredAssetContentConnectorDefinition {

    private List<AssetFilterDefinition> filters = Collections.emptyList();

    public FilteredAssetContentConnectorDefinition() {
        setImplementationClass(CloudinaryAssetContentConnector.class);
    }

    public List<AssetFilterDefinition> getFilters() {
        return filters;
    }

    public void setFilters(List<AssetFilterDefinition> filters) {
        this.filters = filters;
    }
}
