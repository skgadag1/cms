package com.tbscg.doosan.magnolia.bobcat.customs.lock;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.locking.api.ContentLockingManager;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.availability.rule.AbstractJcrRule;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.stream.Stream;

public class UnLockAvailabilityRule extends AbstractJcrRule<UnlockAvailabilityRuleDefinition> {
    private final ContentLockingManager lockingManager;

    public UnLockAvailabilityRule(AvailabilityDefinition availabilityDefinition, UnlockAvailabilityRuleDefinition ruleDefinition, ContentLockingManager lockingManager) {
        super(availabilityDefinition, ruleDefinition);
        this.lockingManager = lockingManager;
    }

    @Override
    protected boolean isAvailableForJcrItem(Item item) throws RepositoryException {
        User user = MgnlContext.getUser();
        Stream<String> roles = user.getAllRoles().stream();
        Collection<String> allowedRoles = getRuleDefinition().getAllowedRoles();
        boolean isAdmin = roles.anyMatch(allowedRoles::contains);
        if (isAdmin) {
            return true;
        }
        return item.isNode() && MgnlContext.getUser().getName().equals(this.lockingManager.getLockOwner((Node) item));
    }
}
