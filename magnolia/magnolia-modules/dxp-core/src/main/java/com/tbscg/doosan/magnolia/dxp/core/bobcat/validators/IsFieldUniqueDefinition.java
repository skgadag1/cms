package com.tbscg.doosan.magnolia.dxp.core.bobcat.validators;

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidatorType("IsFacetFieldUnique")
public class IsFieldUniqueDefinition extends ConfiguredFieldValidatorDefinition {
    private String nodeType;
    private String fieldName;

    public IsFieldUniqueDefinition() {
        super.setFactoryClass(IsFieldUniqueFactory.class);
    }

}
