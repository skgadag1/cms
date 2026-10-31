package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config;

import com.google.common.net.MediaType;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.thumbnail.CloudinaryThumbnailComponentProvider;
import info.magnolia.dam.app.ui.field.configuration.PreviewConfiguration;

import java.util.List;

public class AssetPreviewConfiguration {

    private PreviewConfiguration previewConfig;
    private Class<? extends CloudinaryThumbnailComponentProvider> thumbnailComponentProviderClass;
    private List<MediaType> supportedMediaTypes;

    public boolean hasPreviewConfig() {
        return previewConfig != null && previewConfig.getPreviewComponentProviderClass() != null;
    }


    // --------------- Getter and Setter Section ------------ //

    public PreviewConfiguration getPreviewConfig() {
        return previewConfig;
    }

    public void setPreviewConfig(PreviewConfiguration previewConfig) {
        this.previewConfig = previewConfig;
    }

    public Class<? extends CloudinaryThumbnailComponentProvider> getThumbnailComponentProviderClass() {
        return thumbnailComponentProviderClass;
    }

    public void setThumbnailComponentProviderClass(Class<? extends CloudinaryThumbnailComponentProvider> thumbnailComponentProviderClass) {
        this.thumbnailComponentProviderClass = thumbnailComponentProviderClass;
    }

    public List<MediaType> getSupportedMediaTypes() {
        return supportedMediaTypes;
    }

    public void addSupportedMediaType(MediaType mediaType) {
        this.supportedMediaTypes.add(mediaType);
    }

    public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
        this.supportedMediaTypes = supportedMediaTypes;
    }
}