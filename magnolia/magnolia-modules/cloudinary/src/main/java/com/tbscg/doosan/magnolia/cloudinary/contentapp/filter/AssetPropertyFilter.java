package com.tbscg.doosan.magnolia.cloudinary.contentapp.filter;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;

public class AssetPropertyFilter implements AssetFilterDefinition {

    private String name;
    private String value;

    @Override
    public String getAssetQueryExpression() {
        return this.name + "=\"" + value + "\"";
    }

    @Override
    public boolean matches(CloudinaryAsset asset) {
        String propertyValue = asset.getProperty(this.name);
        return this.value.equals(propertyValue);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
