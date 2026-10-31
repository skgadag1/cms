package com.tbscg.doosan.magnolia.bobcat.customs.template;


import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;

@AvailabilityRuleType("hasMultipleTemplates")
public class MultipleTemplateAvailabilityRuleDefinition extends ConfiguredAvailabilityRuleDefinition {
    
    public MultipleTemplateAvailabilityRuleDefinition() {
        this.setImplementationClass(MultipleTemplateAvailabilityRule.class);
    }
}
