package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.pages.app.detail.action.PasteComponentsAction;
import info.magnolia.pages.app.detail.action.PasteComponentsActionDefinition;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.framework.ContentClipboard;
import info.magnolia.ui.observation.DatasourceObservation;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

@Slf4j
public class DxpPasteComponentsAction<T> extends PasteComponentsAction {

    private final DxpComponentToSendAction componentToSendAction;

    @Inject
    public DxpPasteComponentsAction(PasteComponentsActionDefinition definition,
                                    ContentClipboard<T> contentClipboard,
                                    ValueContext<T> valueContext,
                                    SimpleTranslator simpleTranslator,
                                    DatasourceObservation.Manual datasourceObservation,
                                    @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, (ContentClipboard<Node>) contentClipboard, (ValueContext<Node>) valueContext, simpleTranslator, datasourceObservation);
        this.componentToSendAction = new DxpComponentToSendAction(eventBus, valueContext);
    }

    @Override
    public void execute() {
        super.execute();
        this.componentToSendAction.execute();
    }
}
