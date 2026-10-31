package com.tbscg.doosan.magnolia.dxp.core.compatibility.field.validator;

import info.magnolia.ui.editor.validator.NodeNameFieldValidatorFactory;
import info.magnolia.ui.field.ValidatorType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidatorType("lowerCaseNodeNameValidator")
public class LowerCaseNodeNameValidatorDefinition extends info.magnolia.ui.editor.validator.NodeNameValidatorDefinition {
    public LowerCaseNodeNameValidatorDefinition() {
        setName("lowerCaseNodeNameValidator");
        setFactoryClass(NodeNameFieldValidatorFactory.class);
        setPattern("[^A-Z]+");
    }
}
