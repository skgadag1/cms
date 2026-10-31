package com.tbscg.doosan.magnolia.cloudinary.contentview.tree;

import com.vaadin.v7.data.Container;
import info.magnolia.dam.external.app.contentconnector.AssetContentConnector;
import info.magnolia.dam.external.app.contentview.list.AssetListPresenter;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.workbench.tree.TreeView;

import javax.inject.Inject;

public class AssetTreePresenter extends AssetListPresenter {

	@Inject
	public AssetTreePresenter(TreeView view, ComponentProvider componentProvider) {
		super(view, componentProvider);
	}

	@Override
	public Container initializeContainer() {
		AssetContentConnector contentConnector = (AssetContentConnector) this.contentConnector;
		container = getComponentProvider().newInstance(AssetHierarchicalContainer.class, contentConnector);
		configureContainer(getPresenterDefinition(), container);
		return container;
	}

	@Override
	public void expand(Object itemId) {
		view.expand(itemId);
	}
}
