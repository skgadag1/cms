package com.tbscg.doosan.magnolia.bobcat.customs.template;

import info.magnolia.pages.app.data.PageTemplateDataProvider;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.availability.rule.AbstractJcrRule;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

public class MultipleTemplateAvailabilityRule extends AbstractJcrRule<MultipleTemplateAvailabilityRuleDefinition> {
    private final PageTemplateDataProvider dataProvider;
    public MultipleTemplateAvailabilityRule(AvailabilityDefinition availabilityDefinition, MultipleTemplateAvailabilityRuleDefinition ruleDefinition, PageTemplateDataProvider dataProvider) {
        super(availabilityDefinition, ruleDefinition);
        this.dataProvider = dataProvider;
    }

    @Override
    protected boolean isAvailableForJcrItem(Item item) throws RepositoryException {
        return this.dataProvider.getItems().size() > 1;
    }
}
