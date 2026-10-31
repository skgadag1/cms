package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;

import javax.jcr.Node;
import java.util.List;

public interface PublicationScope {

    PublicationScope ALL = new All();

    default List<Node> filterNodes(final String workspace, final List<Node> nodes) {
        return nodes;
    }

    default boolean includes(final String workspace, final PublicationItem item) {
        return true;
    }

    /**
     * Identity scope: keeps every node and accepts every {@link PublicationItem}.
     * Implemented as a named record so it appears clearly in stack traces and toString output
     * (rather than an anonymous {@code PublicationScope$1}).
     */
    record All() implements PublicationScope {
    }
}
