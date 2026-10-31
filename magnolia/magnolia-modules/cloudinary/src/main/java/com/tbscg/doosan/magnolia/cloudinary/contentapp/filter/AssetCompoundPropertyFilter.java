package com.tbscg.doosan.magnolia.cloudinary.contentapp.filter;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter
public class AssetCompoundPropertyFilter implements AssetFilterDefinition {

    private final List<AssetPropertyFilter> filters;
    private String name;
    private List<String> names;
    private String[] values;

    public AssetCompoundPropertyFilter() {
        filters = new LinkedList<>();
        names = new ArrayList<>();
    }

    @Override
    public String getAssetQueryExpression() {
        String[] queryParts = new String[names.size()];
        for (int i = 0; i < queryParts.length; i++) {
            queryParts[i] = this.names.get(i) + "=\"" + this.values[i] + "\"";
        }
        return "(" + String.join(" OR ", queryParts) + ")";
    }

    @Override
    public boolean matches(CloudinaryAsset asset) {
        return this.filters.stream().anyMatch(filter -> filter.matches(asset));
    }

    public void setName(String name) {
        this.name = name;
        Collections.addAll(this.names, name.split(","));
        updateFilters();
    }

    public void setValues(String[] values) {
        this.values = values;
        updateFilters();
    }

    private void updateFilters() {
        for (int i = 0; i < names.size(); i++) {
            if (filters.size() <= i) {
                filters.add(new AssetPropertyFilter());
            }
            filters.get(i).setName(names.get(i));
            filters.get(i).setValue(values[i]);
        }
    }
}
