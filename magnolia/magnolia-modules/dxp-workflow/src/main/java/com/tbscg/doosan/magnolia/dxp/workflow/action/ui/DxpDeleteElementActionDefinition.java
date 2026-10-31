package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.pages.app.detail.action.DeleteElementActionDefinition;

public class DxpDeleteElementActionDefinition extends DeleteElementActionDefinition {

    public DxpDeleteElementActionDefinition() {
        setImplementationClass(DxpDeleteElementAction.class);
    }
}
