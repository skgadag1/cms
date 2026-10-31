package com.tbscg.doosan.magnolia.cloudinary.util;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.cloudinary.CloudinaryConfig;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.field.dto.CloudinaryFileBrowser;
import info.magnolia.dam.api.Asset;
import info.magnolia.objectfactory.annotation.LazySingleton;

import javax.inject.Inject;

/**
 * This service class is used by field factory classes that are linked to {@link CloudinaryAsset}.
 */
@LazySingleton
public class CloudinaryAssetFieldService {

    private final CloudinaryConfig config;

    @Inject
    public CloudinaryAssetFieldService(CloudinaryConfig config) {
        this.config = config;
    }

    /**
     * Creates cloudinary file browser model that is being used by CKEditor plugin.
     *
     * @param asset existing asset handled by Magnolia
     * @return filebrowser model used by plugins
     */
    public CloudinaryFileBrowser getCloudinaryFileBrowser(Asset asset) {
        Preconditions.checkArgument(asset instanceof CloudinaryAsset, "Selected asset needs to be a Cloudinary asset");
        Preconditions.checkNotNull(this.config.getCloudName(), "Cloud name needs to be configured");

        CloudinaryAsset cloudinaryAsset = (CloudinaryAsset) asset;

        return new CloudinaryFileBrowser(
                cloudinaryAsset.getLink(), this.config.getCloudName(), cloudinaryAsset.getPublicId(),
                cloudinaryAsset.getWidth(), cloudinaryAsset.getHeight(), cloudinaryAsset.getAspectRatio(),
                this.config.getCloudPlayer()
        );
    }
}
