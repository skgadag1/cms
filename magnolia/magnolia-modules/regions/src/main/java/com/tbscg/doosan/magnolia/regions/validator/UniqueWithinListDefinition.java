package com.tbscg.doosan.magnolia.regions.validator;

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

/**
 * Validator definition for ensuring no duplicate values within the same multifield list.
 */
@Getter
@Setter
@ValidatorType("uniqueWithinList")
public class UniqueWithinListDefinition extends ConfiguredFieldValidatorDefinition {
    
    private String errorMessage = "This hreflang locale is already used in this language";
    
    public UniqueWithinListDefinition() {
        super.setFactoryClass(UniqueWithinListFactory.class);
    }
}
