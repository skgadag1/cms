package com.tbscg.doosan.magnolia.dxp.workflow.event;

import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEventHandler;
import com.tbscg.doosan.magnolia.dxp.core.aws.EventBridge;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.CONFIG_WORKSPACE;

public class ConfigEventHandler extends DxpActionEventHandler {

    @Override
    protected String getMagnoliaApp(String workspace) {
        return EventBridge.MAGNOLIA_CONFIG_APP_NAME;
    }

    @Override
    protected String getMagnoliaDetailType(String workspace) {
        return EventBridge.MAGNOLIA_CONFIG_DETAIL_TYPE;
    }
    @Override
    public boolean shouldApplyHandler(DxpActionEvent event) {
        return CONFIG_WORKSPACE.equals(event.getWorkspace()) && (
                event.getPath().startsWith("/modules/multisite/config/sites") ||
                event.getPath().startsWith("/modules/bobcat/commonConfig") ||
                event.getPath().startsWith("/modules/cloudinary/config")
        );
    }
}
