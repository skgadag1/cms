package com.tbscg.doosan.magnolia.cloudinary.dam;

import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;
import com.vaadin.server.ExternalResource;
import com.vaadin.v7.data.util.BeanItem;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.external.app.imageprovider.AssetImageProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class CloudinaryImageProvider extends AssetImageProvider {

	private final ContentConnector contentConnector;
	private final RenditionService renditionService;

	@Inject
	public CloudinaryImageProvider(ContentConnector contentConnector, AssetProviderRegistry assetProviderRegistry,
								   RenditionService renditionService) {
		super(contentConnector, assetProviderRegistry);
		this.contentConnector = contentConnector;
		this.renditionService = renditionService;
	}

	@Override
	public Object getThumbnailResource(Object itemId, String generator) {
		BeanItem<info.magnolia.dam.api.Item> item = (BeanItem<info.magnolia.dam.api.Item>) contentConnector.getItem(itemId);
		if (item != null && item.getBean() instanceof CloudinaryAsset) {
			final CloudinaryAsset asset = (CloudinaryAsset) item.getBean();
			String thumbnailUrl = this.renditionService.getThumbnailUrl(asset);
			return new ExternalResource(thumbnailUrl);
		} else {
			log.debug("CloudinaryImageProvider works with BeanItem<com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset> only.");
		}
		return null;
	}
}
