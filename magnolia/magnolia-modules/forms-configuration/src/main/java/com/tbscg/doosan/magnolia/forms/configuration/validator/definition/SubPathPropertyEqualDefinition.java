package com.tbscg.doosan.magnolia.forms.configuration.validator.definition;

import com.tbscg.doosan.magnolia.forms.configuration.validator.SubPathPropertyEqual;

import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AvailabilityRuleType("subPathPropertyEqual")
public class SubPathPropertyEqualDefinition extends ConfiguredAvailabilityRuleDefinition {
    private String propertyName;
    private String propertyValue;
    private String subPath;

    public SubPathPropertyEqualDefinition() {
        super.setImplementationClass(SubPathPropertyEqual.class);
    }
}
