package com.tbscg.doosan.magnolia.dxp.core.compatibility.field.validator;

import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

/**
 * {@link ConfiguredFieldValidatorDefinition} that enables configuration
 * support for JCR property name and value.
 *
 * @author apuigescobar
 */
@Deprecated
public class PropertyFieldValidatorDefinition extends ConfiguredFieldValidatorDefinition {

    private String propertyName;
    private String propertyValue;
    private String workspace;

    public PropertyFieldValidatorDefinition() {
        super.setFactoryClass(PropertyFieldValidatorFactory.class);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
}
