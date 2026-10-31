package com.tbscg.doosan.magnolia.regions.service;

import info.magnolia.module.site.Site;

import java.util.List;

/**
 * {@link SiteService} defines an API for obtaining sites registered in Magnolia.
 *
 * @author Alejandro Puig Escobar
 */
public interface SiteService {

    /**
     * Gets all registered sites.
     *
     * @return <code>List<Site></code>.
     * @throws NoSitesException when no sites are found.
     */
    List<Site> getSites() throws NoSitesException;

    /**
     * Gets all registered sites whose names match a base name.
     *
     * @param baseName name to match.
     * @return <code>List<Site></code>.
     * @throws NoSitesException when no sites are found.     *
     * @throws SiteNotFoundException when site is not found.
     */
    List<Site> getSites(String baseName) throws NoSitesException, SiteNotFoundException;

    /**
     * Gets registered site by name.
     *
     * @param siteName site name to look for.
     * @return <code>Site</code>.
     * @throws NoSitesException when no sites are found.
     * @throws SiteNotFoundException when site is not found.
     */
    Site getSite(String siteName) throws NoSitesException, SiteNotFoundException;
}
