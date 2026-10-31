package com.tbscg.doosan.magnolia.cloudinary.dam;

import com.tbscg.doosan.magnolia.cloudinary.api.ApiObject;
import com.tbscg.doosan.magnolia.cloudinary.api.CloudinaryService;
import com.tbscg.doosan.magnolia.dxp.core.util.PathUtil;
import info.magnolia.dam.api.Folder;
import info.magnolia.dam.api.Item;
import info.magnolia.dam.api.ItemKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CloudinaryFolder extends AbstractCloudinaryItem implements Folder {

	public CloudinaryFolder(CloudinaryAssetProvider assetProvider, String path) {
		super(assetProvider, new ItemKey(assetProvider.getIdentifier(), path));
	}

	public static Folder getRoot(CloudinaryAssetProvider assetProvider) {
		return new CloudinaryFolder(assetProvider, PathUtil.ROOT_FOLDER_PATH);
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public boolean isRoot() {
		return PathUtil.ROOT_FOLDER_PATH.equals(getPath());
	}

	@Override
	public Iterator<Item> getChildren() {
		List<Item> results = new ArrayList<>();

		List<String> subFolders = getFolderService().getSubfolders(getPath());

		subFolders.forEach(folderName -> {
			String folderPath = PathUtil.getPath(getPath(), folderName);
			results.add(new CloudinaryFolder(getAssetProvider(), folderPath));
		});

		CloudinaryService cloudinary = getCloudinary();

		List<ApiObject> assets = cloudinary.getAssetsInFolder(getPath());

		assets.stream().map(CloudinaryAssetDetails::new).forEach(assetMetadata ->
			results.add(new CloudinaryAsset(getAssetProvider(), assetMetadata))
		);

		return results.iterator();
	}

	@Override
	public Item getItem(final String name) {
		String childPath = PathUtil.getPath(getPath(), name);

		if (getFolderService().folderExists(childPath)) {
			return getAssetProvider().getFolder(childPath);

		} else {
			return getAssetProvider().getAsset(childPath);
		}
	}

	@Override
	public String getPath() {
		return getItemKey().getAssetId();
	}

	@Override
	public String getName() {
		return PathUtil.getFileName(getPath());
	}

	@Override
	public Folder getParent() {
		if (isRoot()) {
			return null;

		} else {
			String parentPath = PathUtil.getParentPath(getPath());
			return getAssetProvider().getFolder(parentPath);
		}
	}
}
