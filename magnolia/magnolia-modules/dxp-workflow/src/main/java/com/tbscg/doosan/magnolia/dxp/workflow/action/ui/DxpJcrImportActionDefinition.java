package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.JcrImportActionDefinition;

@ActionType("dxpJcrImportAction")
public class DxpJcrImportActionDefinition extends JcrImportActionDefinition {

    public DxpJcrImportActionDefinition() {
        setImplementationClass(DxpJcrImportAction.class);
    }
}
