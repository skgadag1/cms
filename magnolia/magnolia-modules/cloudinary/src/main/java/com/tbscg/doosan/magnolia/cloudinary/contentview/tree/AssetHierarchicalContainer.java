package com.tbscg.doosan.magnolia.cloudinary.contentview.tree;

import com.tbscg.doosan.magnolia.cloudinary.contentview.CloudinaryAssetContainer;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.vaadin.v7.data.Container;
import info.magnolia.dam.api.Folder;
import info.magnolia.dam.api.Item;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnector;

import java.util.*;

/**
 * Hierarchical extension of base asset container, used as a datasource in tree content view
 */
public class AssetHierarchicalContainer extends CloudinaryAssetContainer implements Container.Hierarchical {

    private static final long serialVersionUID = 1L;

    private List<ItemKey> rootItemKeys = new ArrayList<>();
    private Map<ItemKey, List<ItemKey>> children = new HashMap<>();

    public AssetHierarchicalContainer(AssetContentConnector connector) {
        super(connector);
    }

    @Override
    public void refresh() {
        rootItemKeys.clear();
        children.clear();
    }

    @Override
    public Collection<ItemKey> getChildren(Object itemId) {
        final List<ItemKey> result = new ArrayList<>();

        if (itemId instanceof ItemKey) {
            if (children.containsKey(itemId)) {
                result.addAll(children.get(itemId));
            } else {
                Item item = getAssetItem(itemId);
                if (item != null && item.isFolder()) {
                    Iterator<Item> iterator = ((Folder) item).getChildren();
                    while (iterator.hasNext()) {
                        Item childItem = iterator.next();
                        if (isItemVisible(childItem)) {
                            result.add(childItem.getItemKey());
                            getItem(childItem.getItemKey());
                        }
                    }
                    children.put((ItemKey) itemId, result);
                }
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public Collection<ItemKey> rootItemIds() {
        if (rootItemKeys.isEmpty()) {
            Iterator<Item> iterator = getAssetProvider().getRootFolder().getChildren();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (isItemVisible(item)) {
                    rootItemKeys.add(item.getItemKey());
                    getItem(item.getItemKey());
                }
            }
        }
        return Collections.unmodifiableCollection(rootItemKeys);
    }

    private boolean isItemVisible(Item item) {
        if (item instanceof CloudinaryAsset) {
            CloudinaryAsset asset = (CloudinaryAsset) item;
            return getFilters().stream().allMatch(filter -> filter.matches(asset));
        }
        return true;
    }

    @Override
    public ItemKey getParent(Object itemId) {
        try {
            if (!(itemId instanceof ItemKey)) {
            return null;
        }
        if (isRoot(itemId)) {
            return null;
        }
        info.magnolia.dam.api.Item item = getAssetItem(itemId);
            if (item == null || (item.isFolder() && ((Folder) item).isRoot()) || item.getParent() == null) {
                return null;
            }
            return item.getParent().getItemKey();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        info.magnolia.dam.api.Item item = getAssetItem(itemId);
        if (item != null && item.isFolder()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isRoot(Object itemId) {
        info.magnolia.dam.api.Item item = getAssetItem(itemId);
        if (item == null) {
            return true;
        }
        if (item.isAsset()) {
            return false;
        }
        return ((Folder) item).isRoot();
    }

    @Override
    public boolean hasChildren(Object itemId) {
        info.magnolia.dam.api.Item item = getAssetItem(itemId);
        if (item == null || item.isAsset()) {
            return false;
        }
        return ((Folder) item).getChildren().hasNext();
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'setParent()' operation");
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'setChildrenAllowed()' operation");
    }
}
