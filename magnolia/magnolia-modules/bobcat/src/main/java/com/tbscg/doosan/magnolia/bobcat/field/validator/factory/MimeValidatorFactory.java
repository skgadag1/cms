package com.tbscg.doosan.magnolia.bobcat.field.validator.factory;

import com.tbscg.doosan.magnolia.bobcat.field.validator.MimeValidatorDefinition;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import org.apache.commons.lang3.StringUtils;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;


public class MimeValidatorFactory extends AbstractFieldValidatorFactory<MimeValidatorDefinition, String> {

    public MimeValidatorFactory(MimeValidatorDefinition definition) {
        super(definition);
    }

    @Override
    public Validator<String> createValidator() {
        return (value, context) -> {
            String[] mimeValues = StringUtils.split(value, ",");
            int mimeValuesLength = mimeValues.length;
            for (int i = 0; i < mimeValuesLength; ++i) {
                String mimeType = mimeValues[i];
                try {
                    new MimeType(mimeType);
                } catch (MimeTypeParseException e) {
                    return ValidationResult.error(this.getI18nErrorMessage());
                }
            }
            return ValidationResult.ok();
        };
    }
}
