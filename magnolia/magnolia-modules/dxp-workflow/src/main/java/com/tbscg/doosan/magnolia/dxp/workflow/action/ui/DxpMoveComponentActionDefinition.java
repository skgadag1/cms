package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.pages.app.detail.action.MoveComponentActionDefinition;

public class DxpMoveComponentActionDefinition extends MoveComponentActionDefinition {

    public DxpMoveComponentActionDefinition() {
        setImplementationClass(DxpMoveComponentAction.class);
    }
}
