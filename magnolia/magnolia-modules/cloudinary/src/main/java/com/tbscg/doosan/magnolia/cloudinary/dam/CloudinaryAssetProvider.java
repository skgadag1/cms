package com.tbscg.doosan.magnolia.cloudinary.dam;

import com.tbscg.doosan.magnolia.cloudinary.api.ApiObject;
import com.tbscg.doosan.magnolia.cloudinary.api.CloudinaryService;
import com.tbscg.doosan.magnolia.cloudinary.api.FolderService;
import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;
import info.magnolia.dam.api.*;
import info.magnolia.dam.api.metadata.AssetMetadata;
import info.magnolia.objectfactory.ComponentProvider;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Iterator;

import static info.magnolia.dam.api.AssetProviderCapability.*;

public class CloudinaryAssetProvider extends AbstractAssetProvider implements PathAwareAssetProvider {

    private final ComponentProvider componentProvider;

    @Inject
    public CloudinaryAssetProvider(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    public RenditionService getRenditionService() {
        return this.componentProvider.getComponent(RenditionService.class);
    }

    public FolderService getFolderService() {
        return this.componentProvider.getComponent(FolderService.class);
    }

    public CloudinaryService getCloudinary() {
        return this.componentProvider.getComponent(CloudinaryService.class);
    }

    @Override
    protected EnumSet<AssetProviderCapability> setupProviderCapabilities() {
        return EnumSet.of(hierarchical, rendition, query, queryByKeyword, queryWithSorting);
    }

    @Override
    public boolean supports(Class<? extends AssetMetadata> metaData) {
        return metaData.isAssignableFrom(CloudinaryAssetMetadata.class);
    }

    @Override
    public Item getItem(ItemKey itemKey) throws AssetNotFoundException, IllegalItemKeyException {
        // path is folder's ID
        if (getFolderService().folderExists(itemKey.getAssetId())) {
            return getFolder(itemKey);
        } else {
            return getAsset(itemKey);
        }
    }

    @Override
    public Item getItem(String path) throws PathNotFoundException {
        if (getFolderService().folderExists(path)) {
            return getFolder(path);
        } else {
            return getAsset(path);
        }
    }

    @Override
    public Folder getFolder(ItemKey folderKey) throws AssetNotFoundException, IllegalItemKeyException {
        // path is folder's ID
        return getFolder(folderKey.getAssetId());
    }

    @Override
    public Folder getRootFolder() {
        return CloudinaryFolder.getRoot(this);
    }

    @Override
    public Folder getFolder(String folderPath) throws PathNotFoundException {
        if (getFolderService().folderExists(folderPath)) {
            return new CloudinaryFolder(this, folderPath);
        } else {
            throw new PathNotFoundException(folderPath);
        }
    }

    @Override
    public Asset getAsset(ItemKey assetKey) throws AssetNotFoundException, IllegalItemKeyException {
        if (assetKey != null) {
            return new CloudinaryAsset(this, assetKey);
        }
        throw new AssetNotFoundException(assetKey);
    }

    @Override
    public Asset getAsset(String assetPath) throws PathNotFoundException {
        ApiObject assetInfo = getCloudinary().getAsset(assetPath);
        if (assetInfo != null) {
            return new CloudinaryAsset(this, new CloudinaryAssetDetails(assetInfo));
        }
        throw new PathNotFoundException(assetPath);
    }

    @Override
    public Iterator<Item> list(AssetQuery query) {
        return list(getCloudinary().search(query).getResults());
    }

    public Iterator<Item> list(final Iterator<ApiObject> iter) {
        return new Iterator<Item>() {
            @Override
            public Item next() {
                return new CloudinaryAsset(CloudinaryAssetProvider.this, new CloudinaryAssetDetails(iter.next()));
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
        };
    }
}
