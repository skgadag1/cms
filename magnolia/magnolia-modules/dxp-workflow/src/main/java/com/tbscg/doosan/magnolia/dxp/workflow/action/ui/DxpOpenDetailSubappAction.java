package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.action.OpenDetailSubappAction;
import info.magnolia.ui.contentapp.action.OpenDetailSubappActionDefinition;
import info.magnolia.ui.datasource.ItemResolver;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class DxpOpenDetailSubappAction<T> extends OpenDetailSubappAction<T> {

    private final DxpComponentToSendAction componentToSendAction;

    @Inject
    protected DxpOpenDetailSubappAction(
            OpenDetailSubappActionDefinition definition,
            ValueContext<T> valueContext,
            LocationController locationController,
            ItemResolver<T> itemResolver,
            @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, valueContext, locationController, itemResolver);
        this.componentToSendAction = new DxpComponentToSendAction(eventBus, valueContext);
    }

    @Override
    public void execute() {
        super.execute();
        this.componentToSendAction.execute();
    }
}
