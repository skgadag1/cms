package com.tbscg.doosan.magnolia.cloudinary.contentapp;

import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;
import info.magnolia.dam.api.AssetProvider;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.api.PathAwareAssetProvider;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnector;
import info.magnolia.dam.external.app.contentconnector.ConfiguredAssetContentConnectorDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudinaryAssetContentConnector extends AssetContentConnector {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryAssetContentConnector.class);

    private final AssetProvider assetProvider;

    @Inject
    public CloudinaryAssetContentConnector(ConfiguredAssetContentConnectorDefinition contentConnectorDefinition, ComponentProvider componentProvider, AssetProviderRegistry assetProviderRegistry) {
        super(contentConnectorDefinition, componentProvider, assetProviderRegistry);
        this.assetProvider = assetProviderRegistry.getProviderById(CloudinaryConstants.ASSET_PROVIDER_ID);
    }

    @Override
    public String getItemUrlFragment(Object itemId) {
        if (itemId instanceof ItemKey) {
            ItemKey key = (ItemKey) itemId;
            try {
                info.magnolia.dam.api.Item item = assetProvider.getItem(key);
                if (item != null) {
                    return item.getPath();
                }
            } catch (AssetProvider.AssetNotFoundException e) {
                log.debug("Failed to retrieve item with given id: " + e.getMessage(), e);
            } catch (AssetProvider.IllegalItemKeyException e) {
                log.debug("Failed to convert item id to URL fragment: " + e.getMessage(), e);
            }
        }
        return null;
    }

    private static final Pattern CLOUDINARY_ASSET_JCR_VALUE = Pattern.compile(
            CloudinaryConstants.ASSET_PROVIDER_ID + ":([^#?&%<>\\\\]+#[a-zA-Z0-9+/=]+)");

    @Override
    public Object getItemIdByUrlFragment(String urlFragment) {
        if (StringUtils.isNotBlank(urlFragment)) {
            try {
                Matcher cloudinaryValueMatcher = CLOUDINARY_ASSET_JCR_VALUE.matcher(urlFragment);
                if (cloudinaryValueMatcher.matches()) {
                    return new ItemKey(CloudinaryConstants.ASSET_PROVIDER_ID, cloudinaryValueMatcher.group(1));
                } else {
                    String path = urlFragment.startsWith("/") ? urlFragment.substring(1) : urlFragment;
                    return ((PathAwareAssetProvider) this.assetProvider).getItem(path).getItemKey();
                }
            } catch (AssetProvider.AssetNotFoundException | PathAwareAssetProvider.PathNotFoundException e) {
                log.debug("Failed to obtain asset id for fragment", e);
            }
        }
        return null;
    }

    @Override
    public Item getItem(Object itemId) {
        if (!(itemId instanceof ItemKey)) {
            return null;
        }
        ItemKey key = (ItemKey) itemId;
        try {
            info.magnolia.dam.api.Item item = assetProvider.getItem(key);
            if (item != null) {
                return new BeanItem<info.magnolia.dam.api.Item>(item);
            }
        } catch (AssetProvider.AssetNotFoundException | AssetProvider.IllegalItemKeyException e) {
            log.debug("Failed to find item for id: " + itemId, e);
        }
        return null;
    }
}
