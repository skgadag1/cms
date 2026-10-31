package com.tbscg.doosan.magnolia.toc.workflow;

import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import com.tbscg.doosan.magnolia.dxp.core.util.DxpPreconditions;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.SqsService;
import com.tbscg.doosan.magnolia.toc.converter.NodeToTocPackageConverter;
import com.tbscg.doosan.magnolia.toc.model.TocPackage;
import com.tbscg.doosan.magnolia.toc.service.TocSqsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.Map;

@Slf4j
public class TocSqsWorkItemHandler implements WorkItemHandler {
    private static final String DATE_FORMAT = "MMM d, yyyy h:mm:ss a";
    private final TasksManager tasksManager;
    private final SqsService sqsService;
    private final NodeToTocPackageConverter nodeToTocPackageConverter;

    @Inject
    public TocSqsWorkItemHandler(TasksManager tasksManager, TocSqsService sqsService,
                                 NodeToTocPackageConverter nodeToTocMsgConverter) {
        this.tasksManager = tasksManager;
        this.sqsService = sqsService;
        this.nodeToTocPackageConverter = nodeToTocMsgConverter;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        try {
            handleExecuteWorkItem(workItem, workItemManager);
        } catch (RepositoryException e) {
            log.error("Error getting node from JCR repository.", e);
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
    }

    /**
     * Handle SQS call within ToC publishing workflow.
     *
     * @param workItem        workItem
     * @param workItemManager workManager
     * @throws RepositoryException if an error when accessing JCR repository occurs.
     */
    @SuppressWarnings("unchecked")
    private void handleExecuteWorkItem(WorkItem workItem, WorkItemManager workItemManager) throws RepositoryException {

        String taskId = (String) workItem.getParameter(WorkflowConstant.PARAMETER_TASK_ID);
        Map<String, Object> data = (Map<String, Object>) workItem.getParameter(WorkflowConstant.PARAMETER_MGNL_DATA);

        String path = (String) data.get(WorkflowConstant.PATH);
        String repository = (String) data.get(WorkflowConstant.REPOSITORY);

        Node node = MgnlContext.getJCRSession(repository).getNode(path);

        TocPackage tocPackage = this.createTocPackage(node, taskId);
        String tocPkgJsonMsg = convertToJson(tocPackage);

        String messageId = sqsService.sendMessage(tocPkgJsonMsg, tocPackage.getId());
        log.info("Published ToC package to SQS with id={}", messageId);
    }

    private String convertToJson(TocPackage tocPackage) {
        return new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .create()
                .toJson(tocPackage);
    }

    /**
     * Creates a ToC package given several sources.
     *
     * @param node node to convert from
     * @return TocPackage
     */
    private TocPackage createTocPackage(Node node, String taskId) {
        //create package with node related properties
        TocPackage tocPackage = this.nodeToTocPackageConverter.toBean(node);

        Date acceptanceDate = DateTime.now().toDate();
        tocPackage.setAcceptanceDate(acceptanceDate);

        Task task = this.tasksManager.getTaskById(taskId);
        Preconditions.checkNotNull(task, "Task must exist");

        String reviewer = task.getActorId();
        DxpPreconditions.checkNotBlank(reviewer, "There must be a reviewer");
        tocPackage.setReviewedBy(reviewer);

        String publisher = task.getRequestor();
        DxpPreconditions.checkNotBlank(publisher, "There must be a publisher");
        tocPackage.setPublishedBy(publisher);

        return tocPackage;
    }
}
