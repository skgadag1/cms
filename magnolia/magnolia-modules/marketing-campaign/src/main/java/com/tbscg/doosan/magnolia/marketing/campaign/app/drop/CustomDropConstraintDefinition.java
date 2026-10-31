package com.tbscg.doosan.magnolia.marketing.campaign.app.drop;


import info.magnolia.ui.contentapp.browser.drop.DropConstraintDefinition;
import info.magnolia.ui.contentapp.browser.drop.DropConstraintType;

@DropConstraintType("marketingCampaignCustomDropConstraint")
public class CustomDropConstraintDefinition extends DropConstraintDefinition {

    public CustomDropConstraintDefinition() {
        setImplementationClass(CustomDropConstraint.class);
    }
}