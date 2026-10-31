package com.tbscg.doosan.magnolia.regions.service;

import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import info.magnolia.module.site.Site;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * Defines an API for region sites.
 *
 * @author Alejandro Puig Escobar
 */
public interface RegionsService {

    /**
     * Obtains a list of all sites defined on regions workspace in form of <code>List<Site></code>.
     *
     * @return <code>List<Site></code>.
     * @throws NoRegionSitesException when no sites are found.
     */
    List<Site> getSites() throws NoRegionSitesException;

    /**
     * Obtains a list of all region sites defined on regions workspace in form of <code>List<RegionSite></code>.
     *
     * @return <code>List<RegionSite></code>.
     * @throws NoRegionSitesException when no region sites are found.
     */
    List<RegionSite> getRegionSites() throws NoRegionSitesException;

    /**
     * Gets a specific region from a <code>RegionSite</code> given its name.
     *
     * @param regionSite region site containing regions.
     * @param regionName name of the region we are looking for.
     * @return <code>Region</code>.
     * @throws RegionNotFoundException if specified region is not found.
     */
    Region getRegion(RegionSite regionSite, String regionName) throws RegionNotFoundException;

    /**
     * Gets a specific region from a path.
     *
     * @param path JCR node path
     * @return <code>Region</code>.
     * @throws RegionNotFoundException if specified region is not found.
     */
    Region getRegionFromPath(String path) throws RegionNotFoundException;

    /**
     * Gets root site name from a path.
     *
     * @param path JCR node path
     * @return root site name string
     */
    String getRootSiteNameFromPath(String path);

    /**
     * Gets region site name from a path.
     *
     * @param path JCR node path
     * @return root site name string
     */
    String getRegionSiteNameFromPath(String path);

    /**
     * Gets site from a path.
     *
     * @param path JCR node path
     * @return <code>RegionSite</code>
     * @throws NoRegionSitesException if no region sites are defined.
     * @throws RegionSiteNotFoundException if desired region site is not found.
     */
    RegionSite getRegionSiteFromPath(String path) throws NoRegionSitesException, RegionSiteNotFoundException;

    /**
     * Returns list of regions for given site.
     *
     * @param site site name to get regions from
     * @return list of regions for given site name
     * @throws RepositoryException JCR repository exception in case of problem with getting the nodes
     */
    List<String> getRegionsForSite(final String site) throws RepositoryException;

    /**
     * Returns list of languages for given site and region.
     *
     * @param site   site name to get languages from
     * @param region region name to get languages from
     * @return list languages for given region
     * @throws RepositoryException JCR repository exception in case of problem with getting the nodes
     */
    List<String> getLanguagesForSiteAndRegion(final String site, final String region) throws RepositoryException;

    /**
     * Checks whether a node path corresponds to a specific region site.
     *
     * @param path node path to check
     * @return whether the given node path belongs to a region site or not
     */
    boolean isRegionSite(String path);

    /**
     * Checks whether a node path corresponds to a specific region.
     *
     * @param path node path to check
     * @return whether the given node path belongs to a region or not
     */
    boolean isRegion(String path);
}
