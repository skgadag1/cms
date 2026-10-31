package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.DuplicateNodeActionDefinition;

@ActionType("dxpDuplicateNodeAction")
public class DxpDuplicateNodeActionDefinition extends DuplicateNodeActionDefinition {

    public DxpDuplicateNodeActionDefinition() {
        setImplementationClass(DxpDuplicateNodeAction.class);
    }
}
