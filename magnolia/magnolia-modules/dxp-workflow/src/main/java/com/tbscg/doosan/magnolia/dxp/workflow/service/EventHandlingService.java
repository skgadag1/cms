package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.tbscg.doosan.magnolia.dxp.workflow.service.eventhandling.*;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.EventHandlingException;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.NAVIGATION_WORKSPACE;
import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.WEBSITE_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;

/**
 * Event Handling service responsible for sending events to AWS Event Bridge.
 */
public class EventHandlingService {

    private final Map<String, EventHandleable> handleableMap = new HashMap<>();
    private final DefaultEventHandlingService defaultEventHandlingService;

    @Inject
    public EventHandlingService(final PageEventHandlingService pageEventHandlingService,
                                final NavigationEventHandlingService navigationEventHandlingService,
                                final DefaultEventHandlingService defaultEventHandlingService,
                                final ProductEventHandlingService productEventHandlingService) {
        this.defaultEventHandlingService = defaultEventHandlingService;
        this.handleableMap.put(WEBSITE_WORKSPACE, pageEventHandlingService);
        this.handleableMap.put(NAVIGATION_WORKSPACE, navigationEventHandlingService);
        this.handleableMap.put(PRODUCTS_WORKSPACE, productEventHandlingService);
    }

    /**
     * Starts event handling process for given workspace and its path and, if recursive, all of its children.
     * Currently, supports website JCR workspaces.
     *
     * @param eventAction event to be handled
     * @param workspace   workspace that is allowed to be handled
     * @param path        path to the JCR node to be handled
     * @param uuid        UUID of the page to be handled
     * @param isRecursive flag indicating if publication process is recursive and if subpages should be handled
     * @return true if processed successfully, false otherwise
     * @throws com.tbscg.doosan.magnolia.dxp.workflow.service.exception.EventHandlingException if event handling cannot be completed
     */
    public boolean handleEvent(final String eventAction, final String workspace, final String path, final String uuid, final boolean isRecursive) throws EventHandlingException {
        return this.handleEvent(eventAction, workspace, path, uuid, isRecursive, Instant.now().getEpochSecond());
    }

    /**
     * Starts event handling process for given workspace and its path and, if recursive, all of its children.
     * Currently, supports website JCR workspaces.
     *
     * @param eventAction event to be handled
     * @param workspace   workspace that is allowed to be handled
     * @param path        path to the JCR node to be handled
     * @param uuid        UUID of the page to be handled
     * @param isRecursive flag indicating if publication process is recursive and if subpages should be handled
     * @param publishTime time of publication
     * @return true if processed successfully, false otherwise
     * @throws com.tbscg.doosan.magnolia.dxp.workflow.service.exception.EventHandlingException if event handling cannot be completed
     */
    public boolean handleEvent(final String eventAction, final String workspace, final String path, final String uuid, final boolean isRecursive, final long publishTime) throws EventHandlingException {
        return handleableMap.getOrDefault(workspace, defaultEventHandlingService).handleEvent(eventAction, path, uuid, isRecursive, publishTime, workspace);
    }
}
