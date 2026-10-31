package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationWorkset;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationRequestContext;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Indexing service responsible for storing page indexing events in DynamoDB table.
 */
@Slf4j
public abstract class BasePageIndexingService extends BaseIndexingService {
    private static final String PAGE_SKIPPED_MSG = "PAGE SKIPPED";

    public BasePageIndexingService(final IndexingServiceConfiguration indexingServiceConfig,
                                   final RegionsService regionsService, DynamoDBService dynamoDBService) throws IndexingException {
        super(indexingServiceConfig, regionsService, dynamoDBService);
    }

    @Override
    public String getWorkspace() {
        return WorkflowConstant.WEBSITE_WORKSPACE;
    }

    protected List<PageNode> getPagesToProcess(final String pagePath,
                                               final String pageUuid,
                                               final SitePagePath rootSitePagePath,
                                               boolean isRecursive) throws IndexingException {
        return PublicationRequestContext.current()
                .map(request -> request.getOrCreatePagesToProcess(pagePath, pageUuid, isRecursive,
                        () -> buildPagesToProcess(pagePath, pageUuid, rootSitePagePath, isRecursive)))
                .orElseGet(() -> buildPagesToProcess(pagePath, pageUuid, rootSitePagePath, isRecursive));
    }

    private List<PageNode> buildPagesToProcess(final String pagePath,
                                               final String pageUuid,
                                               final SitePagePath rootSitePagePath,
                                               final boolean isRecursive) {
        final PublicationWorkset workSet = WorkflowUtil.collectPublicationWorkset(pagePath, WorkflowConstant.WEBSITE_WORKSPACE, isRecursive);
        if (workSet.items().isEmpty()) {
            return List.of();
        }
        final List<PageNode> pagesToProcess = new ArrayList<>(workSet.size());
        final PublicationItem rootItem = workSet.items().get(0);
        final String resolvedUuid = rootItem.uuid();
        if (resolvedUuid != null && !resolvedUuid.equals(pageUuid)) {
            log.warn("Root page UUID mismatch: caller supplied '{}' but workset found '{}' for path={}. Using workset uuid.",
                    pageUuid, resolvedUuid, pagePath);
        }
        pagesToProcess.add(new PageNode(resolvedUuid,
                rootItem.parentUuid(),
                rootSitePagePath,
                rootItem.lastModifiedDate(),
                rootItem.nodeType(),
                rootItem.customLinksByLanguage()));
        workSet.items().stream()
                .skip(1)
                .map(PublicationItem::toPageNode)
                .forEach(pagesToProcess::add);
        log.debug("Page expansion: path={}, recursive={}, pages={}", pagePath, isRecursive, pagesToProcess.size());
        return pagesToProcess;
    }

    protected boolean isRegionInvalid(final SitePagePath sitePagePath) throws IndexingException {
        final List<String> regions = this.getSiteRegions(sitePagePath.getSite());
        if (!regions.contains(sitePagePath.getRegion())) {
            log.warn("{}: {}: Invalid region '{}'.", PAGE_SKIPPED_MSG, sitePagePath, sitePagePath.getRegion());
            return true;
        }
        return false;
    }

    private List<String> getSiteRegions(final String siteName) throws IndexingException {
        try {
            final List<String> regions = regionsService.getRegionsForSite(siteName);
            if (regions.isEmpty()) {
                log.warn("No regions were found for given site.");
            }
            return regions;
        } catch (RepositoryException e) {
            final String message = this.handleExceptionMessage(String.format("Error while trying to get regions: %s", e.getMessage()));
            throw new IndexingException(message, e);
        }
    }

    protected String handleExceptionMessage(final String message) {
        final String errorMessage = String.format("%s: %s", getExceptionMessageTitle(), message);
        log.error(errorMessage);
        return errorMessage;
    }

    protected List<String> getIndexableSites() throws IndexingException {
        try {
            return indexingServiceConfig.getIndexableSites();
        } catch (RepositoryException e) {
            final String message = this.handleExceptionMessage(String.format("Invalid JCR configuration: %s", e.getMessage()));
            throw new IndexingException(message, e);
        }
    }

    protected abstract String getExceptionMessageTitle();
}
