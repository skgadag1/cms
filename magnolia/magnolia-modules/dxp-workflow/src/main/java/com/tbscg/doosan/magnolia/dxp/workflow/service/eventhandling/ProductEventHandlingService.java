package com.tbscg.doosan.magnolia.dxp.workflow.service.eventhandling;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;

import javax.inject.Named;
import javax.jcr.Node;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.applyCurrentPublicationScope;
import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.collectNodesForPublish;

public class ProductEventHandlingService extends DefaultEventHandlingService {


    public ProductEventHandlingService(@Named(SystemEventBus.NAME) final EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public List<Node> getNodesForPublish(String path, String workspace, boolean isRecursive) {
        return applyCurrentPublicationScope(workspace, collectNodesForPublish(path, workspace, isRecursive));
    }
}
