package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.pages.app.detail.action.PasteComponentsActionDefinition;

public class DxpPasteComponentsActionDefinition extends PasteComponentsActionDefinition {

    public DxpPasteComponentsActionDefinition() {
        setImplementationClass(DxpPasteComponentsAction.class);
    }
}
