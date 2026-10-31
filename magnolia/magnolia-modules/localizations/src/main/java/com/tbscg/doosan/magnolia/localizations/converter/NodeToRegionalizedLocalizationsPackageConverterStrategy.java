package com.tbscg.doosan.magnolia.localizations.converter;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.concepts.service.ConceptsService;
import com.tbscg.doosan.magnolia.localizations.model.LanguageGroupsPair;
import com.tbscg.doosan.magnolia.localizations.model.RegionLanguagesPair;
import com.tbscg.doosan.magnolia.localizations.model.RegionalizedLocalizationsPackage;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import com.tbscg.doosan.magnolia.regions.service.NoRegionSitesException;
import com.tbscg.doosan.magnolia.regions.service.RegionSiteNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getDirectNodes;

/**
 * Strategy to convert {@link Node} into {@link RegionalizedLocalizationsPackage}
 *
 * @author apuigescobar
 */
@Slf4j
@Singleton
public class NodeToRegionalizedLocalizationsPackageConverterStrategy extends AbstractNodeToLocalizationsPackageConverterStrategy {

    private final RegionsService regionsService;

    @Inject
    public NodeToRegionalizedLocalizationsPackageConverterStrategy(RegionsService regionsService, LocaleService localeService,
                                                                   ConceptsService conceptsService) {
        super(localeService, conceptsService);
        this.regionsService = regionsService;
    }

    /**
     * Converts JCR node into <code>RegionalizedLocalizationsPackage</code> model instance.
     *
     * @param fromNode node to convert into <code>RegionalizedLocalizationsPackage</code>.
     * @return <code>RegionalizedLocalizationsPackage</code> model instance constructed from node parameter
     */
    @Override
    protected RegionalizedLocalizationsPackage handleConversion(Node fromNode) throws RepositoryException {
        Preconditions.checkNotNull(fromNode, "Site/Region node can't be null");
        if (regionsService.isRegion(fromNode.getPath())) {
            return this.handleConversionForRegion(fromNode);
        } else {
            return this.handleConversionForSite(fromNode);
        }
    }

    private RegionalizedLocalizationsPackage handleConversionForRegion(Node regionNode) throws RepositoryException {
        final String appName = this.getCheckedAppName(regionNode);
        final Set<LanguageGroupsPair> languageGroupsPairs = this.getLanguageGroupsPairs(regionNode);
        final RegionLanguagesPair regionLanguagesPair = new RegionLanguagesPair(regionNode.getName(), languageGroupsPairs);
        final Set<RegionLanguagesPair> regionLanguagesPairs = new HashSet<>(Collections.singletonList(regionLanguagesPair));
        return new RegionalizedLocalizationsPackage(appName, regionLanguagesPairs);
    }

    private RegionalizedLocalizationsPackage handleConversionForSite(Node siteNode) throws RepositoryException {
        final String appName = this.getCheckedAppName(siteNode);
        final Collection<Node> regionNodes = getDirectNodes(siteNode);
        Preconditions.checkArgument(!regionNodes.isEmpty(), String.format("There should be at least one region node for site [%s]", siteNode.getName()));
        final Set<RegionLanguagesPair> regionLanguagesPairs = new HashSet<>();

        for (final Node regionNode : regionNodes) {
            regionLanguagesPairs.add(new RegionLanguagesPair(regionNode.getName(), this.getLanguageGroupsPairs(regionNode)));
        }

        Preconditions.checkArgument(!regionLanguagesPairs.isEmpty(), String.format("There should be at least one region language pair for site [%s]", siteNode.getName()));
        return new RegionalizedLocalizationsPackage(appName, regionLanguagesPairs);
    }

    @Override
    protected Collection<String> getGroupNames(Node regionNode) throws RepositoryException {
        String regionPath = regionNode.getPath();
        Preconditions.checkArgument(regionsService.isRegion(regionPath), String.format("Node [%s] should map to a region", regionPath));
        return getDirectGroupNames(regionNode);
    }

    @Override
    protected String getAppName(Node fromNode) {
        try {
            final RegionSite regionSite = regionsService.getRegionSiteFromPath(fromNode.getPath());
            Preconditions.checkNotNull(regionSite, "Region site can't be null");
            return regionSite.getName();
        } catch (NoRegionSitesException | RegionSiteNotFoundException | RepositoryException e) {
            log.debug("Unable to find region sites for node path [{}]", fromNode);
            return null;
        }
    }
}
