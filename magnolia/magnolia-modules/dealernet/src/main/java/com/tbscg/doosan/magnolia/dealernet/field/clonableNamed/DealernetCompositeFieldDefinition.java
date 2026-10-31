package com.tbscg.doosan.magnolia.dealernet.field.clonableNamed;

import info.magnolia.ui.field.CompositeFieldDefinition;
import info.magnolia.ui.field.ConfiguredSwitchableFieldDefinition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DealernetCompositeFieldDefinition<T> extends CompositeFieldDefinition<T> implements CloneableNamedField<DealernetCompositeFieldDefinition> {

    public DealernetCompositeFieldDefinition clone() {
        try {
            return (DealernetCompositeFieldDefinition) super.clone();
        } catch (CloneNotSupportedException e) {
            log.warn("Cannot clone ConfiguredSwitchableFieldDefinition", e);
            return null;
        }
    }
}
