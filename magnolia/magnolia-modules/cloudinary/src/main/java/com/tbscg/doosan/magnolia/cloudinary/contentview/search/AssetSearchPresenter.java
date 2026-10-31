package com.tbscg.doosan.magnolia.cloudinary.contentview.search;

import com.tbscg.doosan.magnolia.cloudinary.contentview.list.AssetListPresenter;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.list.ListView;
import info.magnolia.ui.workbench.search.SearchPresenter;
import info.magnolia.ui.workbench.search.SearchView;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AssetSearchPresenter extends SearchPresenter {

    private final AssetListPresenter presenter;

    @Inject
    public AssetSearchPresenter(SearchView view, ComponentProvider componentProvider) {
        super(view, componentProvider);
        this.presenter = new AssetListPresenter(view, componentProvider);
    }

    public void search(String fulltextExpr) {
        presenter.getContainer().setSearchQuery(fulltextExpr);
        refresh();
    }

    public void clear() {
        presenter.getContainer().setSearchQuery(null);
        refresh();
    }

    @Override
    public ListView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName, ContentConnector contentConnector) {
        return presenter.start(workbenchDefinition, eventBus, viewTypeName, contentConnector);
    }

    @Override
    public void select(List<Object> itemIds) {
        presenter.select(itemIds);
    }

    @Override
    public void refresh() {
        presenter.refresh();
    }

    @Override
    protected Container initializeContainer() {
        return presenter.initializeContainer();
    }

    @Override
    public String getIcon(Item item) {
        return presenter.getIcon(item);
    }

    @Override
    public List<Object> getSelectedItemIds() {
        return presenter.getSelectedItemIds();
    }

    @Override
    public Object getSelectedItemId() {
        return presenter.getSelectedItemId();
    }

    @Override
    public void setSelectedItemIds(List<Object> selectedItemIds) {
        presenter.setSelectedItemIds(selectedItemIds);
    }

    @Override
    public void onItemSelection(Set<Object> itemIds) {
        presenter.onItemSelection(itemIds);
    }

    @Override
    public void onDoubleClick(Object itemId) {
        presenter.onDoubleClick(itemId);
    }

    @Override
    public void onRightClick(Object itemId, int clickX, int clickY) {
        presenter.onRightClick(itemId, clickX, clickY);
    }

    @Override
    public void onShortcutKey(int keyCode, int[] modifierKeys) {
        presenter.onShortcutKey(keyCode, modifierKeys);
    }

    @Override
    public void expand(Object itemId) {
        presenter.expand(itemId);
    }

    /* unused methods: */
    @Override
    protected AbstractJcrContainer createContainer() {
        throw new UnsupportedOperationException("createContainer method should not be called for AssetSearchPresenter");
    }

    @Override
    protected void configureContainer(ContentPresenterDefinition presenterDefinition, Container container) {
        throw new UnsupportedOperationException("configureContainer method should not be called for AssetSearchPresenter");
    }

    @Override
    protected ComponentProvider getComponentProvider() {
        throw new UnsupportedOperationException("getComponentProvider method should not be called for AssetSearchPresenter");
    }

    @Override
    protected Iterator<ColumnDefinition> getColumnsIterator() {
        throw new UnsupportedOperationException("getColumnsIterator method should not be called for AssetSearchPresenter");
    }

    @Override
    protected List<ColumnDefinition> getAvailableColumns(List<ColumnDefinition> allColumns) {
        throw new UnsupportedOperationException("getAvailableColumns method should not be called for AssetSearchPresenter");
    }

    @Override
    protected ContentPresenterDefinition getPresenterDefinition() {
        throw new UnsupportedOperationException("getPresenterDefinition method should not be called for AssetSearchPresenter");
    }
}
