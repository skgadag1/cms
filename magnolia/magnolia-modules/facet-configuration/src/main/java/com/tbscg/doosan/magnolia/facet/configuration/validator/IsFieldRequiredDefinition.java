package com.tbscg.doosan.magnolia.facet.configuration.validator;

import info.magnolia.ui.field.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.field.ValidatorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidatorType("IsFacetIdFieldRequired")
public class IsFieldRequiredDefinition extends ConfiguredFieldValidatorDefinition {
    private String facetTypeFieldName;
    private String facetProviderFieldName;
    private String tagFacet;
    private String facetProvider;

    public IsFieldRequiredDefinition() {
        super.setFactoryClass(IsFieldRequiredFactory.class);
    }
}
