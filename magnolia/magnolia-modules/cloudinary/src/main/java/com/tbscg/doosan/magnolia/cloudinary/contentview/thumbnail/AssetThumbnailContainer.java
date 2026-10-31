package com.tbscg.doosan.magnolia.cloudinary.contentview.thumbnail;

import com.google.common.collect.ImmutableSet;
import com.tbscg.doosan.magnolia.cloudinary.contentview.CloudinaryAssetContainer;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetDetails;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.AbstractProperty;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnector;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.layout.data.PagingThumbnailContainer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * Extension of base asset container, used as a datasource in thumbnail content view
 */
@Slf4j
public class AssetThumbnailContainer extends CloudinaryAssetContainer implements PagingThumbnailContainer {

    private static final long serialVersionUID = 1L;

    public static final String THUMBNAIL_PROPERTY_ID = "thumbnail";

    private final ImageProvider imageProvider;

    public AssetThumbnailContainer(AssetContentConnector connector, ImageProvider imageProvider) {
        super(connector);
        this.imageProvider = imageProvider;
    }

    @Override
    public Property getCaptionProperty(Object itemId) {
        if (itemId instanceof ItemKey) {
            ItemKey key = (ItemKey) itemId;
            return new ThumbnailCaptionProperty(key);
        } else {
            log.error("CloudinaryThumbnailContainer supports only info.magnolia.dam.api.ItemKey");
            throw new IllegalArgumentException("CloudinaryThumbnailContainer supports only info.magnolia.dam.api.ItemKey");
        }
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        return ImmutableSet.builder().addAll(super.getContainerPropertyIds()).add(THUMBNAIL_PROPERTY_ID).build();
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        if (THUMBNAIL_PROPERTY_ID.equals(propertyId)) {
            return new ThumbnailContainerProperty(itemId, imageProvider);
        }
        return super.getContainerProperty(itemId, propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        if (THUMBNAIL_PROPERTY_ID.equals(propertyId)) {
            return Object.class;
        }
        return super.getType(propertyId);
    }

    @Override
    public Object getThumbnailPropertyId() {
        return THUMBNAIL_PROPERTY_ID;
    }

    @Override
    public Property getThumbnailProperty(Object itemId) {
        return getContainerProperty(itemId, THUMBNAIL_PROPERTY_ID);
    }

    @Override
    public void setPageSize(int pageSize) {
        //don't bother with small page sizes
        super.setPageSize(Math.max(pageSize, CloudinaryAssetContainer.DEFAULT_PAGE_LENGTH));
    }

    /**
     * ThumbnailContainer property that builds a caption based on the filename.
     * Should ideally be configurable.
     */
    public class ThumbnailCaptionProperty extends AbstractProperty<String> {

        private static final long serialVersionUID = 1L;

        private String value;

        public ThumbnailCaptionProperty(final ItemKey itemKey) {
            CloudinaryAssetDetails assetMetadata = CloudinaryAsset.fromJcrValue(itemKey.getAssetId());
            this.value = assetMetadata.getFilename();
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String newValue) throws ReadOnlyException {
            this.value = newValue;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }

    public class ThumbnailContainerProperty extends AbstractProperty<Object> {

        private Object resourceId;

        private final ImageProvider imageProvider;

        public ThumbnailContainerProperty(final Object resourceId, ImageProvider imageProvider) {
            this.resourceId = resourceId;
            this.imageProvider = imageProvider;
        }

        @Override
        public Object getValue() {
            if (imageProvider == null) {
                return null;
            }
            return imageProvider.getThumbnailResource(resourceId, ImageProvider.THUMBNAIL_GENERATOR);
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException {
            this.resourceId = newValue;
        }

        @Override
        public Class<Object> getType() {
            return Object.class;
        }
    }
}
