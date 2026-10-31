package com.tbscg.doosan.magnolia.cloudinary.contentapp.filter;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;

public interface AssetFilterDefinition {

    String getAssetQueryExpression();
    boolean matches(CloudinaryAsset asset);
}
