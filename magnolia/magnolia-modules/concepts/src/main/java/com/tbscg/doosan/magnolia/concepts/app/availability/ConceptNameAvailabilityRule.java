package com.tbscg.doosan.magnolia.concepts.app.availability;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRuleDefinition;
import info.magnolia.ui.availability.rule.AbstractJcrRule;

import javax.jcr.Item;

public class ConceptNameAvailabilityRule<T extends AvailabilityRuleDefinition> extends AbstractJcrRule<T> {
    public ConceptNameAvailabilityRule(AvailabilityDefinition availabilityDefinition, T ruleDefinition) {
        super(availabilityDefinition, ruleDefinition);
    }

    @Override
    protected boolean isAvailableForJcrItem(Item item) {
        final String path = NodeUtil.getPath(item);
        return !path.startsWith("/bobcat");
    }
}
