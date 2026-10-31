package com.tbscg.doosan.magnolia.regions.validator;

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

/**
 * Validator definition for ensuring hreflangLocale values are unique within a website.
 * Allows the same hreflangLocale across different dxp:website nodes.
 */
@Getter
@Setter
@ValidatorType("uniqueHreflangLocale")
public class UniqueHreflangLocaleDefinition extends ConfiguredFieldValidatorDefinition {
    private String errorMessage = "This hreflang locale is already used in another language within this website";

    public UniqueHreflangLocaleDefinition() {
        super.setFactoryClass(UniqueHreflangLocaleFactory.class);
    }
}
