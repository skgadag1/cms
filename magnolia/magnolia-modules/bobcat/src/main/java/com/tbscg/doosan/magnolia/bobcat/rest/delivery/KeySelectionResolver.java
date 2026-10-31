package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.context.Context;
import info.magnolia.rest.reference.ReferenceResolver;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class KeySelectionResolver implements ReferenceResolver<String, Object> {
    private final Provider<Context> contextProvider;
    private final KeySelectionProviderDefinition definition;

    @Inject
    public KeySelectionResolver(KeySelectionProviderDefinition definition, Provider<Context> contextProvider) {
        this.definition = definition;
        this.contextProvider = contextProvider;
    }

    @Override
    public Optional<Object> resolve(String identifier) {
        try {
            Node node = contextProvider.get().getJCRSession(definition.getTargetWorkspace()).getNodeByIdentifier(identifier);
            Map<String, Object> resolvedItems = resolveItemsFromNode(node);
            return Optional.of(resolvedItems);
        } catch (RepositoryException e) {
            return Optional.empty();
        }
    }

    private Map<String, Object> resolveItemsFromNode(Node node) throws RepositoryException {
        return definition.getItemsToResolve().stream()
                .map(item -> getItemFromNode(node, item).map(value -> new AbstractMap.SimpleEntry<>(item, value)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Optional<Object> getItemFromNode(Node node, String name) {
        try {
            if (node.hasProperty(name)) {
                return Optional.of(node.getProperty(name).getString());
            }
            if (node.hasNode(name)) {
                return Optional.of(node.getNode(name));
            }
        } catch (RepositoryException e) {
            log.warn("Error getting item from node: {}", name, e);
        }
        return Optional.empty();
    }
}
