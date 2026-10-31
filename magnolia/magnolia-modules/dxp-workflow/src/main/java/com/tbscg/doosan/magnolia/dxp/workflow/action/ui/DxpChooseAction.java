package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.contentapp.action.ChooseAction;
import info.magnolia.ui.contentapp.action.ChooseActionDefinition;
import info.magnolia.ui.framework.overlay.ChooserController;

import javax.inject.Inject;
import javax.inject.Named;

public class DxpChooseAction<T> extends ChooseAction {

    private final DxpComponentToSendAction componentToSendAction;

    @Inject
    public DxpChooseAction(ChooseActionDefinition definition, CloseHandler closeHandler,
                           ChooserController.ChooseHandler selectionHandler,
                           @Named(SystemEventBus.NAME) EventBus eventBus, ValueContext<T> valueContext) {
        super(definition, closeHandler, selectionHandler);
        this.componentToSendAction = new DxpComponentToSendAction(eventBus, valueContext);
    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        componentToSendAction.execute();
    }
}
