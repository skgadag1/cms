package com.tbscg.doosan.magnolia.bobcat.ui.contentapp;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

@Slf4j
public class LockedPagesPublishFilteringPredicate extends AbstractPredicate<Node> {
    @Override
    public boolean evaluateTyped(Node node) {
        try {
            if(node.isLocked()) {
                return false;
            }
            Node parent = node.getParent();
            while (parent != null) {
                if (parent.isLocked() && NodeTypes.Activatable.getActivationStatus(node) == NodeTypes.Activatable.ACTIVATION_STATUS_NOT_ACTIVATED) {
                    return false;
                }
                parent = parent.getParent();
            }
        } catch (RepositoryException e) {
            log.error("Couldnt check if node {} is locked", node);
        }
        return true;
    }
}
