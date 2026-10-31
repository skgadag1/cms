package com.tbscg.doosan.magnolia.cloudinary.dam.reference;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;

import info.magnolia.dam.api.AssetDecorator;

public class CloudinaryAssetReference extends AssetDecorator {

	public CloudinaryAssetReference(CloudinaryAsset asset) {
		super(asset);
	}

	@Override
	public String getLink() {
		CloudinaryAsset decorated = (CloudinaryAsset) getDecorated();
		return decorated.getTransformationUrlTemplate();
	}
}
