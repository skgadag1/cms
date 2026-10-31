package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.clipboard.PasteContentActionDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;

@ActionType("dxpPasteContentAction")
public class DxpPasteContentActionDefinition extends PasteContentActionDefinition {
    @Getter
    @Setter
    private Collection<String> allowedRoles = Collections.emptyList();


    @Getter
    @Setter
    private boolean sendComponentAction = true;

    public DxpPasteContentActionDefinition() {
        setImplementationClass(DxpPasteContentAction.class);
    }
}
