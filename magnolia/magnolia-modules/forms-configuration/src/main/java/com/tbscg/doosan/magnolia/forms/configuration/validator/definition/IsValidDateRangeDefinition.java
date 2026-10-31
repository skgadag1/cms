package com.tbscg.doosan.magnolia.forms.configuration.validator.definition;

import com.tbscg.doosan.magnolia.forms.configuration.validator.factory.IsValidDateRangeFactory;
import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidatorType("IsValidDateRange")
public class IsValidDateRangeDefinition extends ConfiguredFieldValidatorDefinition {
    private String startDateFieldName;
    private String endDateFieldName;
    private String period;

    public IsValidDateRangeDefinition() {
        super.setFactoryClass(IsValidDateRangeFactory.class);
    }
}
