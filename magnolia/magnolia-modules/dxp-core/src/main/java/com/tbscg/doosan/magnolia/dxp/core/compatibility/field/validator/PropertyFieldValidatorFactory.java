package com.tbscg.doosan.magnolia.dxp.core.compatibility.field.validator;

import com.vaadin.v7.data.Validator;
import info.magnolia.ui.form.validator.factory.AbstractFieldValidatorFactory;

import javax.inject.Inject;

/**
 * {@link info.magnolia.ui.form.validator.factory.FieldValidatorFactory} that instantiates property validators
 * compatible with any JCR property type.
 *
 * @author apuigescobar
 */
@Deprecated
public class PropertyFieldValidatorFactory extends AbstractFieldValidatorFactory<PropertyFieldValidatorDefinition> {

    @Inject
    public PropertyFieldValidatorFactory(PropertyFieldValidatorDefinition definition) {
        super(definition);
    }

    @Override
    public Validator createValidator() {
        return new PropertyFieldValidator(this.definition.getPropertyName(), this.definition.getPropertyValue(),
                this.definition.getWorkspace(), this.definition.getErrorMessage());
    }
}
