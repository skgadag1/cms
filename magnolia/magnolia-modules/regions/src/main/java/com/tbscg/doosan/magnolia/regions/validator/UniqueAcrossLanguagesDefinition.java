package com.tbscg.doosan.magnolia.regions.validator;

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

/**
 * Validator definition for ensuring hreflangLocale values are unique across all languages in a website.
 */
@Getter
@Setter
@ValidatorType("uniqueAcrossLanguages")
public class UniqueAcrossLanguagesDefinition extends ConfiguredFieldValidatorDefinition {
    
    private String errorMessage = "This hreflang locale is already used in another language within this website";
    
    public UniqueAcrossLanguagesDefinition() {
        super.setFactoryClass(UniqueAcrossLanguagesFactory.class);
    }
}
