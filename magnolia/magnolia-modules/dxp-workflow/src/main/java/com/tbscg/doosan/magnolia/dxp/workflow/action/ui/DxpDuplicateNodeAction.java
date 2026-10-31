package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.action.DuplicateNodeAction;
import info.magnolia.ui.contentapp.action.DuplicateNodeActionDefinition;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

@Slf4j
public class DxpDuplicateNodeAction extends DuplicateNodeAction {

    private final DxpComponentToSendAction componentToSendAction;
    private final UiContext uiContext;

    @Inject
    public DxpDuplicateNodeAction(DuplicateNodeActionDefinition definition,
                                  ValueContext<Node> valueContext,
                                  NodeNameHelper nodeNameHelper,
                                  JcrDatasourceDefinition jcrDatasourceDefinition,
                                  UiContext uiContext,
                                  @Named(SystemEventBus.NAME) EventBus eventBus) {
        super(definition, valueContext, nodeNameHelper, jcrDatasourceDefinition);
        this.componentToSendAction = new DxpComponentToSendAction(eventBus, valueContext);
        this.uiContext = uiContext;
    }

    @Override
    public Node process(Node node) throws RepositoryException {
        if (NodeUtil.isNodeOrParentLocked(node)) {
            log.error("Duplication failed: Either the Parent or the node is locked");
            this.uiContext.openAlert(MessageStyleTypeEnum.WARNING, "Duplication Disabled", "Either parent or the page is locked. Please unlock it before duplicating this page.", "OK", () -> {
            });
            return node;
        } else {
            Node processedNode = super.process(node);
            this.componentToSendAction.execute();
            return processedNode;
        }

    }
}
