package com.tbscg.doosan.magnolia.marketing.campaign.app.drop;

import com.tbscg.doosan.magnolia.bobcat.field.BobcatContentType;
import com.vaadin.shared.ui.grid.DropLocation;
import info.magnolia.ui.contentapp.browser.drop.AbstractDropConstraint;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class CustomDropConstraint extends AbstractDropConstraint<CustomDropConstraintDefinition, Item> {

    @Inject
    public CustomDropConstraint(CustomDropConstraintDefinition definition) {
        super(definition);
    }

    @Override
    public boolean isAllowedAt(Item source, Item target, DropLocation dropLocation) {
        try {
            return this.checkDropAllowed(source, target, dropLocation);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDropAllowed(Item sourceItem, Item targetItem, DropLocation location) throws RepositoryException {
        Node sourceNode = (Node) sourceItem;
        Node targetNode = (Node) targetItem;
        BobcatContentType sourceNodeProductType = BobcatContentType.getBobcatContentByType(sourceNode.getPrimaryNodeType().getName());
        BobcatContentType targetNodeProductType = BobcatContentType.getBobcatContentByType(targetNode.getPrimaryNodeType().getName());

        return !sourceNodeProductType.equals(targetNodeProductType) &&
               !sourceNodeProductType.equals(BobcatContentType.REGION) &&
               !targetNode.getParent().getName().isEmpty();
    }
}