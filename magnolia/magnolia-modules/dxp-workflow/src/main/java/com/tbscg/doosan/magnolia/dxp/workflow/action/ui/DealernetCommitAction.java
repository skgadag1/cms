package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.CommitAction;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.observation.DatasourceObservation;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class DealernetCommitAction<T> extends CommitAction<T> {

    private final DxpComponentToSendAction componentToSendAction;

    @Inject
    public DealernetCommitAction(CommitActionDefinition definition, CloseHandler closeHandler,
                                 ValueContext<T> valueContext, EditorView<T> form, Datasource<T> datasource,
                                 DatasourceObservation.Manual datasourceObservation,
                                 @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation);
        this.componentToSendAction = new DxpComponentToSendAction(eventBus, valueContext);
    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        this.componentToSendAction.execute();
    }
}
