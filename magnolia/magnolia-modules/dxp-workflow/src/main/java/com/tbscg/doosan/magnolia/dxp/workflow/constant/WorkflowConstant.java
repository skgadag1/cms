package com.tbscg.doosan.magnolia.dxp.workflow.constant;

import java.util.Locale;

public interface WorkflowConstant {
    String PATH = "path";
    String REPOSITORY = "repository";
    String PARAMETER_MGNL_DATA =  "mgnlData";
    String PARAMETER_TASK_ID = "taskId";
    String PUBLISHER = "userName";
    String FORM_CONFIGURATION_TYPE = "dxp:formConfiguration";
    String FORM_EMAIL_CONFIG_PROPERTY = "dxpFormEmailConfig";

    String CUSTOM_NAME_FOR_LANG = "customNameForLang";
    String DEFAULT_LANG = Locale.ENGLISH.getLanguage();
    String REMOVE_FROM_URL_PROPERTY = "removeFromUrl";
    String COMPLETELY_HIDDEN_PROPERTY = "completelyHidden";
    String CUSTOM_NAME_PROPERTY = "customName";
    String CUSTOM_LINK_PROPERTY = "customLink";
    String REGION_PROPERTY = "region";
    String FACETS_PROPERTY = "facet";
    String FORM_CONFIGURATION_PROPERTY = "configuration";
    String DXP_TAG_PROPERTY = "dxpTag";

    /* workspaces */
    String CONFIG_WORKSPACE = "config";
    String WEBSITE_WORKSPACE = "website";
    String NAVIGATION_WORKSPACE = "navigation";
    String MARKETING_CAMPAIGNS_WORKSPACE = "marketingCampaigns";
    String FORMS_EMAIL_CONF_WORKSPACE = "formsEmailConfig";
    String FORMS_TYPE_WORKSPACE = "formsType";
    String FORMS_CONFIGURATION_WORKSPACE = "formsConfiguration";
    String FACETS_WORKSPACE = "facets";
    String DXP_TAGS_WORKSPACE = "dxpTags";
    String REGIONS_WORKSPACE = "regions";
    String MARKETING_TAGS_WORKSPACE = "marketing-tags";
    String FAQ_WORKSPACE = "faq";
    String CONCEPTS_WORKSPACE = "concepts";
    String FEATURES_WORKSPACE = "features";

    String BOBCAT_SITE = "bobcat";

    String DEALERNET_SITE = "dealernet";
}
