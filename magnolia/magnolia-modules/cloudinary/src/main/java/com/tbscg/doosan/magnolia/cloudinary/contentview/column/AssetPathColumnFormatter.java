package com.tbscg.doosan.magnolia.cloudinary.contentview.column;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetDetails;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.ui.Table;
import info.magnolia.dam.api.Item;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

public class AssetPathColumnFormatter extends AbstractColumnFormatter<AssetPathColumnDefinition> {

    private final ContentConnector contentConnector;

    @Inject
    public AssetPathColumnFormatter(AssetPathColumnDefinition definition, ContentConnector contentConnector) {
        super(definition);
        this.contentConnector = contentConnector;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        com.vaadin.v7.data.Item uiItem = contentConnector.getItem(itemId);
        if (uiItem instanceof BeanItem) {
            Item item = ((BeanItem<Item>) uiItem).getBean();
            if (item != null && item.isAsset()) {
                if (definition.isIncludeFileName()) {
                    return item.getPath();
                } else {
                    if (item instanceof CloudinaryAsset) {
                        CloudinaryAsset asset = (CloudinaryAsset) item;
                        return asset.getProperty(CloudinaryAssetDetails.FOLDER);
                    } else {
                        return item.getParent().getPath();
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }
}
