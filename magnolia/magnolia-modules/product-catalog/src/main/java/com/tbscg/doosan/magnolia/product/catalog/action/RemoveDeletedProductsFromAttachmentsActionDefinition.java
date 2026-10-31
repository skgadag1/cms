package com.tbscg.doosan.magnolia.product.catalog.action;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.api.action.CommandActionDefinition;


@ActionType("removeDeletedProductsFromAttachmentsAction")
public class RemoveDeletedProductsFromAttachmentsActionDefinition extends CommandActionDefinition {

    public RemoveDeletedProductsFromAttachmentsActionDefinition() {
        setImplementationClass(RemoveDeletedProductsFromAttachmentsAction.class);
        setCommand("updateAttachments");
    }
}
