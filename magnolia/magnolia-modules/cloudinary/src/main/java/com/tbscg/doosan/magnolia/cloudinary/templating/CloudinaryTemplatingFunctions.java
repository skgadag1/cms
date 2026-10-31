package com.tbscg.doosan.magnolia.cloudinary.templating;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.google.common.net.MediaType;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetRenderer;

import info.magnolia.dam.api.*;
import info.magnolia.dam.api.AssetProviderRegistry.NoSuchAssetRendererException;
import info.magnolia.dam.api.metadata.AssetMetadataRegistry;
import info.magnolia.dam.templating.functions.DamTemplatingFunctions;

@Slf4j
@Singleton
public class CloudinaryTemplatingFunctions extends DamTemplatingFunctions {

	private final AssetProviderRegistry providerRegistry;

	@Inject
	public CloudinaryTemplatingFunctions(AssetProviderRegistry providerRegistry,
			AssetMetadataRegistry metadataRegistry) {
		super(providerRegistry, metadataRegistry);
		this.providerRegistry = providerRegistry;
	}

	/**
	 * Based on the passed {@link MediaType} and {@link Asset}, get an appropriate
	 * {@link AssetRenderer}. From the {@link AssetRenderer} get the
	 * {@link AssetRendition} for the given transformation.
	 */
	public AssetRendition getTransformation(Asset asset, MediaType mediaType, String transformation) {
		try {
			final AssetRenderer renderer = providerRegistry.getRendererFor(asset, mediaType);
			if (renderer.canRender(asset, mediaType) && renderer instanceof CloudinaryAssetRenderer) {
				CloudinaryAssetRenderer cloudinaryRenderer = (CloudinaryAssetRenderer) renderer;
				return cloudinaryRenderer.transform(asset, mediaType, transformation);
			}
		} catch (NoSuchAssetRendererException nsare) {
			log.warn(
					"No rendition found for the following assetId '{}', mediaType '{}' and transformation '{}'. Exception: {} ",
					asset.getItemKey().asString(), mediaType.toString(), transformation, nsare);
		}
		return null;
	}

	/**
	 * @return {@link AssetRendition} based on the {@link MediaType} built from the
	 *         {@link Asset#getMimeType()}.
	 */
	public AssetRendition getTransformation(Asset asset, String transformation) {
		return getTransformation(asset, MediaType.parse(asset.getMimeType()), transformation);
	}

	public AssetRendition getTransformation(String itemKey, String transformation) {
		Asset asset = getAsset(itemKey);
		if (asset == null) {
			log.warn("Trying to get asset with item key {} returned null.", itemKey);
			return null;
		}
		return getTransformation(asset, MediaType.parse(asset.getMimeType()), transformation);
	}

	public AssetRendition getTransformation(String itemKey, MediaType mediaType, String transformation) {
		return getTransformation(getAsset(itemKey), mediaType, transformation);
	}

	public Asset getAsset(String itemKey) {
		try {
			validateItemKey(itemKey);
			ItemKey key = ItemKey.from(itemKey);
			return this.providerRegistry.getProviderFor(key).getAsset(key);
		} catch (Exception e) {
			log.warn("The following ItemKey '{}' generated exceptions when trying to retrieve the associated Item : {}", itemKey, e.getMessage());
		}
		return null;
	}

	public Asset getAsset(String providerId, String assetPath) {
		AssetProvider provider = this.providerRegistry.getProviderById(providerId);
		if (provider != null) {
			// Check if the provider support path
			if (!(provider instanceof PathAwareAssetProvider)) {
				throw new IllegalArgumentException("The provider '" + providerId + "' does not provide implementation for '"
						+ PathAwareAssetProvider.class.getName() + "'. Retrieving an Item by path is not a supported operation ");
			}
			return ((PathAwareAssetProvider) provider).getAsset(assetPath);
		}
		return null;
	}

	private void validateItemKey(String itemKey) {
		if (StringUtils.isBlank(itemKey)) {
			throw new IllegalArgumentException("ItemKey is null or blank.");
		}
		if (!ItemKey.isValid(itemKey)) {
			throw new IllegalArgumentException("ItemKey is not valid.");
		}
	}
}
