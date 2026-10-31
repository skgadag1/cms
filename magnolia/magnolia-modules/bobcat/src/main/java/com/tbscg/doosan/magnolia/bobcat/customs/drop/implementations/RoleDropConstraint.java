package com.tbscg.doosan.magnolia.bobcat.customs.drop.implementations;

import com.tbscg.doosan.magnolia.bobcat.customs.drop.definitions.RoleDropConstraintDefinition;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.ui.Notification;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.contentapp.browser.drop.AbstractDropConstraint;

import javax.inject.Inject;
import javax.jcr.Item;
import java.util.Collection;
import java.util.stream.Stream;

public class RoleDropConstraint extends AbstractDropConstraint<RoleDropConstraintDefinition, Item> {
    @Inject
    protected RoleDropConstraint(RoleDropConstraintDefinition definition) {
        super(definition);
    }

    @Override
    public boolean isAllowedAt(Item item, Item t1, DropLocation dropLocation) {
        User user = MgnlContext.getUser();
        Stream<String> roles = user.getAllRoles().stream();
        Collection<String> allowedRoles = getDefinition().getAllowedRoles();
        boolean isAllowed = roles.anyMatch(allowedRoles::contains);

        if(!isAllowed) {
            notifyUser();
        }

        return isAllowed;
    }

    public void notifyUser() {
        Notification.show("Only admin can drag&drop pages", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
    }
}
