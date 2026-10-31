package com.tbscg.doosan.magnolia.dxp.workflow.event;

import com.tbscg.doosan.magnolia.dxp.core.Visibility;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEventHandler;
import com.tbscg.doosan.magnolia.dxp.core.aws.EventBridge;
import com.tbscg.doosan.magnolia.dxp.core.util.VisibilityUtil;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.DEALERNET_SITE;
import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.WEBSITE_WORKSPACE;

public class DealernetPagesEventHandler extends DxpActionEventHandler {

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
        return WEBSITE_WORKSPACE.equals(event.getWorkspace()) && DEALERNET_SITE.equals(event.getSite());
    }

    @Override
    protected Event buildEvent(final DxpActionEvent actionEvent, final String instance) {
        final String path = actionEvent.getPath();
        return DealernetEvent.builder()
                .withId(actionEvent.getId())
                .withModelId(actionEvent.getModelId())
                .withParentId(actionEvent.getParentId())
                .withPath(path)
                .withCustomLinks(actionEvent.getCustomLinks())
                .withNodeType(actionEvent.getNodeType())
                .withInstance(instance)
                .withMagnoliaApp(getMagnoliaApp(actionEvent.getWorkspace()))
                .withLastModified(actionEvent.getLastModifiedInMillis())
                .withSite(actionEvent.getSite())
                .withPageVisibility(VisibilityUtil.getPageVisibility(path, WEBSITE_WORKSPACE, DEALERNET_SITE))
                .withComponents(VisibilityUtil.getPageComponentsWithVisibiility(path, WEBSITE_WORKSPACE))
                .build();
    }

    @Getter
    @SuperBuilder(setterPrefix = "with")
    static class DealernetEvent extends Event {
        final List<Visibility> pageVisibility;
    }
}
