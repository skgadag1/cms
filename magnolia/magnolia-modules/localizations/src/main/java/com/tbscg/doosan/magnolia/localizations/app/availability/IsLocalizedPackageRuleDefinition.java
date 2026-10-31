package com.tbscg.doosan.magnolia.localizations.app.availability;

import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;

/**
 * Modern availability rule definition for checking if a node is a localized package.
 * 
 * @author modernization
 */
@AvailabilityRuleType("isLocalizedPackage")
public class IsLocalizedPackageRuleDefinition extends ConfiguredAvailabilityRuleDefinition {
    
    public IsLocalizedPackageRuleDefinition() {
        this.setImplementationClass(IsLocalizedPackageRule.class);
    }
}

