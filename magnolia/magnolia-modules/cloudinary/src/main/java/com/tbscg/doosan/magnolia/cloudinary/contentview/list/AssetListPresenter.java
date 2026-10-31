package com.tbscg.doosan.magnolia.cloudinary.contentview.list;

import com.tbscg.doosan.magnolia.cloudinary.contentview.CloudinaryAssetContainer;
import info.magnolia.dam.api.AssetProvider;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnector;
import info.magnolia.dam.external.app.contentview.AbstractAssetContainer;
import info.magnolia.dam.external.app.contentview.AssetProviderAwarePresenter;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.container.AbstractContainer;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.list.ListPresenter;
import info.magnolia.ui.workbench.list.ListView;

import javax.inject.Inject;

import com.vaadin.v7.data.Container;

public class AssetListPresenter extends ListPresenter implements AssetProviderAwarePresenter {

    @Inject
    public AssetListPresenter(ListView view, ComponentProvider componentProvider) {
        super(view, componentProvider);
    }

    @Override
    public Container initializeContainer() {
        AssetContentConnector contentConnector = (AssetContentConnector) this.contentConnector;
        container = getComponentProvider().newInstance(CloudinaryAssetContainer.class, contentConnector);
        configureContainer(getPresenterDefinition(), container);
        return container;
    }

    protected void configureContainer(ContentPresenterDefinition presenterDefinition, Container container) {
        for (ColumnDefinition column : presenterDefinition.getColumns()) {
            String propertyId = column.getPropertyName() != null ? column.getPropertyName() : column.getName();
            container.addContainerProperty(propertyId, column.getType(), null);
        }
    }

    @Override
    public void setAssetProvider(AssetProvider assetProvider) {
        ((AbstractAssetContainer) container).setAssetProvider(assetProvider);
    }

    public CloudinaryAssetContainer getContainer() {
        return (CloudinaryAssetContainer) container;
    }
}
