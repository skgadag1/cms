package com.tbscg.doosan.magnolia.dxp.workflow.service.eventhandling;

import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.core.Component;
import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionDeletedEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionPublishedEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionUnpublishedEvent;
import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.EventHandlingException;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.service.indexing.BasePageIndexingService;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.getPageComponents;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
public class PageEventHandlingService extends BasePageIndexingService implements EventHandleable {

    private final EventBus eventBus;

    protected static final String PAGE_EVENT_NOT_HANDLED_MSG = "PAGE EVENT NOT SEND TO EVENT BRIDGE";

    @Inject
    public PageEventHandlingService(final IndexingServiceConfiguration indexingServiceConfig,
                                    final RegionsService regionsService, DynamoDBService dynamoDBService,
                                    final @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(indexingServiceConfig, regionsService, dynamoDBService);
        this.eventBus = eventBus;
    }

    @Override
    public boolean handleEvent(final String eventAction,
                               final String pagePath,
                               final String pageUuid,
                               final boolean isRecursive,
                               final long publishTime,
                               final String workspace) throws IndexingException {
        return TracingUtil.callWithTracing(traceID -> {
            if (isBlank(pagePath) || isBlank(pageUuid)) {
                log.error("Cannot handle {} event, page UUID or path is empty", eventAction);
                throw new EventHandlingException(this.handleExceptionMessage("Page UUID or path is empty"));
            }
            log.info("Handling {} event for page for: {} (recursive: {})", eventAction, pagePath, isRecursive);
            final List<String> indexableSites = this.getIndexableSites();
            final SitePagePath rootSitePagePath = new SitePagePath(pagePath);
            if (indexableSites.contains(rootSitePagePath.getSite())) {
                final List<PageNode> pagesToIndex = this.getPagesToProcess(pagePath, pageUuid, rootSitePagePath, isRecursive);
                log.info("Page event scope summary: path={}, recursive={}, pages={}", pagePath, isRecursive, pagesToIndex.size());
                for (final PageNode page : pagesToIndex) {
                    this.runEventProcessing(eventAction, page, publishTime);
                }
            }
            return true;
        });
    }

    private void runEventProcessing(final String eventAction, final PageNode page, final long lastModified) {
        final String pagePath = page.path().toString();
        final List<Component> components = getPageComponents(pagePath, getWorkspace());
        final List<String> customLinks = page.getCustomLinks();
        final String site = page.path().getSite();
        switch (eventAction) {
            case ActionType.PUBLISH ->
                this.eventBus.fireEvent(new DxpActionPublishedEvent(page.uuid(), EMPTY, page.parentUuid(), EMPTY, pagePath, customLinks, page.nodeType(), getWorkspace(), lastModified, components, site));
            case ActionType.UNPUBLISH ->
                this.eventBus.fireEvent(new DxpActionUnpublishedEvent(page.uuid(), EMPTY, page.parentUuid(), EMPTY, pagePath, customLinks, page.nodeType(), getWorkspace(), lastModified, components, site));
            case ActionType.DELETE -> {
                this.eventBus.fireEvent(new DxpActionUnpublishedEvent(page.uuid(), EMPTY, page.parentUuid(), EMPTY, pagePath, customLinks, page.nodeType(), getWorkspace(), lastModified, components, site));
                this.eventBus.fireEvent(new DxpActionDeletedEvent(page.uuid(), EMPTY, page.parentUuid(), EMPTY, pagePath, customLinks, page.nodeType(), getWorkspace(), lastModified, site));
            }
            default -> log.warn("Not recognised event type {}. Event handling won't be processed", eventAction);
        }
    }

    @Override
    protected String getExceptionMessageTitle() {
        return PAGE_EVENT_NOT_HANDLED_MSG;
    }
}
