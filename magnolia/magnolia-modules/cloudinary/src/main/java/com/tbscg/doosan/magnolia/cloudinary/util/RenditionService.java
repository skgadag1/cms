package com.tbscg.doosan.magnolia.cloudinary.util;

import com.tbscg.doosan.magnolia.cloudinary.CloudinaryConfig;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;

public class RenditionService {

	private final CloudinaryConfig config;

	@Inject
	public RenditionService(CloudinaryConfig config) {
		this.config = config;
	}

	public String getRenditionUrl(CloudinaryAsset asset, String renditionName) {
		return getTransformationUrl(asset, "t_" + renditionName);
	}

	public String getTransformationUrl(CloudinaryAsset asset, String transformation) {
		String urlTemplate = getTransformationUrlTemplate(asset);
		Map<String, Object> params = new HashMap<>();
		params.put("transformation", transformation);
		UriBuilder builder = UriBuilder.fromUri(urlTemplate).resolveTemplates(params, false);
		return builder.toTemplate();
	}

	public String getTransformationUrlTemplate(CloudinaryAsset asset) {
		String urlTemplate = getConfig().getRenditionUrlTemplate();
		Map<String, Object> params = new HashMap<>();
		params.putAll(asset.getProperties());
		params.put("cloud_name", getConfig().getApi().getCloudName());

		if (StringUtils.isEmpty(urlTemplate)) {
			return asset.getLink();
		} else {
			UriBuilder builder = UriBuilder.fromUri(urlTemplate).resolveTemplates(params, false);
			return builder.toTemplate();
		}
	}

	public CloudinaryConfig getConfig() {
		return config;
	}

	public String getThumbnailUrl(CloudinaryAsset asset) {
		String thumbnailTransformation = getConfig().getThumbnailTransformation();
		if (StringUtils.isNotBlank(thumbnailTransformation)) {
			return getTransformationUrl(asset, thumbnailTransformation);
		} else {
			return this.getRenditionUrl(asset, CloudinaryConstants.THUMBNAIL_RENDITION_NAME);
		}
	}

	/**
	 * Gets the rendition URL of an asset image given a rendition name.
	 *
	 * @param asset asset to render.
	 * @param renditionName rendition to be applied.
	 * @return default image rendition URL.
	 */
	public String getDefaultImageRenditionUrl(CloudinaryAsset asset, String renditionName) {
		return this.getRenditionUrl(asset, renditionName) + CloudinaryConstants.DEFAULT_IMAGE_EXTENSION;
	}

	/**
	 * Gets the rendition URL of an asset image.
	 *
	 * @param asset asset to render.
	 * @return default image rendition URL.
	 */
	public String getDefaultImageRenditionUrl(CloudinaryAsset asset) {
		final String fileWithoutExt = FilenameUtils.removeExtension(asset.getLink());
		return fileWithoutExt + CloudinaryConstants.DEFAULT_IMAGE_EXTENSION;
	}
}
