package com.tbscg.doosan.magnolia.cloudinary.dam;

import com.tbscg.doosan.magnolia.cloudinary.api.ApiObject;

import java.util.Calendar;
import java.util.Map;

/**
 * Java class that holds asset details information coming from Cloudinary.
 * It has getter methods for default asset information.
 *
 * @author Alejandro Puig Escobar
 */
public class CloudinaryAssetDetails {

    public static final String ASPECT_RATIO = "aspect_ratio";
    public static final String FORMAT = "format";
    public static final String RESOURCE_TYPE = "resource_type";
    public static final String SECURE_URL = "secure_url";
    public static final String CREATED_AT = "created_at";
    public static final String BACKUP_BYTES = "backup_bytes";
    public static final String TYPE = "type";
    public static final String VERSION = "version";
    public static final String ACCESS_MODE = "access_mode";
    public static final String URL = "url";
    public static final String PUBLIC_ID = "public_id";
    public static final String FOLDER = "folder";
    public static final String ASSET_FOLDER = "asset_folder";
    public static final String FILENAME = "filename";
    public static final String PIXELS = "pixels";
    public static final String UPLOADED_AT = "uploaded_at";
    public static final String BYTES = "bytes";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String ETAG = "etag";
    public static final String STATUS = "status";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String CONTEXTUAL_METADATA = "context";
    public static final String STRUCTURED_METADATA = "metadata";

    private final ApiObject assetInfo;
    private final CloudinaryAssetMetadata metadata;

    public CloudinaryAssetDetails(ApiObject assetInfo) {
        this.assetInfo = assetInfo;
        this.metadata = new CloudinaryAssetMetadata(this.getProperty(CONTEXTUAL_METADATA),
                this.getProperty(STRUCTURED_METADATA));
    }

    public Number getAspectRatio() {
        return this.assetInfo.getProperties().containsKey(ASPECT_RATIO) ? this.assetInfo.getNumber(ASPECT_RATIO) : 0;
    }

    public String getFormat() {
        return this.assetInfo.getString(FORMAT);
    }

    public String getResourceType() {
        return this.assetInfo.getString(RESOURCE_TYPE);
    }

    public String getSecureUrl() {
        return this.assetInfo.getString(SECURE_URL);
    }

    public Calendar getCreatedAt() {
        return this.assetInfo.getDate(CREATED_AT);
    }

    public Long getBackupBytes() {
        return this.assetInfo.getLong(BACKUP_BYTES);
    }

    public String getType() {
        return this.assetInfo.getString(TYPE);
    }

    public Long getVersion() {
        return this.assetInfo.getLong(VERSION);
    }

    public String getAccessMode() {
        return this.assetInfo.getString(ACCESS_MODE);
    }

    public String getUrl() {
        return this.assetInfo.getString(URL);
    }

    public String getPublicId() {
        return this.assetInfo.getString(PUBLIC_ID);
    }

    public String getAssetFolder() {
        return this.assetInfo.getString(ASSET_FOLDER);
    }

    public String getFolder() {
        return this.assetInfo.getString(FOLDER);
    }

    public Long getPixels() {
        return this.assetInfo.getLong(PIXELS);
    }

    public Calendar getUploadedAt() {
        return this.assetInfo.getDate(UPLOADED_AT);
    }

    public String getEtag() {
        return this.assetInfo.getString(ETAG);
    }

    public Long getBytes() {
        return this.assetInfo.getLong(BYTES);
    }

    public Long getWidth() {
        return this.assetInfo.getProperties().containsKey(WIDTH) ? this.assetInfo.getLong(WIDTH) : 0;
    }

    public Long getHeight() {
        return this.assetInfo.getProperties().containsKey(HEIGHT) ? this.assetInfo.getLong(HEIGHT) : 0;
    }

    public String getStatus() {
        return this.assetInfo.getString(STATUS);
    }

    public String getFilename() {
        return this.assetInfo.getString(FILENAME);
    }

    public String getTitle() {
        return this.assetInfo.getString(TITLE);
    }

    public String getDescription() {
        return this.assetInfo.getString(DESCRIPTION);
    }

    public String getFilenameWithExtension() {
        return this.getFilename() + "." + this.getFormat();
    }

    public CloudinaryAssetMetadata getMetadata() {
        return this.metadata;
    }

    public <M> M getProperty(String key) {
        return (M) this.assetInfo.getRaw(key);
    }

    public Map<String, Object> getAllDetails() {
        return this.assetInfo.getProperties();
    }
}
