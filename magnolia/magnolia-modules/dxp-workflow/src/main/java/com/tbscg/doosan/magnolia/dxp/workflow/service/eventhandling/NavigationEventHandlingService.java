package com.tbscg.doosan.magnolia.dxp.workflow.service.eventhandling;

import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionDeletedEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionPublishedEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionUnpublishedEvent;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.EventHandlingException;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Collections;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class NavigationEventHandlingService implements EventHandleable {
    private final EventBus eventBus;

    public NavigationEventHandlingService(@Named(SystemEventBus.NAME) final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public boolean handleEvent(final String eventAction,
                               final String nodePath,
                               final String nodeUuid,
                               final boolean isRecursive,
                               final long lastModified,
                               final String workspace) throws EventHandlingException {
        NodeUtil.collectDxpNodeByIdentifier(nodeUuid, workspace).ifPresent(node -> {
            String nodeType = NodeUtil.getPrimaryNodeType(node);
            String parentId = NodeUtil.getParentNodeId(node);
            String site = WorkflowConstant.BOBCAT_SITE;
            switch (eventAction) {
                case ActionType.PUBLISH:
                    this.eventBus.fireEvent(new DxpActionPublishedEvent(nodeUuid, EMPTY, parentId, EMPTY, nodePath, Collections.emptyList(), nodeType, workspace, lastModified, site));
                    break;
                case ActionType.UNPUBLISH:
                    this.eventBus.fireEvent(new DxpActionUnpublishedEvent(nodeUuid, EMPTY, parentId,  EMPTY, nodePath, Collections.emptyList(), nodeType, workspace, lastModified, site));
                    break;
                case ActionType.DELETE:
                    this.eventBus.fireEvent(new DxpActionDeletedEvent(nodeUuid, EMPTY, parentId, EMPTY, nodePath, Collections.emptyList(), nodeType, workspace, lastModified, site));
                    break;
                default:
                    log.warn("Not recognised even type {}. Event handling won't be processed", eventAction);
            }
        });
        return true;
    }
}
