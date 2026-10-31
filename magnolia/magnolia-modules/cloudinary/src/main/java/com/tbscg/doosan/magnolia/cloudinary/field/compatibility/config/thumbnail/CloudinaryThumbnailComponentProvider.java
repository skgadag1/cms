package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.thumbnail;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.vaadin.ui.Component;

/**
 * Base {@link CloudinaryThumbnailComponentProvider} definition for providing thumbnails of Cloudinary assets.
 *
 * @author Alejandro Puig Escobar
 */
public interface CloudinaryThumbnailComponentProvider {

    /**
     * Generate a Thumbnail {@link Component}.
     *
     * @param asset asset to be displayed as a Thumbnail.
     * @return Thumbnail Component (could be a Label/Resource...)
     */
    Component createThumbnailComponent(CloudinaryAsset asset);

}
