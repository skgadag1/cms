package com.tbscg.doosan.magnolia.dxp.workflow.model;

import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Page representation that includes path, UUID and type.
 */
public record PageNode(String uuid,
                       String parentUuid,
                       SitePagePath path,
                       Instant lastModifiedDate,
                       String nodeType,
                       Map<String, String> customLinksByLanguage) {

    public PageNode {
        customLinksByLanguage = immutableCustomLinks(customLinksByLanguage);
    }

    public String getCustomLinkForLanguage(final String language) {
        return customLinksByLanguage.get(language);
    }

    public List<String> getCustomLinks() {
        if (customLinksByLanguage.isEmpty()) {
            return Collections.emptyList();
        }
        return customLinksByLanguage.values().stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static Map<String, String> immutableCustomLinks(final Map<String, String> customLinksByLanguage) {
        if (customLinksByLanguage == null || customLinksByLanguage.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(customLinksByLanguage));
    }
}
