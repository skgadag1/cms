package com.tbscg.doosan.magnolia.bobcat.ui.contentapp;

import com.machinezoo.noexception.Exceptions;
import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class for JCR-based {@link com.vaadin.data.provider.DataProvider}s.
 */
class JcrDataProviderUtils {

    private JcrDataProviderUtils() {
    }

    static Stream<? extends Item> streamJcrItems(final Iterator<? extends Item> items) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(items, Spliterator.DISTINCT | Spliterator.NONNULL), false);
    }

    /**
     * Filters out node types not defined in {@link JcrDatasourceDefinition#getAllowedNodeTypes()}.
     */
    static class NodeTypeFilteringPredicate extends info.magnolia.jcr.predicate.NodeFilteringPredicate {

        NodeTypeFilteringPredicate(final JcrDatasourceDefinition definition) {
            setNodeTypes(definition.getAllowedNodeTypes());
        }

        @Override
        public boolean evaluateTyped(final Node node) {
            return getNodeTypes().isEmpty() || !super.evaluateTyped(node);
        }
    }

    /**
     * Filters out JCR system nodes.
     */
    static class JcrMgnlNodeHidingPredicate extends AbstractPredicate<Node> {
        private final boolean isIncludeSystemNodes;

        public JcrMgnlNodeHidingPredicate(final boolean isIncludeSystemNodes) {
            this.isIncludeSystemNodes = isIncludeSystemNodes;
        }

        @Override
        public boolean evaluateTyped(final Node node) {
            return (isIncludeSystemNodes || !Exceptions.wrap().get(node::getName).startsWith(NodeTypes.JCR_PREFIX)) && !Exceptions.wrap().get(node::getName).startsWith(NodeTypes.REP_PREFIX);
        }
    }

    /**
     * Always true predicate.
     */
    static class AlwaysTruePredicate extends AbstractPredicate<Property> {

        @Override
        public boolean evaluateTyped(final Property node) {
            return true;
        }
    }

    static class HasChildrenProductsNodesPredicate extends AbstractPredicate<Node> {
        private static final String NON_MATCHED_TYPE = "product";
        private static final String SEPARATOR = ":";
        private static final String NODE_TYPE_PREFIX = "dxp";

        private final String propertyName;
        private final String validPropertyState;
        private final NodeFilter modelFilter;

        public HasChildrenProductsNodesPredicate(final NodeFilter nodeFilter, final String propertyName, final String validPropertyState) {
            this.propertyName = propertyName;
            this.validPropertyState = validPropertyState;
            this.modelFilter = nodeFilter;

        }

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return !isMatchingNodeType(node) || hasAnyChild(node);
            } catch (RepositoryException e) {
                return true;
            }
        }

        private boolean hasAnyChild(Node node) throws RepositoryException {
            for (Node childNode : NodeUtil.collectAllChildren(node, new NodeTypePredicate(NODE_TYPE_PREFIX + SEPARATOR + NON_MATCHED_TYPE))) {
                if (childNode.hasProperty(propertyName) && Objects.equals(childNode.getProperty(propertyName).getValue().getString(), validPropertyState)) {
                    return true;
                }
            }
            return false;
        }


        private boolean isMatchingNodeType(Node node) throws RepositoryException {
            final String nodeType = node.getPrimaryNodeType().getName().split(SEPARATOR)[1];
            return modelFilter.isHideEmpty() && !Objects.equals(nodeType, NON_MATCHED_TYPE);
        }
    }

}
