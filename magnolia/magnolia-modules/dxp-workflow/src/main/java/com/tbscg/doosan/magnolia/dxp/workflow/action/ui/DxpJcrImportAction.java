package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.JcrImportAction;
import info.magnolia.ui.contentapp.action.JcrImportActionDefinition;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.editor.FormView;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

@Slf4j
public class DxpJcrImportAction extends JcrImportAction {

    private final DxpComponentToSendAction<Node> componentToSendAction;

    @Inject
    @SuppressWarnings("deprecation")
    public DxpJcrImportAction(JcrImportActionDefinition definition,
                           CommandsManager commandsManager,
                           ValueContext<Node> valueContext,
                           Context context,
                           FormView<Node> form,
                           CloseHandler closeHandler,
                           AsyncActionExecutor asyncActionExecutor,
                           JcrDatasource datasource,
                           @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, commandsManager, valueContext, context, form, closeHandler, asyncActionExecutor, datasource);
        this.componentToSendAction = new DxpComponentToSendAction<>(eventBus, valueContext);
    }

    @Override
    public void execute() {
        super.execute();
        this.componentToSendAction.execute();
    }
}
