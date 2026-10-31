package com.tbscg.doosan.magnolia.dxp.core.aws;

public interface EventBridge {
    String SOURCE = "dxp-magnolia";
    String EVENT_BUS_NAME = "dxp-magnolia-event-bus";
    String MAGNOLIA_COMMIT_ACTION = "commitAction";
    String MAGNOLIA_DELETE_ACTION = "deleteAction";
    String MAGNOLIA_PUBLISH_ACTION = "publishAction";
    String MAGNOLIA_UNPUBLISH_ACTION = "unpublishAction";

    /* navigation */
    String MAGNOLIA_NAVIGATION_APP_NAME = "navigation";
    String MAGNOLIA_NAVIGATION_DETAIL_TYPE = "navigationApp";

    /* pages */
    String MAGNOLIA_PAGES_APP_NAME = "pages";
    String MAGNOLIA_PAGES_DETAIL_TYPE = "pagesApp";

    /* config */
    String MAGNOLIA_CONFIG_APP_NAME = "config";
    String MAGNOLIA_CONFIG_DETAIL_TYPE = "configApp";

    /* products */
    String MAGNOLIA_PRODUCTS_APP_NAME = "products";
    String MAGNOLIA_PRODUCTS_DETAIL_TYPE = "productsApp";

    /* attachments */
    String MAGNOLIA_ATTACHMENTS_APP_NAME = "attachments";
    String MAGNOLIA_ATTACHMENTS_DETAIL_TYPE = "attachmentsApp";

    /* marketingCampaigns */
    String MAGNOLIA_MARKETING_CAMP_APP_NAME = "marketingCampaigns";
    String MAGNOLIA_MARKETING_CAMP_DETAIL_TYPE = "marketingCampaignsApp";

    /* formsEmailConfig */
    String MAGNOLIA_FORM_EMAIL_CONF_APP_NAME = "formsEmailConfig";
    String MAGNOLIA_FORM_EMAIL_CONF_DETAIL_TYPE = "formsEmailConfigApp";

    /* formsType */
    String MAGNOLIA_FORMS_TYPE_APP_NAME = "formsType";
    String MAGNOLIA_FORMS_TYPE_DETAIL_TYPE = "formsTypeApp";

    /* facets */
    String MAGNOLIA_FACETS_APP_NAME = "facets";
    String MAGNOLIA_FACETS_DETAIL_TYPE = "facetsApp";

    /* dxpTags */
    String MAGNOLIA_DXP_TAGS_APP_NAME = "dxpTags";
    String MAGNOLIA_DXP_TAGS_DETAIL_TYPE = "dxpTagsApp";

    /* regions */
    String MAGNOLIA_REGIONS_APP_NAME = "regions";
    String MAGNOLIA_REGIONS_DETAIL_TYPE = "regionsApp";

    /* marketingTags */
    String MAGNOLIA_MARKETING_TAGS_APP_NAME = "marketingTags";
    String MAGNOLIA_MARKETING_TAGS_DETAIL_TYPE = "marketingTagsApp";

    /* faq */
    String MAGNOLIA_FAQ_APP_NAME = "faq";
    String MAGNOLIA_FAQ_DETAIL_TYPE = "faqApp";

    /* concepts */
    String MAGNOLIA_CONCEPTS_APP_NAME = "concepts";
    String MAGNOLIA_CONCEPTS_DETAIL_TYPE = "conceptsApp";

}
