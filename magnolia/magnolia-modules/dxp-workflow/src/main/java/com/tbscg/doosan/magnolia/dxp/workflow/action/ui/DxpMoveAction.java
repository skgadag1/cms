package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.datasources.RestrictedMoveItemsDatasource;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.MoveAction;
import info.magnolia.ui.contentapp.action.MoveActionDefinition;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.framework.overlay.ChooserController;

public class DxpMoveAction<T extends JcrNodeWrapper> extends MoveAction<T> {
    public DxpMoveAction(DxpMoveActionDefinition<T> definition, MoveActionDefinition<T> actionDefinition, ChooserController chooserController, Datasource<T> datasource, ValueContext<T> valueContext, AppDescriptor appDescriptor) {
        super(actionDefinition, chooserController, new RestrictedMoveItemsDatasource<>(datasource, definition.getAllowedRoles()), valueContext, appDescriptor);
    }
}
