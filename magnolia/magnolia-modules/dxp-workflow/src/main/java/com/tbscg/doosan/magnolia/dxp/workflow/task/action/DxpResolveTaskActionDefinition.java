package com.tbscg.doosan.magnolia.dxp.workflow.task.action;

import info.magnolia.ui.admincentral.shellapp.pulse.task.action.ResolveTaskActionDefinition;

public class DxpResolveTaskActionDefinition extends ResolveTaskActionDefinition {
    public DxpResolveTaskActionDefinition() {
        this.setImplementationClass(DxpResolveTaskAction.class);
    }
}