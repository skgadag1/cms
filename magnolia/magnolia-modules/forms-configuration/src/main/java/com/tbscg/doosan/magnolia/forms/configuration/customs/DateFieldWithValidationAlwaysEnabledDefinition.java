package com.tbscg.doosan.magnolia.forms.configuration.customs;

import info.magnolia.ui.field.DateFieldDefinition;
import info.magnolia.ui.field.FieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@FieldType("dateFieldWithValidationAlwaysEnabled")
public class DateFieldWithValidationAlwaysEnabledDefinition extends DateFieldDefinition {

    public DateFieldWithValidationAlwaysEnabledDefinition() {
        setType(Date.class);
        setFieldBinderClass((Class) DateFieldWithValidationAlwaysEnabledBinder.class);
    }
}
