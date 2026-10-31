package com.tbscg.doosan.magnolia.cloudinary.contentview;

import com.tbscg.doosan.magnolia.cloudinary.api.SearchResult;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.CustomChooseDialogCallback;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.FilteredAssetContentConnectorDefinition;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.filter.AssetFilenameFilter;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.filter.AssetFilterDefinition;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetProvider;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.ContainerHelpers;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.MethodProperty;
import info.magnolia.dam.api.AssetProvider;
import info.magnolia.dam.api.AssetQuery;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnector;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnectorDefinition;
import info.magnolia.ui.workbench.container.AbstractContainer;
import info.magnolia.ui.workbench.container.Refreshable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base datasource for assets used in asset browser/chooser list content view.
 * Optimized for Cloudinary integration.
 * Supports filtering, lazy loading and searching.
 */
public class CloudinaryAssetContainer extends AbstractContainer implements Container.Indexed, Refreshable, Container.ItemSetChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryAssetContainer.class);

    public static final int DEFAULT_PAGE_LENGTH = 300;

    private int pageSize = DEFAULT_PAGE_LENGTH;

    private final AssetProvider assetProvider;
    private final AssetContentConnectorDefinition contentConnectorDefinition;

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    private final Map<Integer, ItemKey> itemIndexes = new HashMap<>();

    private int size = 0;

    private Iterator<info.magnolia.dam.api.Item> assetIterator;

    private List<AssetFilterDefinition> filters = Collections.emptyList();

    private String searchQuery;

    @Inject
    public CloudinaryAssetContainer(AssetContentConnector connector) {
        this.assetProvider = connector.getAssetProvider();
        this.contentConnectorDefinition = connector.getContentConnectorDefinition();
        initFilters();
    }

    private void initFilters() {
        if (contentConnectorDefinition instanceof FilteredAssetContentConnectorDefinition) {
            FilteredAssetContentConnectorDefinition filteredDef = (FilteredAssetContentConnectorDefinition) contentConnectorDefinition;
            this.filters = filteredDef.getFilters();
        }
    }

    public AssetProvider getAssetProvider() {
        return this.assetProvider;
    }

    public List<AssetFilterDefinition> getFilters() {
        return filters;
    }

    /* {@link Container} interface */

    @Override
    public int size() {
        return size;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    public BeanItem<info.magnolia.dam.api.Item> getItem(Object itemId) {
        if (itemId instanceof ItemKey && ItemKey.isValid(((ItemKey) itemId).asString())) {
            try {
                info.magnolia.dam.api.Item item = getAssetProvider().getItem((ItemKey) itemId);
                return new BeanItem<info.magnolia.dam.api.Item>(item);
            } catch (AssetProvider.AssetNotFoundException e) {
                log.error("Failed to retrieve item with given id: " + e.getMessage(), e);
            } catch (AssetProvider.IllegalItemKeyException e) {
                log.error("Failed to handle item id: " + e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public boolean containsId(Object itemId) {
        return getItem(itemId) != null;
    }

    @Override
    public Property<?> getContainerProperty(Object itemId, Object propertyId) {
        info.magnolia.dam.api.Item item = getAssetItem(itemId);
        if (item == null) {
            return null;
        }
        return new MethodProperty<Object>(item, (String) propertyId);
    }

    @Override
    public Collection<String> getItemIds() {
        throw new UnsupportedOperationException("Asset container does not support 'getItemIds()' operation");
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'addItem(Object itemId)' operation");
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'addItem()' operation");
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'removeAllItems()' operation");
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'removeItem(Object itemId)' operation");
    }

    /* {@link Refreshable} interface */

    @Override
    public void refresh() {
        resetIterator();
        itemIndexes.clear();
        fireItemSetChange();
    }

    private void resetIterator() {
        log.debug("iterator reset");
        this.size = 0;
        if (assetProvider instanceof CloudinaryAssetProvider) {
            CloudinaryAssetProvider cloudinaryProvider = (CloudinaryAssetProvider) assetProvider;
            AssetQuery query = constructQuery(getPageSize());
            SearchResult result = cloudinaryProvider.getCloudinary().search(query);
            this.assetIterator = cloudinaryProvider.list(result.getResults());
            this.size = result.getTotalCount();
            log.debug("size updated: " + size);
        }
    }

    protected AssetQuery constructQuery(int pageSize) {
        AssetQuery.Builder builder = new AssetQuery.Builder();
        if (pageSize > 0) {
            builder.withMaxResults(pageSize);
        }
        List<AssetFilterDefinition> filters = new ArrayList<>();
        filters.addAll(getFilters());
        if (StringUtils.isNotBlank(this.searchQuery)) {
            String[] split = this.searchQuery.split("\\s+");
            for (String token : split) {
                filters.add(new AssetFilenameFilter(token));
            }
        }
        if (!filters.isEmpty()) {
            builder.withAdditionalQueryStatement(filters.stream()
                    .map(AssetFilterDefinition::getAssetQueryExpression)
                    .collect(Collectors.joining(" AND ")));
        }
        return builder.build();
    }

    protected info.magnolia.dam.api.Item getAssetItem(Object itemKey) {
        if (!ItemKey.isValid(((ItemKey) itemKey).asString())) {
            return null;
        }
        try {
            return getAssetProvider().getItem((ItemKey) itemKey);
        } catch (AssetProvider.AssetNotFoundException e) {
            log.debug("Failed to retrieve item with given id: " + e.getMessage(), e);
        } catch (AssetProvider.IllegalItemKeyException e) {
            log.debug("Failed to handle item id: " + e.getMessage(), e);
        }
        return null;
    }

    private void loadItemsUpToIndex(int index) {
        if (itemIndexes.containsKey(index)) {
            return;
        }
        log.trace("loading item indexes cache up to {}", index);
        Iterator<info.magnolia.dam.api.Item> iterator = getAssetIterator();
        while (!itemIndexes.containsKey(index) && iterator.hasNext()) {
            itemIndexes.put(itemIndexes.size(), iterator.next().getItemKey());
        }
    }

    private Iterator<info.magnolia.dam.api.Item> getAssetIterator() {
        if (this.assetIterator == null) {
            resetIterator();
        }
        return assetIterator;
    }

    /* {@link Container.Indexed} interface */

    @Override
    public int indexOfId(Object itemId) {
        if (!containsId(itemId)) {
            return -1;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            if (getIdByIndex(i).equals(itemId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemKey getIdByIndex(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        if (itemIndexes.containsKey(index)) {
            return itemIndexes.get(index);
        }
        loadItemsUpToIndex(index);
        return itemIndexes.get(index);
    }

    @Override
    public List<?> getItemIds(int startIndex, int numberOfItems) {
        return new LazyItemIdList(startIndex, numberOfItems, this);
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'addItemAt(int index, newItemId)' operation");
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'addItemAt(int index)' operation");
    }

    /* {@link Container.Ordered} interface */

    @Override
    public Object nextItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) + 1);
    }

    @Override
    public Object prevItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) - 1);
    }

    @Override
    public Object firstItemId() {
        if (size == 0) {
            return null;
        }
        if (!itemIndexes.containsKey(0)) {
            loadItemsUpToIndex(0);
        }
        return itemIndexes.get(0);
    }

    @Override
    public Object lastItemId() {
        final int lastIx = size() - 1;
        if (!itemIndexes.containsKey(lastIx)) {
            loadItemsUpToIndex(lastIx);
        }
        return itemIndexes.get(lastIx);
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return firstItemId().equals(itemId);
    }

    @Override
    public boolean isLastId(Object itemId) {
        return lastItemId().equals(itemId);
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'addItemAfter(Object previousItemId, Object newItemId)' operation");
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Asset container does not support 'addItemAfter(Object previousItemId)' operation");
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /* {@link Container.ItemSetChangeNotifier} interface */

    @Override
    public void addItemSetChangeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedHashSet<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        addItemSetChangeListener(listener);
    }

    @Override
    public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
            if (itemSetChangeListeners.isEmpty()) {
                itemSetChangeListeners = null;
            }
        }
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        removeItemSetChangeListener(listener);
    }

    public void fireItemSetChange() {
        if (itemSetChangeListeners != null && !itemSetChangeListeners.isEmpty()) {
            final Object[] l = itemSetChangeListeners.toArray();
            BaseItemSetChangeEvent event = new BaseItemSetChangeEvent(this);
            for (Object listener : l) {
                ((ItemSetChangeListener) listener).containerItemSetChange(event);
            }
        }
    }

    /**
     * Basic itemSet change event.
     */
    private static class BaseItemSetChangeEvent extends EventObject implements Container.ItemSetChangeEvent, Serializable {

        private static final long serialVersionUID = 1L;

        protected BaseItemSetChangeEvent(Container source) {
            super(source);
        }

        @Override
        public Container getContainer() {
            return (Container) getSource();
        }
    }

}
