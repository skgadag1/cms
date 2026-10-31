package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

import java.util.List;
import java.util.Optional;

/**
 *  Tries each strategy in order to resolve placeholder - first one which is able to resolve wins.
 */
class PlaceholderResolver {

    private final List<PlaceholderResolverStrategy> strategies;

    PlaceholderResolver(List<PlaceholderResolverStrategy> strategies) {
        this.strategies = strategies;
    }

    Optional<String> resolve(Placeholder placeholder, String moduleName) {
        return strategies
                .stream()
                .filter(strategy -> strategy.resolve(placeholder, moduleName).isPresent())
                .findFirst()
                .flatMap(strategy -> strategy.resolve(placeholder, moduleName));
    }

    interface PlaceholderResolverStrategy {
        Optional<String> resolve(Placeholder placeholder, String moduleName);
    }
}
