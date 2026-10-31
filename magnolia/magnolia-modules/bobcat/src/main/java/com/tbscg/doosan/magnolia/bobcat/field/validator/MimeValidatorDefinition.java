package com.tbscg.doosan.magnolia.bobcat.field.validator;

import com.tbscg.doosan.magnolia.bobcat.field.validator.factory.MimeValidatorFactory;
import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;

@ValidatorType("mimeValidator")
public class MimeValidatorDefinition extends ConfiguredFieldValidatorDefinition {
    public MimeValidatorDefinition() {
        this.setFactoryClass(MimeValidatorFactory.class);
    }
}
