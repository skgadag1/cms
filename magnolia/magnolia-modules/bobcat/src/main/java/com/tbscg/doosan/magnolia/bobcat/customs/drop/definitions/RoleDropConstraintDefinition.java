package com.tbscg.doosan.magnolia.bobcat.customs.drop.definitions;

import com.tbscg.doosan.magnolia.bobcat.customs.drop.implementations.RoleDropConstraint;
import info.magnolia.ui.contentapp.browser.drop.DropConstraintDefinition;
import info.magnolia.ui.contentapp.browser.drop.DropConstraintType;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@DropConstraintType("roleDropConstraint")
public class RoleDropConstraintDefinition extends DropConstraintDefinition {
    @Getter
    @Setter
    private Collection<String> allowedRoles;

    public RoleDropConstraintDefinition() {
        setImplementationClass(RoleDropConstraint.class);
    }
}
