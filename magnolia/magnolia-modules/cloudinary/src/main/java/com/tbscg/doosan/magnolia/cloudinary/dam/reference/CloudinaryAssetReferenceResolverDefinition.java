package com.tbscg.doosan.magnolia.cloudinary.dam.reference;

import info.magnolia.rest.reference.dam.ConfiguredAssetReferenceResolverDefinition;

public class CloudinaryAssetReferenceResolverDefinition extends ConfiguredAssetReferenceResolverDefinition {

	public CloudinaryAssetReferenceResolverDefinition() {
		super();
		setImplementationClass(CloudinaryAssetReferenceResolver.class);
		setExpand(false);
		setIncludeAssetMetadata(false);
		setIncludeDownloadLink(true);
	}
}
