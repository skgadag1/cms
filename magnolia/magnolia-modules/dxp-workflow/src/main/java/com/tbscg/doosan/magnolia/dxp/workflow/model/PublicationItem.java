package com.tbscg.doosan.magnolia.dxp.workflow.model;

import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record PublicationItem(String workspace,
                              String path,
                              String uuid,
                              String parentUuid,
                              String nodeType,
                              Instant lastModifiedDate,
                              Map<String, String> customLinksByLanguage,
                              String productLifecycle) {

    public PublicationItem {
        customLinksByLanguage = immutableCustomLinks(customLinksByLanguage);
    }

    public PageNode toPageNode() {
        return new PageNode(uuid, parentUuid, new SitePagePath(path), lastModifiedDate, nodeType, customLinksByLanguage);
    }

    private static Map<String, String> immutableCustomLinks(final Map<String, String> customLinksByLanguage) {
        if (customLinksByLanguage == null || customLinksByLanguage.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(customLinksByLanguage));
    }
}
