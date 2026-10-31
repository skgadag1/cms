package com.tbscg.doosan.magnolia.regions.compatibility.app.drop;

import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import com.vaadin.v7.data.Item;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.tree.MoveLocation;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import javax.jcr.Node;

/**
 * Checks whether a region (website, region or language) content item can be hierarchically moved on regions workspace.
 *
 * @author Alejandro Puig Escobar
 */
public class RegionSiteDropConstraint implements DropConstraint {

    @Override
    public boolean allowedAsChild(Item sourceItem, Item targetItem) {
        return this.isAllowedAt(sourceItem, targetItem, MoveLocation.INSIDE);
    }

    @Override
    public boolean allowedBefore(Item sourceItem, Item targetItem) {
        return this.isAllowedAt(sourceItem, targetItem, MoveLocation.BEFORE);
    }

    @Override
    public boolean allowedAfter(Item sourceItem, Item targetItem) {
        return this.isAllowedAt(sourceItem, targetItem, MoveLocation.AFTER);
    }

    @Override
    public boolean allowedToMove(Item sourceItem) {
        return true;
    }

    @Override
    public boolean isAllowedAt(Item sourceItem, Item targetItem, MoveLocation location) {
        if ((sourceItem instanceof JcrItemAdapter) && (targetItem instanceof JcrItemAdapter)) {
            return isAllowed(((JcrItemAdapter) sourceItem).getJcrItem(),
                    ((JcrItemAdapter) targetItem).getJcrItem(), location);
        }

        return false;
    }

    /**
     * Checks if the item can be moved inside another item by node type hierarchy. Otherwise, checks if node type is
     * identical so it can be moved at the same level of depth.
     *
     * @param sourceItem source item to move.
     * @param targetItem target item to move to.
     * @param location type of operation performed.
     * @return whether is allowed to be moved or not
     */
    private boolean isAllowed(javax.jcr.Item sourceItem, javax.jcr.Item targetItem, MoveLocation location) {
        try {
            Node sourceNode = (Node) sourceItem;
            Node targetNode = (Node) targetItem;

            if (location == MoveLocation.INSIDE) {

                if (sourceNode.getPrimaryNodeType().isNodeType(RegionsConstants.NodeTypes.Region.NAME) &&
                        targetNode.getPrimaryNodeType().isNodeType(RegionsConstants.NodeTypes.Website.NAME)) {
                    return true;
                }

                return sourceNode.getPrimaryNodeType().isNodeType(RegionsConstants.NodeTypes.Language.NAME) &&
                        targetNode.getPrimaryNodeType().isNodeType(RegionsConstants.NodeTypes.Region.NAME);
            } else {

                return sourceNode.getPrimaryNodeType().equals(targetNode.getPrimaryNodeType());

            }

        } catch (Exception e) {
            return false;
        }
    }
}
