package com.tbscg.doosan.magnolia.dxp.workflow.event;

import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEvent;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionEventHandler;
import com.tbscg.doosan.magnolia.dxp.core.aws.EventBridge;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.Set;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.*;

public class DefaultEventHandler extends DxpActionEventHandler {

    private static final Set<String> DEDICATED_HANDLER_WORKSPACES = Set.of(
            WEBSITE_WORKSPACE,
            NAVIGATION_WORKSPACE,
            CONFIG_WORKSPACE);

    @Override
    protected String getMagnoliaApp(String workspace) {
        return switch (workspace) {
            case CONFIG_WORKSPACE -> EventBridge.MAGNOLIA_CONFIG_APP_NAME;
            case PRODUCTS_WORKSPACE -> EventBridge.MAGNOLIA_PRODUCTS_APP_NAME;
            case ATTACHMENTS_WORKSPACE -> EventBridge.MAGNOLIA_ATTACHMENTS_APP_NAME;
            case MARKETING_CAMPAIGNS_WORKSPACE -> EventBridge.MAGNOLIA_MARKETING_CAMP_APP_NAME;
            case FORMS_EMAIL_CONF_WORKSPACE -> EventBridge.MAGNOLIA_FORM_EMAIL_CONF_APP_NAME;
            case FORMS_TYPE_WORKSPACE -> EventBridge.MAGNOLIA_FORMS_TYPE_APP_NAME;
            case FACETS_WORKSPACE -> EventBridge.MAGNOLIA_FACETS_APP_NAME;
            case DXP_TAGS_WORKSPACE -> EventBridge.MAGNOLIA_DXP_TAGS_APP_NAME;
            case MARKETING_TAGS_WORKSPACE -> EventBridge.MAGNOLIA_MARKETING_TAGS_APP_NAME;
            case REGIONS_WORKSPACE -> EventBridge.MAGNOLIA_REGIONS_APP_NAME;
            case FAQ_WORKSPACE -> EventBridge.MAGNOLIA_FAQ_APP_NAME;
            case CONCEPTS_WORKSPACE -> EventBridge.MAGNOLIA_CONCEPTS_APP_NAME;
            default -> Strings.EMPTY;
        };
    }

    @Override
    protected String getMagnoliaDetailType(String workspace) {
        return switch (workspace) {
            case CONFIG_WORKSPACE -> EventBridge.MAGNOLIA_CONFIG_DETAIL_TYPE;
            case PRODUCTS_WORKSPACE -> EventBridge.MAGNOLIA_PRODUCTS_DETAIL_TYPE;
            case ATTACHMENTS_WORKSPACE -> EventBridge.MAGNOLIA_ATTACHMENTS_DETAIL_TYPE;
            case MARKETING_CAMPAIGNS_WORKSPACE -> EventBridge.MAGNOLIA_MARKETING_CAMP_DETAIL_TYPE;
            case FORMS_EMAIL_CONF_WORKSPACE -> EventBridge.MAGNOLIA_FORM_EMAIL_CONF_DETAIL_TYPE;
            case FORMS_TYPE_WORKSPACE -> EventBridge.MAGNOLIA_FORMS_TYPE_DETAIL_TYPE;
            case FACETS_WORKSPACE -> EventBridge.MAGNOLIA_FACETS_DETAIL_TYPE;
            case DXP_TAGS_WORKSPACE -> EventBridge.MAGNOLIA_DXP_TAGS_DETAIL_TYPE;
            case MARKETING_TAGS_WORKSPACE -> EventBridge.MAGNOLIA_MARKETING_TAGS_DETAIL_TYPE;
            case REGIONS_WORKSPACE -> EventBridge.MAGNOLIA_REGIONS_DETAIL_TYPE;
            case FAQ_WORKSPACE -> EventBridge.MAGNOLIA_FAQ_DETAIL_TYPE;
            case CONCEPTS_WORKSPACE -> EventBridge.MAGNOLIA_CONCEPTS_DETAIL_TYPE;
            default -> Strings.EMPTY;
        };
    }

    @Override
    public boolean shouldApplyHandler(DxpActionEvent event) {
        return StringUtils.isNotEmpty(event.getWorkspace())
                && !DEDICATED_HANDLER_WORKSPACES.contains(event.getWorkspace());
    }
}
