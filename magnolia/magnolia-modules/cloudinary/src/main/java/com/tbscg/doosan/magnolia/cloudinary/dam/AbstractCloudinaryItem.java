package com.tbscg.doosan.magnolia.cloudinary.dam;

import com.tbscg.doosan.magnolia.cloudinary.api.CloudinaryService;
import com.tbscg.doosan.magnolia.cloudinary.api.FolderService;

import info.magnolia.dam.api.AbstractItem;
import info.magnolia.dam.api.ItemKey;

public abstract class AbstractCloudinaryItem extends AbstractItem<CloudinaryAssetProvider> {

	private ItemKey key;

	public AbstractCloudinaryItem(CloudinaryAssetProvider assetProvider, ItemKey key) {
		super(assetProvider, key);
		this.key = key;
	}

	@Override
	public ItemKey getItemKey() {
		return this.key;
	}

	public void setItemKey(ItemKey key) {
		this.key = key;
	}

	protected FolderService getFolderService() {
		return getAssetProvider().getFolderService();
	}

	protected CloudinaryService getCloudinary() {
		return getAssetProvider().getCloudinary();
	}
}