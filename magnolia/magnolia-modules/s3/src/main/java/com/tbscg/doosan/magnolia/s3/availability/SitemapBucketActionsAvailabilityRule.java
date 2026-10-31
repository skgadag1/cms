package com.tbscg.doosan.magnolia.s3.availability;

import info.magnolia.amazon.s3.AmazonS3IntegrationModule;
import info.magnolia.amazon.s3.BucketConfiguration;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

public abstract class SitemapBucketActionsAvailabilityRule extends AbstractAvailabilityRule {

    private final static String SITEMAPS_NAME = "sitemaps";
    protected final String sitemapBucketName;

    @Inject
    public SitemapBucketActionsAvailabilityRule(Provider<AmazonS3IntegrationModule> amazonS3IntegrationModuleProvider) {
        List<BucketConfiguration> bucketConfigurations = amazonS3IntegrationModuleProvider.get().getBucketConfigurations();
        sitemapBucketName = getBucketName(bucketConfigurations);
    }

    @Override
    protected abstract boolean isAvailableForItem(Object o);

    protected boolean isSitemapBucket(String path) {
        String bucketName = getRootName(path);
        return sitemapBucketName.equals(bucketName);
    }

    private String getRootName(String path) {
        String[] parts = path.split("/");
        return parts.length > 1 ? parts[1] : "";
    }

    private String getBucketName(List<BucketConfiguration> bucketConfigurations) {
        return bucketConfigurations.stream()
                .filter(bucketConfiguration -> SITEMAPS_NAME.equals(bucketConfiguration.getName()))
                .map(BucketConfiguration::getBucketName)
                .findFirst()
                .orElse("");
    }
}
