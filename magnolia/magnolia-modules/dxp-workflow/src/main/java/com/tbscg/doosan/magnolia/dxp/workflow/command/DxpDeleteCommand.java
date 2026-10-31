package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.workflow.service.EventHandlingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.EventHandlingServiceConfiguration;
import info.magnolia.commands.impl.DeleteCommand;
import info.magnolia.context.Context;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

import static com.tbscg.doosan.magnolia.dxp.core.util.LoggingUtil.callAndLogTiming;

@Slf4j
public class DxpDeleteCommand extends DeleteCommand {

    private final EventHandlingService eventHandlingService;

    @Inject
    public DxpDeleteCommand(EventHandlingService eventHandlingService) {
        this.eventHandlingService = eventHandlingService;
    }

    @Override
    public boolean execute(Context ctx) throws Exception {
        handleDeletionEvent();
        return super.execute(ctx);
    }

    private void handleDeletionEvent() throws Exception {
        final String workspace = getRepository();
        if (EventHandlingServiceConfiguration.isEventHandleableWorkspace(workspace)) {
            callAndLogTiming(() -> eventHandlingService.handleEvent(ActionType.DELETE, workspace, getPath(), getUuid(), true), log, "Deletion event handling");
        }
    }
}
