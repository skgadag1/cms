package com.tbscg.doosan.magnolia.dxp.workflow.service.eventhandling;

import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionPublishedEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionUnpublishedEvent;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.EventHandlingException;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.jcr.Node;
import java.util.Collections;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.collectNodesForPublish;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class DefaultEventHandlingService implements EventHandleable {

    private final EventBus eventBus;

    public DefaultEventHandlingService(@Named(SystemEventBus.NAME) final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Handles events and sends them to event bridge, that are later processed in Lambda functions.
     *
     * @param eventAction event type. Expected values are PUBLISH or UNPUBLISH.
     * @param nodePath    JCR node path.
     * @param nodeUuid    JCR UUID
     * @param isRecursive flag saying if recursive publication was used.
     * @param publishTime time of publication.
     * @param workspace   JCR workspace name.
     * @return true value if processing was successful, false otherwise.
     * @throws EventHandlingException if event handling cannot be completed.
     */
    @Override
    public boolean handleEvent(final String eventAction,
                               final String nodePath,
                               final String nodeUuid,
                               final boolean isRecursive,
                               final long publishTime,
                               final String workspace) throws EventHandlingException {
        List<Node> nodes = getNodesForPublish(nodePath, workspace, isRecursive);
        log.info("Default event scope summary: workspace={}, path={}, recursive={}, nodes={}",
                workspace, nodePath, isRecursive, nodes.size());
        nodes.forEach(node -> {
            String nodeType = NodeUtil.getPrimaryNodeType(node);
            String parentNodeType = NodeUtil.getParentNodeType(node);
            String parentId = NodeUtil.getParentNodeId(node);
            String nodeId = info.magnolia.jcr.util.NodeUtil.getNodeIdentifierIfPossible(node);
            String modelId = PropertyUtil
                    .tryGetProperty(node, "id")
                    .flatMap(PropertyUtil::getStringValue)
                    .orElse(EMPTY);
            String path = info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node);
            String site = WorkflowConstant.BOBCAT_SITE;
            switch (eventAction) {
                case ActionType.PUBLISH ->
                        this.eventBus.fireEvent(new DxpActionPublishedEvent(
                                nodeId, modelId, parentId, parentNodeType, path, Collections.emptyList(), nodeType, workspace, publishTime, site));
                case ActionType.UNPUBLISH ->
                        this.eventBus.fireEvent(new DxpActionUnpublishedEvent(
                                nodeId, modelId, parentId, parentNodeType, path, Collections.emptyList(), nodeType, workspace, publishTime, site));
                default -> log.warn("Not recognised event type {}. Event handling won't be processed", eventAction);
            }
        });
        return true;
    }

    public List<Node> getNodesForPublish(String path, String workspace, boolean isRecursive) {
        return collectNodesForPublish(path, workspace, isRecursive);
    }
}
