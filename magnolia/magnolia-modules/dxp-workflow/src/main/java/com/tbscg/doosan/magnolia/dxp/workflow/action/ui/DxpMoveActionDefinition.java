package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.MoveActionDefinition;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;

@ActionType("dxpMoveAction")
public class DxpMoveActionDefinition<T extends JcrNodeWrapper> extends MoveActionDefinition<T> {
    @Getter
    @Setter
    private Collection<String> allowedRoles = Collections.emptyList();

    public DxpMoveActionDefinition() {
        setImplementationClass(DxpMoveAction.class);
    }
}
