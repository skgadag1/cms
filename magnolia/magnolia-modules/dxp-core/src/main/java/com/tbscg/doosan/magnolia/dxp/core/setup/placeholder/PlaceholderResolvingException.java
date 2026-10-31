package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Exception thrown in run-time when a placeholder(s) cannot be resolved.
 */
public class PlaceholderResolvingException extends RuntimeException {

    private final List<Placeholder> unresolvedPlaceholders;

    public PlaceholderResolvingException(List<Placeholder> unresolvedPlaceholders) {
        super("Cannot resolve placeholders: " + String.join(", ", unresolvedPlaceholders.stream()
                .map(placeholder -> placeholder.toString()).collect(toList())));
        this.unresolvedPlaceholders = unresolvedPlaceholders;
    }
}
