package com.tbscg.doosan.magnolia.concepts.action.ui.validators;

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidatorType("ChipChooserValidator")
public class ChipChooserValidatorDefinition extends ConfiguredFieldValidatorDefinition {
    public ChipChooserValidatorDefinition() {
        this.setFactoryClass(ChipChooserValidatorFactory.class);
    }
}
