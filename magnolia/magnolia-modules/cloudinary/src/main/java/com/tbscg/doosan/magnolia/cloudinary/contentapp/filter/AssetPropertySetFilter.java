package com.tbscg.doosan.magnolia.cloudinary.contentapp.filter;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AssetPropertySetFilter implements AssetFilterDefinition {

    private String name;
    private String[] values;

    @Override
    public String getAssetQueryExpression() {
        List<String> queryParts = Arrays.stream(this.values)
                .map(el -> this.name + "=\"" + el + "\"").collect(Collectors.toList());
        return String.join(" OR ", queryParts);
    }

    @Override
    public boolean matches(CloudinaryAsset asset) {
        String propertyValue = asset.getProperty(this.name);
        return Arrays.asList(this.values).contains(propertyValue);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String values[]) {
        this.values = values;
    }
}
