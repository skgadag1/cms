package com.tbscg.doosan.magnolia.cloudinary.dam;

import java.io.InputStream;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import com.google.common.net.MediaType;
import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;

import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetDecorator;
import info.magnolia.dam.api.AssetRenderer;
import info.magnolia.dam.api.AssetRendition;
import info.magnolia.objectfactory.ComponentProvider;

/**
 * Asset renderer for Cloudinary, using url-based transformations to generate renditions.
 */
@Slf4j
public class CloudinaryAssetRenderer implements AssetRenderer {

	private final ComponentProvider components;

	@Inject
	public CloudinaryAssetRenderer(ComponentProvider components) {
		this.components = components;
	}

	public RenditionService getRenditionService() {
		return this.components.getComponent(RenditionService.class);
	}

	@Override
	public boolean supports(MediaType from, MediaType to) {
		if (from == null || to == null) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the provided {@link info.magnolia.dam.api.Asset} and the
	 * {@link com.google.common.net.MediaType} can be rendered by this
	 * {@link info.magnolia.dam.api.AssetRenderer}.
	 *
	 * @see #supports(com.google.common.net.MediaType,
	 *      com.google.common.net.MediaType)
	 */
	@Override
	public boolean canRender(Asset asset, MediaType to) {
		final Asset unwrappedAsset = unwrapDecoratedAsset(asset);
		if (unwrappedAsset == null || !(unwrappedAsset instanceof CloudinaryAsset)) {
			return false;
		}
		return supports(MediaType.parse(unwrappedAsset.getMimeType()), to);
	}

	/**
	 * Renders an {@link info.magnolia.dam.api.AssetRendition} fo the provided
	 * {@link info.magnolia.dam.api.Asset}.
	 */
	@Override
	public AssetRendition render(Asset asset, MediaType to, String renditionName) {
		log.debug("render(" + asset.getFileName() + ", " + to + ", " + renditionName + ")");
		asset = unwrapDecoratedAsset(asset); // Unwrap legacy assets

		if (asset instanceof CloudinaryAsset) {
			CloudinaryAsset cloudinaryAsset = (CloudinaryAsset) asset;
			String link = getRenditionService().getRenditionUrl(cloudinaryAsset, renditionName);
			return new CloudinaryAssetRendition(asset, link, to, renditionName);
		} else {
			throw new IllegalArgumentException("Asset renditions of non-cloudinary assets cannot be generated.");
		}
	}

	public AssetRendition transform(Asset asset, MediaType to, String transformation) {
		log.debug("transform(" + asset.getFileName() + ", " + to + ", " + transformation + ")");
		asset = unwrapDecoratedAsset(asset); // Unwrap legacy assets

		if (asset instanceof CloudinaryAsset) {
			CloudinaryAsset cloudinaryAsset = (CloudinaryAsset) asset;
			String link = getRenditionService().getTransformationUrl(cloudinaryAsset, transformation);
			return new CloudinaryAssetRendition(asset, link, to, transformation);
		} else {
			throw new IllegalArgumentException("Asset renditions of non-cloudinary assets cannot be generated.");
		}
	}

	/**
	 * Unwraps an {@link info.magnolia.dam.api.Asset} if necessary.
	 *
	 * <p>
	 * This is required for being backwards compatible to legacy code that might
	 * still wrap {@link info.magnolia.dam.api.Asset}s.
	 * </p>
	 */
	private Asset unwrapDecoratedAsset(Asset asset) {
		if (asset instanceof AssetDecorator) {
			return AssetDecorator.unwrap(asset);
		}
		return asset;
	}

	/**
	 * {@link info.magnolia.dam.api.AssetRendition} based on the imaging module.
	 */
	private static class CloudinaryAssetRendition implements AssetRendition {

		private final Asset asset;
		private final String link;
		private final String renditionName;
		private final MediaType mimeType;

		public CloudinaryAssetRendition(Asset asset, String link, MediaType mediaType, String renditionName) {
			this.asset = asset;
			this.link = link;
			this.renditionName = renditionName;
			this.mimeType = mediaType;
		}

		@Override
		public String getRenditionName() {
			return renditionName;
		}

		@Override
		public String getMimeType() {
			return mimeType.toString();
		}

		/**
		 * Currently we do not support returning an {@link java.io.InputStream} of the
		 * rendition itself. We should not simply return
		 * {@link info.magnolia.dam.api.Asset#getContentStream()}!
		 */
		@Override
		public InputStream getStream() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Asset getAsset() {
			return asset;
		}

		@Override
		public String getLink() {
			return link;
		}

	}
}