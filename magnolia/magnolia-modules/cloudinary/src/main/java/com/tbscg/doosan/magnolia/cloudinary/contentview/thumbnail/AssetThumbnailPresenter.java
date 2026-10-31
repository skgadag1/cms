package com.tbscg.doosan.magnolia.cloudinary.contentview.thumbnail;

import com.vaadin.v7.data.Container;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnector;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.workbench.thumbnail.ThumbnailPresenter;
import info.magnolia.ui.workbench.thumbnail.ThumbnailView;

public class AssetThumbnailPresenter extends ThumbnailPresenter {

    private final ImageProvider imageProvider;

    public AssetThumbnailPresenter(ThumbnailView view, ImageProvider imageProvider, ComponentProvider componentProvider) {
        super(view, imageProvider, componentProvider);
        this.imageProvider = imageProvider;
    }

    @Override
    protected Container initializeContainer() {
        AssetContentConnector connector = ((AssetContentConnector) contentConnector);
        return new AssetThumbnailContainer(connector, imageProvider);
    }
}
