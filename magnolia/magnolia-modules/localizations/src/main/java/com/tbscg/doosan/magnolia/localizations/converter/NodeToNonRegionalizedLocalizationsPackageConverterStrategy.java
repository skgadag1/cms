package com.tbscg.doosan.magnolia.localizations.converter;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.concepts.service.ConceptsService;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.localizations.model.LanguageGroupsPair;
import com.tbscg.doosan.magnolia.localizations.model.NonRegionalizedLocalizationsPackage;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import com.tbscg.doosan.magnolia.regions.service.NoRegionSitesException;
import com.tbscg.doosan.magnolia.regions.service.RegionSiteNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudfront.model.InvalidArgumentException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getDirectNodes;
import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getName;

/**
 * Strategy to convert {@link Node} into {@link NonRegionalizedLocalizationsPackage}
 *
 * @author apuigescobar
 */
@Singleton
@Slf4j
public class NodeToNonRegionalizedLocalizationsPackageConverterStrategy extends AbstractNodeToLocalizationsPackageConverterStrategy {

    private final SiteManager siteManager;
    private final RegionsService regionsService;

    @Inject
    public NodeToNonRegionalizedLocalizationsPackageConverterStrategy(SiteManager siteManager, RegionsService regionsService,
                                                                      LocaleService localeService, ConceptsService conceptsService) {
        super(localeService, conceptsService);
        this.siteManager = siteManager;
        this.regionsService = regionsService;
    }

    /**
     * Converts JCR node into <code>NonRegionalizedLocalizationsPackage</code> model instance.
     *
     * @param fromNode node to convert into <code>NonRegionalizedLocalizationsPackage</code>.
     * @return <code>NonRegionalizedLocalizationsPackage</code> model instance constructed from node parameter
     */
    @Override
    protected NonRegionalizedLocalizationsPackage handleConversion(Node fromNode) throws RepositoryException {
        final String appName = this.getCheckedAppName(fromNode);
        Node siteNode = this.getSiteNode(fromNode);
        final Set<LanguageGroupsPair> languageGroupsPairs = getLanguageGroupsPairs(siteNode);
        return new NonRegionalizedLocalizationsPackage(appName, languageGroupsPairs);
    }

    @Override
    protected Collection<String> getGroupNames(Node fromNode) throws RepositoryException {
        final String nodePath = getName(fromNode);
        if (regionsService.isRegionSite(nodePath) && !regionsService.isRegion(nodePath)) {
            final Collection<Node> singletonList = NodeUtil.getDirectNodes(fromNode);
            Preconditions.checkArgument(singletonList.size() == 1, String.format("There should be only one region for node [%s]", nodePath));
            for (final Node regionNode : singletonList) {
                return this.getDirectGroupNames(regionNode);
            }
        }
        return this.getDirectGroupNames(fromNode);
    }

    @Override
    protected String getAppName(Node fromNode) throws RepositoryException {
        try {
            final RegionSite regionSite = regionsService.getRegionSiteFromPath(fromNode.getPath());
            return regionSite.getName();
        } catch (NoRegionSitesException | RegionSiteNotFoundException e) {
            log.debug("Unable to find region site for node path [{}]", fromNode.getPath(), e);
        }
        final Site site = siteManager.getAssignedSite(fromNode);
        Preconditions.checkNotNull(site, String.format("Site can't be null for node [%s]", fromNode.getPath()));
        return site.getName();
    }

    private Node getSiteNode(Node fromNode) throws RepositoryException {
        String nodePath = fromNode.getPath();
        if (regionsService.isRegionSite(nodePath)) {
            if (regionsService.isRegion(nodePath)) {
                return fromNode;
            } else {
                return this.getUniqueRegionNode(fromNode);
            }
        } else if (siteManager.getAssignedSite(fromNode) != null) {
            return fromNode;
        }
        throw InvalidArgumentException.builder().message(String.format("Not valid site node [%s]", nodePath)).build();
    }

    private Node getUniqueRegionNode(Node regionSiteNode) throws RepositoryException {
        Collection<Node> regionNodesSingletonList = getDirectNodes(regionSiteNode);
        Optional<Node> optionalRegion = regionNodesSingletonList.stream().findFirst();
        Preconditions.checkArgument(optionalRegion.isPresent(), String.format("There must be a unique region for node [%s]", regionSiteNode.getPath()));
        return optionalRegion.get();
    }
}
