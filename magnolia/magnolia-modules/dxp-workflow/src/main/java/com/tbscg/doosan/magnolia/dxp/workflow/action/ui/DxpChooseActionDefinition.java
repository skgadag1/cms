package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.ui.contentapp.action.ChooseActionDefinition;

public class DxpChooseActionDefinition extends ChooseActionDefinition {

    public DxpChooseActionDefinition() {
        setImplementationClass(DxpChooseAction.class);
    }
}
