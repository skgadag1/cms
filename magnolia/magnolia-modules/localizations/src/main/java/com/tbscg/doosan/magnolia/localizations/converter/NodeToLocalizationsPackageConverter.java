package com.tbscg.doosan.magnolia.localizations.converter;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConversionException;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConverter;
import com.tbscg.doosan.magnolia.localizations.model.LocalizationsPackage;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;

/**
 * {@link BeanConverter} for converting {@link Node} into {@link LocalizationsPackage}.
 *
 * @author apuigescobar
 */
@Slf4j
@Singleton
public class NodeToLocalizationsPackageConverter implements BeanConverter<Node, LocalizationsPackage> {

    private final RegionsService regionsService;
    private final NodeToNonRegionalizedLocalizationsPackageConverterStrategy localizationsPackageConverterStrategy;
    private final NodeToRegionalizedLocalizationsPackageConverterStrategy regionalizedLocalizationsPackageConverterStrategy;

    @Inject
    public NodeToLocalizationsPackageConverter(RegionsService regionsService,
                                               NodeToNonRegionalizedLocalizationsPackageConverterStrategy localizationsPackageConverterStrategy,
                                               NodeToRegionalizedLocalizationsPackageConverterStrategy regionalizedLocalizationsPackageConverterStrategy) {
        this.regionsService = regionsService;
        this.localizationsPackageConverterStrategy = localizationsPackageConverterStrategy;
        this.regionalizedLocalizationsPackageConverterStrategy = regionalizedLocalizationsPackageConverterStrategy;
    }

    /**
     * Converts JCR node into <code>LocalizationsPackage</code> model instance.
     *
     * @param fromNode node to convert into <code>LocalizationsPackage</code>.
     * @return <code>LocalizationsPackage</code> model instance constructed from node parameter
     * @throws BeanConversionException in case that something goes wrong when converting node to <code>LocalizationsPackage</code>.
     */
    @Override
    public LocalizationsPackage toBean(Node fromNode) throws BeanConversionException {
        try {
            return this.handleConversion(fromNode);
        } catch (Exception e) {
            throw new BeanConversionException("Unable to convert node into " + LocalizationsPackage.class.getSimpleName(), e);
        }
    }

    private LocalizationsPackage handleConversion(Node fromNode) throws RepositoryException {
        Preconditions.checkNotNull(fromNode, "Localizations package node can't be null");
        final String nodePath = fromNode.getPath();
        if (regionsService.isRegionSite(nodePath)) {
            final String siteName = regionsService.getRootSiteNameFromPath(nodePath);
            final Collection<String> regionNames = regionsService.getRegionsForSite(siteName);
            if (regionNames.size() > 1) {
                return regionalizedLocalizationsPackageConverterStrategy.handleConversion(fromNode);
            }
        }
        return localizationsPackageConverterStrategy.handleConversion(fromNode);
    }
}
