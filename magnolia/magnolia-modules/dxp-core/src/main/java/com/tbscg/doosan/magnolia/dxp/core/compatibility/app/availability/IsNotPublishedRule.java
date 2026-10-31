package com.tbscg.doosan.magnolia.dxp.core.compatibility.app.availability;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * {@link AbstractAvailabilityRule} that checks if a given node is not published.
 *
 * @author apuigescobar
 */
@Deprecated
public class IsNotPublishedRule extends AbstractAvailabilityRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsNotPublishedRule.class);

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        //JcrItemId is deprecated even though current AvailabilityChecker operates with it...
        if (itemId instanceof JcrItemId) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            try {
                Node jcrNode = NodeUtil.getNodeByIdentifier(jcrItemId.getWorkspace(), jcrItemId.getUuid());
                return !NodeTypes.Activatable.isActivated(jcrNode);
            } catch (RepositoryException e) {
                LOGGER.warn("Error evaluating availability for node with UUID [{}], returning false: {}",
                        jcrItemId.getUuid(), e.getMessage());
                return false;
            }
        }
        return false;
    }
}
