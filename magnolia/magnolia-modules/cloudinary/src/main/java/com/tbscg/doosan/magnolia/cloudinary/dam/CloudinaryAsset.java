package com.tbscg.doosan.magnolia.cloudinary.dam;

import com.tbscg.doosan.magnolia.cloudinary.api.ApiObject;
import com.tbscg.doosan.magnolia.dxp.core.util.PathUtil;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.Folder;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.api.metadata.AssetMetadata;
import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.tika.Tika;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class CloudinaryAsset extends AbstractCloudinaryItem implements Asset {

    private static final String ID_SEPARATOR = "#";
    private static final String RAW_RESOURCE_TYPE = "raw";

    private final CloudinaryAssetDetails assetDetails;

    public CloudinaryAsset(CloudinaryAssetProvider assetProvider, CloudinaryAssetDetails assetDetails) {
        super(assetProvider, new ItemKey(assetProvider.getIdentifier(), toJcrValue(assetDetails)));
        this.assetDetails = assetDetails;
    }

    public CloudinaryAsset(CloudinaryAssetProvider assetProvider, ItemKey assetKey) {
        this(assetProvider, fromJcrValue(assetKey.getAssetId()));
    }

    public static String toJcrValue(CloudinaryAssetDetails assetDetails) {
        return assetDetails.getPublicId() + ID_SEPARATOR + ApiObject.toEncodedString(assetDetails.getAllDetails());
    }

    public static CloudinaryAssetDetails fromJcrValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            String[] split = value.split(ID_SEPARATOR, 2);
            String encoded;
            if (split.length == 2) {
                encoded = split[1];
            } else {
                encoded = value;
            }
            try {
                return new CloudinaryAssetDetails(ApiObject.fromEncodedString(encoded));
            } catch (Exception e) {
                log.error("Error parsing encoded asset details: {}", encoded, e);
            }
        }
        log.error("Invalid asset details value: {}", value);
        throw new IllegalArgumentException("invalid JCR value: " + value);
    }

    public String getTransformationUrlTemplate() {
        return this.getAssetProvider().getRenditionService().getTransformationUrlTemplate(this);
    }

    public Map<String, Object> getProperties() {
        return this.assetDetails.getAllDetails();
    }

    public String getProperty(String key) {
        Object value = this.assetDetails.getProperty(key);
        return String.valueOf(value);
    }

    @Override
    public boolean isAsset() {
        return true;
    }

    @Override
    public String getFileName() {
        if (this.assetDetails.getFilename() != null) {
            return this.assetDetails.getFilename();
        }

        return PathUtil.getFileName(this.assetDetails.getUrl());
    }

    @Override
    public String getLink() {
        return this.assetDetails.getSecureUrl();
    }

    @Override
    public String getTitle() {
        return this.assetDetails.getFilename();
    }

    @Override
    public String getSubject() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getDescription() {
        return this.assetDetails.getDescription();
    }

    @Override
    public String getCaption() {
        return this.assetDetails.getTitle();
    }

    @Override
    public String getLanguage() {
        //default language until we get the proper assetDetails entry
        return Locale.US.getDisplayLanguage();
    }

    @Override
    public String getCopyright() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getComment() {
        return StringUtils.EMPTY;
    }

    @Override
    public Calendar getCreated() {
        return this.assetDetails.getCreatedAt();
    }

    @Override
    public Calendar getLastModified() {
        return getCreated();
    }

    @Override
    public <M extends AssetMetadata> boolean supports(Class<M> metaDataType) {
        return this.getAssetProvider().supports(metaDataType);
    }

    @Override
    public <M extends AssetMetadata> M getMetadata(Class<M> metaDataType) {
        if (!getAssetProvider().supports(metaDataType)) {
            throw new IllegalArgumentException("Unsupported requested metadata type " + metaDataType.getName());
        }

        //we only support this type of assetDetails
        return (M) this.assetDetails.getMetadata();
    }

    @Override
    public String getMimeType() {
        Tika mimeTypeResolver = Components.getComponent(Tika.class);
        return mimeTypeResolver.detect(this.assetDetails.getUrl());
    }

    @Override
    public long getFileSize() {
        return this.assetDetails.getBytes();
    }

    @Override
    public InputStream getContentStream() {
        throw new UnsupportedOperationException("Cloudinary assets should be accessed through CDN, not streamed");
    }

    @Override
    public String getName() {
        return getFileName();
    }

    @Override
    public String getPath() {
        String folderPath = this.getFixedOrDynamicFolder() != null ? this.getFixedOrDynamicFolder() : "";
        return PathUtil.getPath(folderPath, getFileName());
    }

    @Override
    public Folder getParent() {
        String folderPath = this.getFixedOrDynamicFolder() != null ? this.getFixedOrDynamicFolder() : "";
        return getAssetProvider().getFolder(folderPath);
    }

    public String getFormat() {
        return this.assetDetails.getFormat();
    }

    public Long getWidth() {
        return this.assetDetails.getWidth();
    }

    public Long getHeight() {
        return this.assetDetails.getHeight();
    }

    public Number getAspectRatio() {
        return this.assetDetails.getAspectRatio();
    }

    public String getPublicId() {
        return this.assetDetails.getPublicId();
    }

    public String getResourceType() {
        return this.assetDetails.getResourceType();
    }

    public String getType() {
        return this.assetDetails.getType();
    }

    public String getFixedOrDynamicFolder() {
        final String fixedFolder = this.assetDetails.getFolder();
        if (fixedFolder == null) {
            return this.assetDetails.getAssetFolder();
        }
        return fixedFolder;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        ItemKey itemKey = this.getItemKey();
        if (itemKey == null || obj == null || getClass() != obj.getClass()) return false;
        CloudinaryAsset that = (CloudinaryAsset) obj;
        ItemKey thatItemKey = that.getItemKey();
        if (thatItemKey == null) return false;
        return new EqualsBuilder().append(itemKey.getAssetId(), thatItemKey.getAssetId()).isEquals();
    }
}
