//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.tbscg.doosan.magnolia.bobcat.customs.lock;


import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;

@AvailabilityRuleType("unlockRule")
public class UnlockAvailabilityRuleDefinition extends ConfiguredAvailabilityRuleDefinition {
    @Getter
    @Setter
    private Collection<String> allowedRoles = Collections.emptyList();

    public UnlockAvailabilityRuleDefinition() {
        this.setImplementationClass(UnLockAvailabilityRule.class);
    }
}
