package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.workflow.service.EventHandlingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.IndexingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.EventHandlingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import info.magnolia.context.Context;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.publishing.command.UnpublicationCommand;

import javax.inject.Inject;

import java.time.Instant;

import static com.tbscg.doosan.magnolia.dxp.core.util.LoggingUtil.callAndLogTiming;

/**
 * DXP unpublication command with Elasticsearch indexing. Implemented on top of default UnpublicationCommand.
 * Deindexing is executed after page is published. The deindexing process is only executed for pages in website workspace
 * and that belong to given list of allowed to index sites. Indexer sends the path of published page to DynamoDB table
 * that triggers an event to Lambda that will parse the page and send it to Elasticsearch to index.
 */
public class DxpUnpublicationCommand extends UnpublicationCommand {

    private final IndexingService indexingService;
    private final EventHandlingService eventHandlingService;

    @Inject
    public DxpUnpublicationCommand(final ComponentProvider componentProvider,
                                   final IndexingService indexingService,
                                   final EventHandlingService eventHandlingService) {
        super(componentProvider);
        this.indexingService = indexingService;
        this.eventHandlingService = eventHandlingService;
    }

    @Override
    public boolean execute(final Context context) throws Exception {
        final String publishTime = Instant.now().toString();
        defaultUnpublish(context);
        handleUnpublicationEvent();
        final String workspace = super.getRepository();
        return deindex(workspace, publishTime);
    }

    private boolean deindex(final String workspace, final String publishTime) throws Exception {
        return IndexingServiceConfiguration.isIndexableWorkspace(workspace)
                ? callAndLogTiming(() -> indexingService.deindex(workspace, getPath(), getUuid(), publishTime), log, "Deindex handling")
                : true;
    }

    private void defaultUnpublish(final Context context) throws Exception {
        callAndLogTiming(() -> super.execute(context), log, "Default unpublish handling");
    }

    private void handleUnpublicationEvent() throws Exception {
        final String workspace = getRepository();
        if (EventHandlingServiceConfiguration.isEventHandleableWorkspace(workspace)) {
            callAndLogTiming(() -> eventHandlingService.handleEvent(ActionType.UNPUBLISH, workspace, getPath(), getUuid(), true), log, "Unpublication event handling");
        }
    }

}
