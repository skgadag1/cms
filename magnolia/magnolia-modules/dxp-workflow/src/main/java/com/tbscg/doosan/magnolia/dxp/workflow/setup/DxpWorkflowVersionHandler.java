package com.tbscg.doosan.magnolia.dxp.workflow.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

/**
 * This class is optional and lets you manage the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 *
 * @see com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @see info.magnolia.module.ModuleVersionHandler
 * @see info.magnolia.module.delta.Task
 */
public class DxpWorkflowVersionHandler extends AutomaticDefaultDxpVersionHandler {

    private static final Task REMOVE_DXP_WORKFLOW = new RemoveNodeTask(
            "Remove custom DXP Workflow",
            "Removes custom workflow from config workspace",
            RepositoryConstants.CONFIG,
            "/modules/dxp-workflow/workflows"
    );

    private static final Task REMOVE_WORK_ITEM_HANDLER = new RemoveNodeTask(
            "Remove custom WorkItemHandler",
            "Removes workItemHandler from config workspace",
            RepositoryConstants.CONFIG,
            "/modules/dxp-workflow/workItemHandlers"
    );

    private static final Task REMOVE_CONTENT_TAGS_LAYOUT = new RemoveNodeTask(
            "Remove content-tags from app launcher layout",
            "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/content-tags"
    );

    private static final Task DISABLE_PUBLISHING_ORDER_SIBLINGS = new SetPropertyTask(
            "Disable orderSiblings feature",
            RepositoryConstants.CONFIG,
            "/modules/publishing-core/config",
            "orderSiblings",
            false
    );

    private static final Task ENABLE_PUBLISHING_ORDER_SIBLINGS = new SetPropertyTask(
            "Enable orderSiblings feature",
            RepositoryConstants.CONFIG,
            "/modules/publishing-core/config",
            "orderSiblings",
            true
    );

    private static final Task SET_WORKFLOW_PUBLICATION_PARAMETER_RESOLVER = new SetPropertyTask(
            "Set custom PublicationTaskParameterResolver",
            RepositoryConstants.CONFIG,
            "/modules/workflow-jbpm/tasks/publish",
            "parameterResolver",
            "com.tbscg.doosan.magnolia.dxp.workflow.resolver.WorkflowPublicationTaskParameterResolver"
    );

    public DxpWorkflowVersionHandler() {
        register(DeltaBuilder.update("2021.5.24", "DXP Workflow cleanup")
                .addTask(REMOVE_DXP_WORKFLOW)
                .addTask(REMOVE_WORK_ITEM_HANDLER));
        register(DeltaBuilder.update("2022.5.17", "Remove content-tags from app launcher layout")
                .addTask(REMOVE_CONTENT_TAGS_LAYOUT));
        register(DeltaBuilder.update("2022.5.23", "Disable ordering of siblings when publishing")
                .addTask(DISABLE_PUBLISHING_ORDER_SIBLINGS));
        register(DeltaBuilder.update("2022.6.8", "Re-enable ordering of siblings when publishing")
                .addTask(ENABLE_PUBLISHING_ORDER_SIBLINGS));
        register(DeltaBuilder.update("2024.1.23", "Add count of nodes in recursive publish")
                .addTask(SET_WORKFLOW_PUBLICATION_PARAMETER_RESOLVER));
    }

}
