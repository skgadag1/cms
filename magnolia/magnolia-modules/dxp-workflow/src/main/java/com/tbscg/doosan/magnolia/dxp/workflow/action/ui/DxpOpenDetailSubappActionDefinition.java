package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.OpenDetailSubappActionDefinition;

@ActionType("dxpOpenDetailSubappAction")
public class DxpOpenDetailSubappActionDefinition extends OpenDetailSubappActionDefinition {

    public DxpOpenDetailSubappActionDefinition() {
        setImplementationClass(DxpOpenDetailSubappAction.class);
    }
}
