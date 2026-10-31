package com.tbscg.doosan.magnolia.s3.availability;

import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import java.util.List;

@Getter
@Setter
@AvailabilityRuleType("sitemapBucketAssetActions")
public class SitemapBucketAssetActionsAvailabilityRuleDefinition extends ConfiguredAvailabilityRuleDefinition {

    private List<String> blockAccessFilesList;

    @Inject
    public SitemapBucketAssetActionsAvailabilityRuleDefinition() {
        this.setImplementationClass(SitemapBucketAssetActionsAvailabilityRule.class);
    }
}
