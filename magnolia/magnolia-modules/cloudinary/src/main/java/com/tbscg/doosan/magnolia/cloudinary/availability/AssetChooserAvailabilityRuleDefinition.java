package com.tbscg.doosan.magnolia.cloudinary.availability;

import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;

@AvailabilityRuleType("AssetChoserAvailabilityRule")
public class AssetChooserAvailabilityRuleDefinition extends ConfiguredAvailabilityRuleDefinition {
    public AssetChooserAvailabilityRuleDefinition() {
        this.setImplementationClass(AssetChooserAvailabilityRule.class);
    }
}
