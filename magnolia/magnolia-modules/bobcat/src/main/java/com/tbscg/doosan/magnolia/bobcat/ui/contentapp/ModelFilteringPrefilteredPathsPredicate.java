package com.tbscg.doosan.magnolia.bobcat.ui.contentapp;

import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Predicate for filtering nodes by {@link NodeFilter} specification.
 */

@Slf4j
public class ModelFilteringPrefilteredPathsPredicate extends ModelFilteringPredicate {

    private final List<String> preselectedNodesPath = new ArrayList<>();
    private final boolean skipPathsFiltering;

    public ModelFilteringPrefilteredPathsPredicate(final NodeFilter nodeFilter, final NodeIterator preselectedNodes, boolean skipPathsFiltering) {
        super(nodeFilter);

        this.skipPathsFiltering = skipPathsFiltering;

        if (!skipPathsFiltering) {
            try {
                while (preselectedNodes.hasNext()) {
                    Node node = (Node) preselectedNodes.next();
                    preselectedNodesPath.add(StringUtils.substringBeforeLast(node.getPath().toLowerCase(), "/"));
                }
            } catch (final RepositoryException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    protected boolean nodeHasChild(Node node) {
        if (this.skipPathsFiltering) {
            return true;
        }

        try {
            String searchedPath = node.getPath().toLowerCase();

            return this.preselectedNodesPath
                    .stream()
                    .anyMatch(preselectedNode -> preselectedNode.contains(searchedPath));
        } catch (RepositoryException e) {
            return false;
        }
    }
}
