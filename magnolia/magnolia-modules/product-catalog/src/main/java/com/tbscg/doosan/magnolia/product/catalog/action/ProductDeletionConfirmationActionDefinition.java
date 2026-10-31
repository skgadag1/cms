package com.tbscg.doosan.magnolia.product.catalog.action;

import info.magnolia.i18nsystem.I18nText;
import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.ConfirmationActionDefinition;

@ActionType("productDeletionConfirmationAction")
public class ProductDeletionConfirmationActionDefinition extends ConfirmationActionDefinition {

    public ProductDeletionConfirmationActionDefinition() {
        setImplementationClass(ProductDeletionConfirmationAction.class);
    }
}
