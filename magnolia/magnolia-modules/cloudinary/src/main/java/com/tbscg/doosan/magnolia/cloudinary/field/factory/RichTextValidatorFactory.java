package com.tbscg.doosan.magnolia.cloudinary.field.factory;

import com.tbscg.doosan.magnolia.cloudinary.field.definition.RichTextValidatorDefinition;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import info.magnolia.context.WebContext;
import info.magnolia.ui.field.SafeHtmlValidatorFactory;


public class RichTextValidatorFactory extends SafeHtmlValidatorFactory {
    public boolean isTurnOn;

    public RichTextValidatorFactory(RichTextValidatorDefinition definition, WebContext context) {
        super(definition, context);
        this.isTurnOn = definition.isTurnOn();
    }

    @Override
    public Validator<String> createValidator() {
        return (value, context) -> isTurnOn ? ValidationResult.ok() : super.createValidator().apply(value, context);
    }
}
