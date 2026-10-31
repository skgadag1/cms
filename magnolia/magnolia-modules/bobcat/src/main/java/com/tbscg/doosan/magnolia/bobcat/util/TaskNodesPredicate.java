package com.tbscg.doosan.magnolia.bobcat.util;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.jcr.predicate.AbstractPredicate;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.time.LocalDate;
import java.time.ZoneId;


@Slf4j
public class TaskNodesPredicate extends AbstractPredicate<Node> {

    private static final String UTC = "UTC";
    private static final String TASK_FOLDER_TYPE = "mgnl:contentNode";
    private static final String TASK_NODE_TYPE = "mgnl:task";
    private final LocalDate expireDate;

    public TaskNodesPredicate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    @Override
    public boolean evaluateTyped(Node node) {
        try {
            return isNodeTaskOrFolder(node) && isTaskExpired(node);
        } catch (RepositoryException e) {
            log.error("Failed to read type of node {}", node);
            return false;
        }
    }

    private boolean isNodeTaskOrFolder(Node node) throws RepositoryException {
        String nodeType = node.getPrimaryNodeType().getName();
        String parentType = node.getParent().getPrimaryNodeType().getName();
        return nodeType.equals(TASK_NODE_TYPE) ||
                (nodeType.equals(TASK_FOLDER_TYPE) && !parentType.equals(TASK_NODE_TYPE));
    }

    private boolean isTaskExpired(Node node) {
        LocalDate nodeDate = NodeUtil.getLastModifiedDate(node).toInstant().atZone(ZoneId.of(UTC)).toLocalDate();
        return nodeDate.isBefore(expireDate);
    }
}
