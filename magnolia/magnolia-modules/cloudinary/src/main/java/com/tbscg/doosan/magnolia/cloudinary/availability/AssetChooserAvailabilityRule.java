package com.tbscg.doosan.magnolia.cloudinary.availability;

import com.tbscg.doosan.magnolia.cloudinary.api.FolderService;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;


public class AssetChooserAvailabilityRule extends AbstractAvailabilityRule {


    private final FolderService folderService;

    public AssetChooserAvailabilityRule(FolderService folderService) {
        this.folderService = folderService;
    }

    @Override
    protected boolean isAvailableForItem(Object itemID) {
        ItemKey itemKey = (ItemKey) itemID;
        return !this.folderService.folderExists(itemKey.getAssetId());
    }
}
