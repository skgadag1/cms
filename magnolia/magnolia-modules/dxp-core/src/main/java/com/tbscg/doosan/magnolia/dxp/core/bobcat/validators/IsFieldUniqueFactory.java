package com.tbscg.doosan.magnolia.dxp.core.bobcat.validators;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.vaadin.data.*;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.*;


@Slf4j
public class IsFieldUniqueFactory extends AbstractFieldValidatorFactory<IsFieldUniqueDefinition, String> {

    private final String nodeType;
    private final info.magnolia.ui.ValueContext<Item> valueContext;
    private final String fieldName;

    @Inject
    public IsFieldUniqueFactory(IsFieldUniqueDefinition definition, info.magnolia.ui.ValueContext<Item> valueContext) {
        super(definition);
        this.valueContext = valueContext;
        this.fieldName = definition.getFieldName();
        this.nodeType = definition.getNodeType();
    }

    @Override
    public Validator<String> createValidator() {
        return (value, ctx) -> {
            try {
                if (!NodeUtil.isNodePropertyUniqueInParent(getCurrentNode(), fieldName, value, nodeType, true)) {
                    return ValidationResult.error("This field must be unique");
                }
                return ValidationResult.ok();
            } catch (Exception e) {
                return ValidationResult.error("Contact with administrator, something went wrong");
            }
        };
    }

    private Node getCurrentNode(){
        return (Node) valueContext.getSingleOrThrow();
    }
}
