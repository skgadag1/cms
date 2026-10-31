package com.tbscg.doosan.magnolia.dxp.workflow.event;

import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEventHandler;
import com.tbscg.doosan.magnolia.dxp.core.aws.EventBridge;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.WEBSITE_WORKSPACE;

public class BobcatPagesEventHandler extends DxpActionEventHandler {

    @Override
    protected String getMagnoliaApp(String workspace) {
        return EventBridge.MAGNOLIA_PAGES_APP_NAME;
    }
    @Override
    protected String getMagnoliaDetailType(String workspace) {
        return EventBridge.MAGNOLIA_PAGES_DETAIL_TYPE;
    }
    @Override
    public boolean shouldApplyHandler(DxpActionEvent event) {
        return WEBSITE_WORKSPACE.equals(event.getWorkspace()) && WorkflowConstant.BOBCAT_SITE.equals(event.getSite());
    }
}
