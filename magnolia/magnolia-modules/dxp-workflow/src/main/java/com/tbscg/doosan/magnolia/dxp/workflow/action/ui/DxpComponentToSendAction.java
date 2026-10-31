package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import com.tbscg.doosan.magnolia.bobcat.custompath.CustomPathUtil;
import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionCommittedEvent;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.ValueContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.isEventSendableSite;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public class DxpComponentToSendAction<T> {

    private static final Set<String> HANDLEABLE_NODE_TYPES = Set.of(
            NodeTypes.Page.NAME,
            "dxp:navigationLink",
            "dxp:navigationList",
            "dxp:navigationMegaList",
            "dxp:navigationMegaMenu",
            "dxp:navigationRow",
            "dxp:navigationColumn",
            "dxp:navigationMegaListColumn",
            "dxp:navigationItem");

    private final EventBus eventBus;
    private final ValueContext<T> valueContext;

    @Inject
    public DxpComponentToSendAction(@Named(SystemEventBus.NAME) EventBus eventBus,
                                    ValueContext<T> valueContext) {
        this.eventBus = eventBus;
        this.valueContext = valueContext;
    }

    public void execute() {
        this.getValueContext().getSingle().ifPresent(this::sendEvent);
    }

    public void sendEvent(T item) {
        if (item instanceof Node wrappedNode) {
            Optional<Node> nodeToHandle = this.findNodeToHandle(wrappedNode);
            nodeToHandle.ifPresent(node -> {
                String id = info.magnolia.jcr.util.NodeUtil.getNodeIdentifierIfPossible(node);
                String path = NodeUtil.getPath(node);
                String nodeType = NodeUtil.getPrimaryNodeType(node);
                String workspace = NodeUtil.tryGetNodeWorkspace(node).orElse(EMPTY);
                long lastModified = NodeUtil.getLastModifiedDate(node).getTimeInMillis();
                String parentId = NodeUtil.getParentNodeId(node);
                String parentNodeType = NodeUtil.getParentNodeType(node);
                String modelId = PropertyUtil
                        .tryGetProperty(node, "id")
                        .flatMap(PropertyUtil::getStringValue)
                        .orElse(EMPTY);
                SitePagePath sitePagePath = new SitePagePath(path);
                List<String> customLinks = CustomPathUtil.getAllCustomLinksFromNode(node);
                if (isEventSendableSite(sitePagePath.getSite())) {
                    this.eventBus.fireEvent(new DxpActionCommittedEvent(id, modelId, parentId, parentNodeType, path, customLinks, nodeType, workspace, lastModified, sitePagePath.getSite()));
                }
            });
        }
    }

    private Optional<Node> findNodeToHandle(Node node) {
        String nodeType = NodeUtil.getPrimaryNodeType(node);
        Optional<Node> optionalNode = Optional.of(node);
        while (optionalNode.isPresent() && !HANDLEABLE_NODE_TYPES.contains(nodeType)) {
            optionalNode = NodeUtil.tryGetParentNode(optionalNode.get());
            if (optionalNode.isPresent()) {
                nodeType = NodeUtil.getPrimaryNodeType(optionalNode.get());
            }
        }
        return optionalNode;
    }
}
