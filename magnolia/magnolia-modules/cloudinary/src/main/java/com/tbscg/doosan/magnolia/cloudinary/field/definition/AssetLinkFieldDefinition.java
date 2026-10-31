package com.tbscg.doosan.magnolia.cloudinary.field.definition;

import com.tbscg.doosan.magnolia.cloudinary.contentapp.filter.AssetFilterDefinition;
import com.tbscg.doosan.magnolia.cloudinary.field.CloudinaryAssetPreviewComponent;
import com.tbscg.doosan.magnolia.cloudinary.field.binder.AssetSelectFieldBinder;
import com.tbscg.doosan.magnolia.cloudinary.field.factory.AssetLinkFieldFactory;
import info.magnolia.ui.editor.ItemPreviewDefinition;
import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.LinkFieldDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants.APP_CLOUDINARY_ASSETS;

@FieldType("assetLinkField")
public class AssetLinkFieldDefinition<T> extends LinkFieldDefinition<T> {

    private static final String DEFAULT_APP_NAME = APP_CLOUDINARY_ASSETS;

    private String appName;
    private List<AssetFilterDefinition> filters = Collections.emptyList();

    public AssetLinkFieldDefinition() {
        super();
        setFactoryClass((Class) AssetLinkFieldFactory.class);
        setFieldBinderClass((Class) AssetSelectFieldBinder.class);
        setPreview(getItemPreviewDefinition());
    }

    private ItemPreviewDefinition<T> getItemPreviewDefinition() {
        ItemPreviewDefinition<T> previewDef = new ItemPreviewDefinition<>();
        previewDef.setImplementationClass((Class) CloudinaryAssetPreviewComponent.class);
        return previewDef;
    }

    public String getAppName() {
        return StringUtils.isNotBlank(appName) ? appName : DEFAULT_APP_NAME;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<AssetFilterDefinition> getFilters() {
        return filters;
    }

    public void setFilters(List<AssetFilterDefinition> filters) {
        this.filters = filters;
    }
}
