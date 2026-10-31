package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.thumbnail;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants;
import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;

import javax.inject.Inject;

/**
 * Creates Vaadin thumbnail image component for image mime types coming from an external URL.
 *
 * @author Alejandro Puig Escobar
 */
public class ImageThumbnailComponentProvider implements CloudinaryThumbnailComponentProvider {

    private RenditionService renditionService;

    @Inject
    public ImageThumbnailComponentProvider(RenditionService renditionService) {
        this.renditionService = renditionService;
    }

    @Override
    public Component createThumbnailComponent(CloudinaryAsset asset) {
        String thumbnailUrl = this.renditionService.getThumbnailUrl(asset);

        Resource imageResource = new ExternalResource(thumbnailUrl);

        //we don't add any caption/description
        return new Image(null, imageResource);
    }

}
