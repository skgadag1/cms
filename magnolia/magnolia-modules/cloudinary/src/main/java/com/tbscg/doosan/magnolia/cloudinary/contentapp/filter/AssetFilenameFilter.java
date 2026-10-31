package com.tbscg.doosan.magnolia.cloudinary.contentapp.filter;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssetFilenameFilter implements AssetFilterDefinition {

    private String query;

    public AssetFilenameFilter(String query) {
        this.query = query;
    }

    private static final Pattern CLOUDINARY_ESCAPABLE_CHARACTERS = Pattern.compile("[ !(){}\\[\\]^~?:\\\\=&><]");
    private static final String FILENAME = "filename";

    private static String escapeCloudinarySearchExpression(CharSequence expression) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = CLOUDINARY_ESCAPABLE_CHARACTERS.matcher(expression);
        while (matcher.find()) {
            matcher.appendReplacement(result, "\\\\$0");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @Override
    public String getAssetQueryExpression() {
        return FILENAME + ":" + escapeCloudinarySearchExpression(query.toLowerCase()) + "*";
    }

    @Override
    public boolean matches(CloudinaryAsset asset) {
        String propertyValue = asset.getProperty(FILENAME);
        return propertyValue.toLowerCase().contains(query.toLowerCase());
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
