package com.tbscg.doosan.magnolia.s3.availability;

import info.magnolia.amazon.s3.AmazonS3IntegrationModule;
import info.magnolia.dam.api.ItemKey;

import javax.inject.Provider;

public class SitemapBucketFolderActionsAvailabilityRule extends SitemapBucketActionsAvailabilityRule {

    public SitemapBucketFolderActionsAvailabilityRule(Provider<AmazonS3IntegrationModule> amazonS3IntegrationModuleProvider) {
        super(amazonS3IntegrationModuleProvider);
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        String path = ((ItemKey) itemId).getAssetId();
        return !isSitemapBucket(path);
    }
}
