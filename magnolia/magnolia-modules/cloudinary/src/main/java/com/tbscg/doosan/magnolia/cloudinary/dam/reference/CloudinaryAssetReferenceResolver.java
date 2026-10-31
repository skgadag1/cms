package com.tbscg.doosan.magnolia.cloudinary.dam.reference;

import java.util.Optional;

import javax.inject.Inject;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;

import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetDecorator;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.rest.reference.ReferenceResolverDefinition;
import info.magnolia.rest.reference.dam.AssetReferenceResolver;

public class CloudinaryAssetReferenceResolver extends AssetReferenceResolver {

	@Inject
	public CloudinaryAssetReferenceResolver(AssetProviderRegistry assetProviderRegistry, ReferenceResolverDefinition referenceResolverDefinition) {
		super(assetProviderRegistry, referenceResolverDefinition);
	}

	@Override
	public Optional<Asset> resolve(String item) {
		Optional<Asset> optional = super.resolve(item);
		if (optional.isPresent()) {
			AssetDecorator asset = (AssetDecorator) optional.get();
			Asset decorated = asset.getDecorated();
			if (decorated instanceof CloudinaryAsset) {
				CloudinaryAsset ca = (CloudinaryAsset) decorated;
				ItemKey key = ca.getItemKey();
				ca.setItemKey(new ItemKey(key.getProviderId(), cleanupAssetId(key.getAssetId())));
				return Optional.of(new CloudinaryAssetReference(ca));
			}
		}
		return optional;
	}

	private String cleanupAssetId(String id) {
		int indexOfSeparator = id.indexOf('#');
		if (id != null && indexOfSeparator >= 0) {
			return id.substring(0, indexOfSeparator);
		}
		return id;
	}
}
