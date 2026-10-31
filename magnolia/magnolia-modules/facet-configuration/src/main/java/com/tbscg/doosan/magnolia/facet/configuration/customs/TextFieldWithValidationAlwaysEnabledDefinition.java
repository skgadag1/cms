package com.tbscg.doosan.magnolia.facet.configuration.customs;

import info.magnolia.ui.field.FieldType;

import info.magnolia.ui.field.TextFieldDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FieldType("textFieldWithValidationAlwaysEnabled")
public class TextFieldWithValidationAlwaysEnabledDefinition extends TextFieldDefinition {

    public TextFieldWithValidationAlwaysEnabledDefinition() {
        setType(String.class);
        setFactoryClass(TextFieldWithValidationAlwaysEnabledFactory.class);
        setFieldBinderClass((Class) TextFieldWithValidationAlwaysEnabledBinder.class);
    }
}

