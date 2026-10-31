package com.tbscg.doosan.magnolia.dxp.workflow.event;

import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEventHandler;
import com.tbscg.doosan.magnolia.dxp.core.aws.EventBridge;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.NAVIGATION_WORKSPACE;

public class NavigationEventHandler extends DxpActionEventHandler {

    @Override
    protected String getMagnoliaApp(String workspace) {
        return EventBridge.MAGNOLIA_NAVIGATION_APP_NAME;
    }

    @Override
    protected String getMagnoliaDetailType(String workspace) {
        return EventBridge.MAGNOLIA_NAVIGATION_DETAIL_TYPE;
    }
    @Override
    public boolean shouldApplyHandler(DxpActionEvent event) {
        return NAVIGATION_WORKSPACE.equals(event.getWorkspace());
    }
}
