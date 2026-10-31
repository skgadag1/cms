package com.tbscg.doosan.magnolia.regions.converter;

import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConversionException;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConverter;
import com.tbscg.doosan.magnolia.dxp.core.util.DxpUtil;
import com.tbscg.doosan.magnolia.regions.RegionsModule;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.ConfiguredSite;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.util.OptionalConsumer;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;

import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * {@link BeanConverter} for converting {@link RegionSite} into {@link List<Site>}.
 *
 * @author Alejandro Puig Escobar
 */
@Singleton
public class RegionToSitesConverter implements BeanConverter<RegionSite, List<Site>> {

    private final SiteManager siteManager;
    private final Provider<RegionsModule> regionsConfigProvider;

    @Inject
    public RegionToSitesConverter(SiteManager siteManager, Provider<RegionsModule> regionsConfigProvider) {
        this.siteManager = siteManager;
        this.regionsConfigProvider = regionsConfigProvider;
    }

    /**
     * Converts region site model into magnolia site list.
     *
     * @param fromRegionSite <code>RegionSite</code> model to convert into <code>List<Site></code>
     * @return <code>List<Site></code> obtained from <code>RegionSite</code> model.
     */
    @Override
    public List<Site> toBean(RegionSite fromRegionSite) throws BeanConversionException {

        if (fromRegionSite.hasRegions()) {
            return this.handleConvertToSite(fromRegionSite);
        }

        throw new BeanConversionException(String.format("Missing regions on region site definition [%s]", fromRegionSite.getName()));
    }

    private List<Site> handleConvertToSite(RegionSite fromRegionSite) {
            List<Site> regionSites =  new ArrayList<>();

            Site defaultSite = siteManager.getDefaultSite();

            for (Region region : fromRegionSite.getRegions()) {

                regionSites.add(this.convertRegionToSite(fromRegionSite, region, defaultSite));

            }
            return regionSites;
    }

    private Site convertRegionToSite(RegionSite fromRegionSite, Region region, Site defaultSite) {
        ConfiguredSite regionSite = new ConfiguredSite();

        String regionSiteName = fromRegionSite.getName() + RegionsConstants.REGION_SITE_NAME_SEPARATOR
                + region.getName();

        regionSite.setDomains(fromRegionSite.hasDomains() ? fromRegionSite.getDomains() : defaultSite.getDomains());
        regionSite.setI18n(this.getI18n(region));

        Map<String, URI2RepositoryMapping> mappings = regionSite.getMappings();

        //all-inclusive workspace mapping until enabling specific field on regions application
        regionsConfigProvider.get().getWorkspaces().forEach(workspace -> mappings.put(workspace,
                this.getMapping(fromRegionSite.getName(), region.getName(), workspace)));

        regionSite.setTemplates(fromRegionSite.getTemplates());
        regionSite.setParameters(defaultSite.getParameters()); //until enabling parameters per region
        regionSite.setTheme(defaultSite.getTheme()); //until enabling themes per site
        regionSite.setVariations(defaultSite.getVariations());

        regionSite.setName(regionSiteName);

        regionSite.setEnabled(true); //enable by default

        return regionSite;
    }


    /**
     * Sets as fallback locale the first language on the region.
     *
     * @param region region containing languages configuration.
     * @return <code>I18nContentSupport</code>.
     */
    private I18nContentSupport getI18n(Region region) {

        I18nContentSupport i18n = region.getI18n();

        Optional<Locale> fallbackLocale = i18n.getLocales().stream().findFirst();
        OptionalConsumer.of(fallbackLocale).ifPresent(i18n::setFallbackLocale);

        return i18n;
    }

    /**
     * Creates <code>URI2RepositoryMapping</code> for a given region site in a specific workspace.
     *
     * @param siteName site name string used for handled and URI prefixes.
     * @param regionName region name string used for handled and URI prefixes.
     * @param workspace workspace used for the mapping.
     * @return <code>URI2RepositoryMapping</code> for a given <code>Site</code> and workspace.
     */
    private URI2RepositoryMapping getMapping(String siteName, String regionName, String workspace) {
        String regionPrefix = PATH_SEPARATOR + regionName;
        String handledPrefix = PATH_SEPARATOR + siteName + regionPrefix;
        String uriPrefix;

        if (workspace.equals(RepositoryConstants.WEBSITE)) {

            uriPrefix = DxpUtil.isAuthor() ? handledPrefix : regionPrefix;

        } else {

            uriPrefix = PATH_SEPARATOR + workspace + handledPrefix;

        }

        return new URI2RepositoryMapping(uriPrefix, workspace, handledPrefix);
    }
}
