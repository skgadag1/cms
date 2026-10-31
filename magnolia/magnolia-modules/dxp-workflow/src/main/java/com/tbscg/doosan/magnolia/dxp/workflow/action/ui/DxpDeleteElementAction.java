package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.pages.app.detail.PageEditorStatus;
import info.magnolia.pages.app.detail.action.DeleteElementAction;
import info.magnolia.pages.app.detail.action.DeleteElementActionDefinition;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.observation.DatasourceObservation;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

@Slf4j
public class DxpDeleteElementAction<T extends DeleteElementActionDefinition> extends DeleteElementAction<T> {

    private final DxpComponentToSendAction componentToSendAction;

    @Inject
    public DxpDeleteElementAction(T definition,
                                  ValueContext<Node> valueContext,
                                  DatasourceObservation.Manual datasourceObservation,
                                  PageEditorStatus pageEditorStatus,
                                  @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, valueContext, datasourceObservation, pageEditorStatus);
        this.componentToSendAction = new DxpComponentToSendAction(eventBus, valueContext);
    }

    @Override
    public void execute() {
        super.execute();
        this.componentToSendAction.execute();
    }
}
