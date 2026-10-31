package com.tbscg.doosan.magnolia.localizations.workflow;

import com.google.gson.GsonBuilder;
import com.tbscg.doosan.magnolia.dxp.workflow.service.SqsService;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import com.tbscg.doosan.magnolia.localizations.converter.LocalizationsPackageJsonSerializer;
import com.tbscg.doosan.magnolia.localizations.converter.NodeToLocalizationsPackageConverter;
import com.tbscg.doosan.magnolia.localizations.model.LocalizationsPackage;
import com.tbscg.doosan.magnolia.localizations.model.NonRegionalizedLocalizationsPackage;
import com.tbscg.doosan.magnolia.localizations.model.RegionalizedLocalizationsPackage;
import com.tbscg.doosan.magnolia.localizations.service.LocalizationPackageStatusService;
import com.tbscg.doosan.magnolia.localizations.service.LocalizationsSqsService;
import info.magnolia.context.MgnlContext;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * BPMN work item handler {@link WorkItemHandler} that sends localizations package message to SQS queue using
 * {@link SqsService}.
 *
 * @author apuigescobar
 */
@Slf4j
public class LocalizationsSqsWorkItemHandler implements WorkItemHandler {

    private final SqsService sqsService;
    private final NodeToLocalizationsPackageConverter nodeToLocalizationsPackageConverter;
    private final LocalizationsPackageJsonSerializer localizationsPackageJsonSerializer;
    private final LocalizationPackageStatusService statusService;

    @Inject
    public LocalizationsSqsWorkItemHandler(LocalizationsSqsService sqsService,
                                           NodeToLocalizationsPackageConverter nodeToLocalizationsPackageConverter,
                                           LocalizationsPackageJsonSerializer localizationsPackageJsonSerializer,
                                           LocalizationPackageStatusService statusService) {
        this.sqsService = sqsService;
        this.nodeToLocalizationsPackageConverter = nodeToLocalizationsPackageConverter;
        this.localizationsPackageJsonSerializer = localizationsPackageJsonSerializer;
        this.statusService = statusService;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        try {
            handleExecuteWorkItem(workItem);
        } catch (RepositoryException e) {
            log.error("Error getting node from JCR repository.", e);
            statusService.reportFailure(workItem, e.getMessage(), e);
            workItemManager.abortWorkItem(workItem.getId());
        } catch (Exception e) {
            log.error("Error while processing work item", e);
            statusService.reportFailure(workItem, e.getMessage(), e);
            workItemManager.abortWorkItem(workItem.getId());
            throw e;
        }
        workItemManager.completeWorkItem(workItem.getId(), workItem.getParameters());
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        workItemManager.abortWorkItem(workItem.getId());
    }

    /**
     * Handle SQS call within localizations package publishing workflow.
     *
     * @param workItem        workItem
     * @throws RepositoryException if an error when accessing JCR repository occurs.
     */
    private void handleExecuteWorkItem(WorkItem workItem) throws RepositoryException {
        final String taskId = WorkflowUtil.getTaskIdFromWorkItem(workItem);
        final Node node = WorkflowUtil.getNodeFromWorkItem(workItem, MgnlContext.getInstance());

        final LocalizationsPackage localizationsPackage = this.nodeToLocalizationsPackageConverter.toBean(node);

        final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(NonRegionalizedLocalizationsPackage.class, this.localizationsPackageJsonSerializer)
                                                         .registerTypeAdapter(RegionalizedLocalizationsPackage.class, this.localizationsPackageJsonSerializer);
        final String localizationsPackageJson = gsonBuilder.create().toJson(localizationsPackage);

        final String messageId = sqsService.sendMessage(localizationsPackageJson, taskId);
        log.info("Published localizations package to localizations SQS with id={}", messageId);
        statusService.reportSuccess(node);
    }
}
