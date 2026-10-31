package com.tbscg.doosan.magnolia.regions.service.impl;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConversionException;
import com.tbscg.doosan.magnolia.regions.converter.NodeToRegionSiteConverter;
import com.tbscg.doosan.magnolia.regions.converter.RegionToSitesConverter;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.service.NoRegionSitesException;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionSiteNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.site.Site;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.regions.util.RegionsConstants.REGIONS_WORKSPACE;
import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * Default implementation for {@link RegionsService}.
 *
 * @author Alejandro Puig Escobar
 */
@Singleton
@Slf4j
public class RegionsServiceImpl implements RegionsService {

    private final NodeToRegionSiteConverter nodeToRegionSiteConverter;
    private final RegionToSitesConverter regionToSitesConverter;
    private final SystemContext systemContext;

    @Inject
    public RegionsServiceImpl(NodeToRegionSiteConverter nodeToRegionSiteConverter, RegionToSitesConverter regionToSitesConverter,
                              SystemContext systemContext) {
        this.nodeToRegionSiteConverter = nodeToRegionSiteConverter;
        this.regionToSitesConverter = regionToSitesConverter;
        this.systemContext = systemContext;
    }

    @Override
    public List<Site> getSites() throws NoRegionSitesException {

        List<RegionSite> regionSites = this.getRegionSites();

        if (!regionSites.isEmpty()) {

            List<Site> sites = new ArrayList<>();

            for (RegionSite regionSite : regionSites) {
                try {
                    sites.addAll(this.regionToSitesConverter.toBean(regionSite));
                } catch (BeanConversionException e) {
                    /*we don't want to abort execution in this case*/
                }
            }

            return sites;
        }

        throw new NoRegionSitesException("No sites on regions workspace found");
    }

    @Override
    public List<RegionSite> getRegionSites() throws NoRegionSitesException {
        try {
            return this.handleGetRegionSites();

        } catch (Exception e) {
            throw new NoRegionSitesException("No region sites found", e);
        }
    }

    private List<RegionSite> handleGetRegionSites() throws RepositoryException {
        Node regionsRootNode = this.systemContext.getJCRSession(REGIONS_WORKSPACE).getRootNode();

        NodeIterator nodeIt = (NodeIterator) NodeUtil.getNodes(regionsRootNode, RegionsConstants.NodeTypes.Website.NAME).iterator();

        Collection<Node> regionSiteNodes = NodeUtil.getCollectionFromNodeIterator(nodeIt);

        return regionSiteNodes.stream().map(this.nodeToRegionSiteConverter::toBean)
                .collect(Collectors.toList());
    }

    @Override
    public Region getRegion(RegionSite regionSite, String regionName) throws RegionNotFoundException {

        Preconditions.checkArgument(regionSite != null, "Region site can't be null");
        Preconditions.checkArgument(!StringUtils.isBlank(regionName), "Region name can't be blank");

        return regionSite.getRegions().stream().filter(region -> region.getName().equals(regionName))
                .findFirst()
                .orElseThrow(() -> new RegionNotFoundException("Region " + regionName + " not found"));
    }

    @Override
    public String getRegionSiteNameFromPath(String path) {

        Preconditions.checkArgument(!StringUtils.isBlank(path), "Path can't be blank");

        String[] siteNameTokens = StringUtils.split(path, PATH_SEPARATOR);

        if (siteNameTokens.length >= 2) {

            StringBuilder siteNameBuilder = new StringBuilder();

            for (int i = 0; i < 2; i++) {

                if (i != 0) {
                    siteNameBuilder.append(RegionsConstants.REGION_SITE_NAME_SEPARATOR);
                }
                siteNameBuilder.append(siteNameTokens[i]);

            }

            return siteNameBuilder.toString();
        }

        throw new IllegalArgumentException("Provided path " + path + " is not correct");
    }

    @Override
    public String getRootSiteNameFromPath(String path) {
        Preconditions.checkArgument(!StringUtils.isBlank(path), "Path can't be blank");

        String[] siteNameTokens = StringUtils.split(path, PATH_SEPARATOR);
        Preconditions.checkArgument(siteNameTokens.length >= 1, String.format("Provided path [%s] is not correct", path));

        return siteNameTokens[0];
    }

    @Override
    public RegionSite getRegionSiteFromPath(String path) throws NoRegionSitesException, RegionSiteNotFoundException {
        Preconditions.checkArgument(StringUtils.isNotBlank(path), String.format("Path [%s] can't be blank", path));

        final String siteName = this.getRootSiteNameFromPath(path);
        Preconditions.checkArgument(StringUtils.isNotBlank(siteName), String.format("Path [%s] does not contain any site name in it!", path));

        for (RegionSite regionSite : this.getRegionSites()) {
            if (regionSite.getName().equals(siteName)) {
                return regionSite;
            }
        }

        throw new RegionSiteNotFoundException(String.format("Region site not found for site name [%s]", siteName));
    }

    @Override
    public Region getRegionFromPath(String path) throws RegionNotFoundException {
        Preconditions.checkArgument(StringUtils.isNotBlank(path), "Path can't be blank");

        String[] siteNameTokens = StringUtils.split(path, PATH_SEPARATOR);
        Preconditions.checkArgument(siteNameTokens.length >= 2, String.format("Path [%s] is invalid", path));
        String regionName = siteNameTokens[1];

        try {
            RegionSite site = this.getRegionSiteFromPath(path);
            return this.getRegion(site, regionName);
        } catch (NoRegionSitesException | RegionSiteNotFoundException e) {
            throw new RegionNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getRegionsForSite(final String site) throws RepositoryException {
        return getJcrNodes(RegionsConstants.NodeTypes.Region.NAME, PATH_SEPARATOR + site);
    }

    @Override
    public List<String> getLanguagesForSiteAndRegion(final String site, final String region) throws RepositoryException {
        return getJcrNodes(RegionsConstants.NodeTypes.Language.NAME, PATH_SEPARATOR + site + PATH_SEPARATOR + region);
    }

    private List<String> getJcrNodes(final String nodeType, final String path) throws RepositoryException {
        final List<String> nodes = new ArrayList<>();
        final Session session = this.systemContext.getJCRSession(REGIONS_WORKSPACE);
        final Node rootNode = session.getNode(path);
        final NodeIterator nodeIterator = rootNode.getNodes();

        while (nodeIterator.hasNext()) {
            final Node node = nodeIterator.nextNode();
            if (node.isNodeType(nodeType)) {
                nodes.add(node.getName());
            }
        }
        return nodes;
    }

    @Override
    public boolean isRegionSite(String path) {
        try {
            return this.getRegionSiteFromPath(path) != null;
        } catch (Exception e) {
            log.debug("Unable to get region site for path {}", path, e);
            return false;
        }
    }

    @Override
    public boolean isRegion(String path) {
        try {
            return this.getRegionFromPath(path) != null;
        } catch (IllegalArgumentException | RegionNotFoundException e) {
            log.debug("Unable to get region site for path {}", path, e);
            return false;
        }
    }
}
