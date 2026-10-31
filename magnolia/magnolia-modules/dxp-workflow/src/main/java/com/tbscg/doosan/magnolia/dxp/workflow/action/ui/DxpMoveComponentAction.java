package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.pages.app.detail.action.MoveComponentAction;
import info.magnolia.pages.app.detail.action.MoveComponentActionDefinition;
import info.magnolia.pages.app.detail.context.MoveComponentContext;
import info.magnolia.ui.ValueContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class DxpMoveComponentAction<T> extends MoveComponentAction<T> {

    private final DxpComponentToSendAction componentToSendAction;

    @Inject
    public DxpMoveComponentAction(MoveComponentActionDefinition definition,
                                  MoveComponentContext moveComponentContext,
                                  ValueContext<T> valueContext,
                                  @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, moveComponentContext, valueContext);
        this.componentToSendAction = new DxpComponentToSendAction(eventBus, valueContext);
    }

    @Override
    public void execute() {
        super.execute();
        this.componentToSendAction.execute();
    }
}
