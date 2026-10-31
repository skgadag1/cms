package com.tbscg.doosan.magnolia.cloudinary.field.dto;

import info.magnolia.dam.app.ui.field.factory.AssetsEnabledRichTextFieldFactory;

public class CloudinaryFileBrowser extends AssetsEnabledRichTextFieldFactory.FileBrowserUrlDTO {

    private Long width;
    private Long height;
    private Number aspectRatio;
    private String publicId;
    private String cloudName;
    private String cloudPlayer;

    public CloudinaryFileBrowser(String url, String cloudName, String publicId, Long width, Long height,
                                 Number aspectRatio, String cloudPlayer) {
        super(url);
        this.width = width;
        this.height = height;
        this.aspectRatio = aspectRatio;
        this.publicId = publicId;
        this.cloudName = cloudName;
        this.cloudPlayer = cloudPlayer;
    }

    public CloudinaryFileBrowser(String url, String errorMessage) {
        super(url, errorMessage);
    }

    public CloudinaryFileBrowser(String url) {
        super(url);
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Number getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(Number aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getCloudPlayer() { return cloudPlayer; }

    public void setCloudPlayer(String cloudPlayer) { this.cloudPlayer = cloudPlayer; }
}
