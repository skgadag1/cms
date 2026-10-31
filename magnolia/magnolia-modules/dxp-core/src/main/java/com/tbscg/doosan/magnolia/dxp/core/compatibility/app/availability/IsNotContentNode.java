package com.tbscg.doosan.magnolia.dxp.core.compatibility.app.availability;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * {@link AbstractAvailabilityRule} that checks if a given node has not <code>mgnl:content</code> as node type.
 *
 * @author Alejandro Puig Escobar
 */
@Deprecated
public class IsNotContentNode extends AbstractAvailabilityRule {

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        //JcrItemId is deprecated even though current AvailabilityChecker operates with it...
        if (itemId instanceof JcrItemId) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            try {
                Node jcrNode = NodeUtil.getNodeByIdentifier(jcrItemId.getWorkspace(), jcrItemId.getUuid());
                return !jcrNode.getPrimaryNodeType().isNodeType(NodeTypes.Content.NAME);
            } catch (RepositoryException e) {
                return false;
            }
        }
        return false;
    }
}
