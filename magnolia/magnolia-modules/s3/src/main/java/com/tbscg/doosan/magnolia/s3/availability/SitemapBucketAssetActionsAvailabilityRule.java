package com.tbscg.doosan.magnolia.s3.availability;

import info.magnolia.amazon.s3.AmazonS3IntegrationModule;
import info.magnolia.dam.api.ItemKey;
import org.apache.poi.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Provider;

public class SitemapBucketAssetActionsAvailabilityRule extends SitemapBucketActionsAvailabilityRule {

    SitemapBucketAssetActionsAvailabilityRuleDefinition definition;

    @Inject
    public SitemapBucketAssetActionsAvailabilityRule(SitemapBucketAssetActionsAvailabilityRuleDefinition definition, Provider<AmazonS3IntegrationModule> amazonS3IntegrationModuleProvider) {
        super(amazonS3IntegrationModuleProvider);
        this.definition = definition;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        String path = ((ItemKey) itemId).getAssetId();
        int objectDepth = StringUtil.countMatches(path, '/') - 1;
        return !isSitemapBucket(path) || (objectDepth == 2 && isAvailableSitemapFile(path));
    }

    protected boolean isAvailableSitemapFile(String path) {
        String fileName = getFileName(path);
        return !definition.getBlockAccessFilesList().contains(fileName);
    }

    private String getFileName(String path) {
        int lastIndex = path.lastIndexOf('/');
        return lastIndex >= 0 ? path.substring(lastIndex + 1) : path;
    }
}
