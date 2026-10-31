package com.tbscg.doosan.magnolia.dxp.workflow.bpmn;

import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

/**
 * BPMN work item handler {@link WorkItemHandler} that increases and activates work item nodes versions.
 *
 * @author apuigescobar
 */
@Slf4j
public class VersionActivationWorkItemHandler implements WorkItemHandler {

    private final VersionManager versionManager;

    @Inject
    public VersionActivationWorkItemHandler(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        new Thread(() -> {
            try {
                handleExecuteWorkItem(workItem);
                manager.completeWorkItem(workItem.getId(), workItem.getParameters());
            } catch (RepositoryException e) {
                log.error("Error operating on JCR repository.", e);
                manager.abortWorkItem(workItem.getId());
            }
        }).start();
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }

    /**
     * Handles node versioning and activation for a specific work item.
     *
     * @param workItem workItem to operate with
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    private void handleExecuteWorkItem(WorkItem workItem) throws RepositoryException {
        final String publisher = WorkflowUtil.getPublisherFromWorkItem(workItem);
        final String rootNodePath = WorkflowUtil.getNodePathFromWorkItem(workItem);
        //can't inject context cause it changes per each thread
        final Context context = Components.getComponent(SystemContext.class);
        MgnlContext.setInstance(context);
        final Session session = WorkflowUtil.getSessionFromWorkItem(workItem,context);
        final Node rootNode = session.getNode(rootNodePath);
        //parameterize recursive activation --> NOT needed for now
        this.handleNodeVersionActivation(rootNode, rootNodePath, publisher);
        session.save();
        session.logout();
        MgnlContext.release();
    }

    /**
     * Handles node versioning and activation for a specific node recursively.
     *
     * @param node node to increase version and activate.
     * @param publisher user attached to the version to be activated.
     * @throws RepositoryException  if any problem occurs when operating with the repository.
     */
    private void handleNodeVersionActivation(Node node, String rootNodePath, String publisher) throws RepositoryException {
        if (node.hasNodes()) {
            NodeIterator childNodesIt = node.getNodes();

            while (childNodesIt.hasNext()) {
                Node childNode = childNodesIt.nextNode();
                this.handleNodeVersionActivation(childNode, rootNodePath, publisher);
            }

            //FIXME avoid checked-in node exception
            if (!node.getPath().equals(rootNodePath)) {
                Version version = this.versionNode(node, publisher);
                this.activateNodeVersion(node, version, publisher);
            }
        }
    }

    /**
     * Increases node version.
     *
     * @param node node to increase version.
     * @param publisher user attached to the version.
     * @return node version.
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    private Version versionNode(Node node, String publisher) throws RepositoryException {
        Rule defaultRule = WorkflowUtil.getDefaultRule(node);
        return versionManager.addVersion(node, defaultRule, publisher);
    }

    /**
     * Activates node version.
     *
     * @param node node to activate version.
     * @param version version to activate.
     * @param publisher user attached to the version activation.
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    private void activateNodeVersion(Node node, Version version, String publisher) throws RepositoryException {
        NodeTypes.Activatable.update(node, publisher, true, version);
    }
}
