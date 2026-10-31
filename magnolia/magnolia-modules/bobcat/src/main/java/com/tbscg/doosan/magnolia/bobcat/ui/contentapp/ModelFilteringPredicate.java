package com.tbscg.doosan.magnolia.bobcat.ui.contentapp;

import com.machinezoo.noexception.Exceptions;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import info.magnolia.jcr.predicate.AbstractPredicate;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Objects;
import java.util.Optional;

/**
 * Predicate for filtering nodes by {@link NodeFilter} specification.
 */

@Slf4j
public class ModelFilteringPredicate extends AbstractPredicate<Node> {

    protected final NodeFilter modelFilter;

    public ModelFilteringPredicate(final NodeFilter nodeFilter) {
        if (nodeFilter == null) {
            log.error("nodeFilter is null");
            throw new IllegalArgumentException("nodeFilter is null");
        }
        this.modelFilter = nodeFilter;
    }

    @Override
    public boolean evaluateTyped(final Node node) {
        if (nodeTypeExpected(node)) {
            return nodePropertyValueExpected(node) || nodeNameExpected(node);
        }

        return !this.modelFilter.isHideEmpty() || nodeHasChild(node);
    }

    protected boolean nodeHasChild(Node rootNode) {
        try {
            for (Node node : NodeUtil.getNodes(rootNode)) {
                if (nodeTypeExpected(node) && nodePropertyValueExpected(node)) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            log.debug("Failed to check {}", rootNode);
        }
        return false;
    }

    protected boolean nodeTypeExpected(final Node node) {
        return Exceptions.wrap().get(() -> node.getPrimaryNodeType().getName().equals(modelFilter.getNodeType()));
    }

    protected boolean nodePropertyValueExpected(final Node node) {
        final Optional<String> propertyName = Optional.ofNullable(modelFilter.getPropertyName());
        return propertyName.map(name -> Exceptions.wrap().get(() ->
                        Objects.equals(
                                tryGetStringValue(node, name).orElse(modelFilter.getPropertyDefaultValue()),
                                modelFilter.getPropertyValue())))
                .orElse(false);
    }

    protected Optional<String> tryGetStringValue(final Node node, final String name) {
        try {
            return node.hasProperty(name)
                    ? Optional.ofNullable(node.getProperty(name).getValue().getString())
                    : Optional.empty();
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    protected boolean nodeNameExpected(final Node node) {
        return Exceptions.wrap().get(() -> node.getName().equals(modelFilter.getNodeName()));
    }
}