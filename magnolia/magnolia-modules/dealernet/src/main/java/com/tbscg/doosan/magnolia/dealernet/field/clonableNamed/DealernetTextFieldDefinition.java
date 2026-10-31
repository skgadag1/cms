package com.tbscg.doosan.magnolia.dealernet.field.clonableNamed;

import info.magnolia.ui.field.TextFieldDefinition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DealernetTextFieldDefinition extends TextFieldDefinition implements CloneableNamedField<DealernetTextFieldDefinition> {

    public DealernetTextFieldDefinition clone() {
        try {
            return (DealernetTextFieldDefinition) super.clone();
        } catch (CloneNotSupportedException e) {
            log.warn("Cannot clone TextFieldDefinition", e);
            return null;
        }
    }
}
