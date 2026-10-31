package com.tbscg.doosan.magnolia.dxp.workflow.service.configuration;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;

import java.util.List;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;

/**
 * Configuration class for the @{@link com.tbscg.doosan.magnolia.dxp.workflow.service.EventHandlingService}.
 */
public final class EventHandlingServiceConfiguration {

    public static final List<String> EVENT_HANDLEABLE_WORKSPACE = List.of(
            WorkflowConstant.WEBSITE_WORKSPACE,
            WorkflowConstant.NAVIGATION_WORKSPACE,
            ATTACHMENTS_WORKSPACE,
            PRODUCTS_WORKSPACE,
            WorkflowConstant.REGIONS_WORKSPACE,
            WorkflowConstant.MARKETING_CAMPAIGNS_WORKSPACE,
            WorkflowConstant.FORMS_EMAIL_CONF_WORKSPACE,
            WorkflowConstant.FORMS_TYPE_WORKSPACE,
            WorkflowConstant.FACETS_WORKSPACE,
            WorkflowConstant.DXP_TAGS_WORKSPACE,
            WorkflowConstant.MARKETING_TAGS_WORKSPACE,
            WorkflowConstant.CONFIG_WORKSPACE,
            WorkflowConstant.FAQ_WORKSPACE,
            WorkflowConstant.CONCEPTS_WORKSPACE);
    public static final List<String> EVENT_HANDLEABLE_NOT_VERSIONED_WORKSPACE = List.of(
            ATTACHMENTS_WORKSPACE,
            PRODUCTS_WORKSPACE,
            WorkflowConstant.CONFIG_WORKSPACE);

    private EventHandlingServiceConfiguration() {
    }

    public static boolean isEventHandleableWorkspace(String workspace) {
        return EVENT_HANDLEABLE_WORKSPACE.contains(workspace);
    }

    public static boolean isEventHandleableNotVersionedWorkspace(String workspace) {
        return EVENT_HANDLEABLE_NOT_VERSIONED_WORKSPACE.contains(workspace);
    }
}
