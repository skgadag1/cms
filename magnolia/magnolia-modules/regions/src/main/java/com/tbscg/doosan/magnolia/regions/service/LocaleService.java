package com.tbscg.doosan.magnolia.regions.service;

import com.tbscg.doosan.magnolia.dxp.core.i18n.LocalesWrapper;

import javax.jcr.Node;

/**
 * LocaleService service used for integration with RegionSite functionality.
 */
public interface LocaleService {
    /**
     * Resolves locales based on provided site node.
     * @param fromNode JCR node.
     * @return Locales with list of locales used for given site node.
     */
    LocalesWrapper resolveLocales(Node fromNode);

    /**
     * Checks whether a node is localized or not.
     *
     * @param fromNode node to check.
     * @return whether the node is localized or not.
     */
    boolean isLocalized(Node fromNode);
}
