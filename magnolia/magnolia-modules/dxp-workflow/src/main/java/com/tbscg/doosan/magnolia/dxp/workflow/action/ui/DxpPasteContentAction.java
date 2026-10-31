package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.validators.NodeValidator;
import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.validators.RegionalActionNodeValidator;
import com.vaadin.ui.Notification;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.contentapp.action.clipboard.PasteContentAction;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.framework.ContentClipboard;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class DxpPasteContentAction<T extends JcrNodeWrapper> extends PasteContentAction<T> {

    private final DxpComponentToSendAction<T> componentToSendAction;
    private final ContentClipboard<T> contentClipboard;
    private final ValueContext<T> valueContext;
    private final NodeValidator<T> pasteValidator;
    private final DxpPasteContentActionDefinition definition;

    @Inject
    public DxpPasteContentAction(DxpPasteContentActionDefinition definition,
                                 ValueContext<T> valueContext,
                                 ContentClipboard<T> contentClipboard,
                                 SimpleTranslator simpleTranslator,
                                 @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, valueContext, contentClipboard, simpleTranslator);
        this.definition = definition;
        this.pasteValidator = new RegionalActionNodeValidator<>(definition.getAllowedRoles());
        this.contentClipboard = contentClipboard;
        this.valueContext = valueContext;
        this.componentToSendAction = new DxpComponentToSendAction<>(eventBus, valueContext);
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (!this.pasteValidator.validate(this.contentClipboard.getContents(), this.valueContext.get())) {
            Notification.show("Only admins can COPY between regions", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
            return;
        }
        super.execute();

        if (!this.definition.isSendComponentAction()) {
            return;
        }
        this.componentToSendAction.execute();
    }
}
