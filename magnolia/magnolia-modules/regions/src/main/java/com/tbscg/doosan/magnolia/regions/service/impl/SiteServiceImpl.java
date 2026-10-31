package com.tbscg.doosan.magnolia.regions.service.impl;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.regions.service.NoSitesException;
import com.tbscg.doosan.magnolia.regions.service.SiteNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.SiteService;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.registry.SiteRegistry;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation for {@link SiteService}.
 *
 * @author Alejandro Puig Escobar
 */
public class SiteServiceImpl implements SiteService {

    private final SiteRegistry siteRegistry;

    @Inject
    public SiteServiceImpl(SiteRegistry siteRegistry) {
        this.siteRegistry = siteRegistry;
    }

    @Override
    public List<Site> getSites() throws NoSitesException {

        List<Site> sites = (List<Site>)this.siteRegistry.getAllDefinitions();

        if (sites.isEmpty()) {
            throw new NoSitesException("No sites available on registries");
        }

        return sites;
    }

    @Override
    public List<Site> getSites(String baseName) throws NoSitesException, SiteNotFoundException {

        Preconditions.checkArgument(!StringUtils.isBlank(baseName), "Base name can't be blank");

        List<Site> sites = this.getSites().stream()
                    .filter(site -> StringUtils.substringBefore(site.getName(), RegionsConstants.REGION_SITE_NAME_SEPARATOR)
                            .equals(baseName))
                    .collect(Collectors.toList());

        if (sites.isEmpty()) {
            throw new SiteNotFoundException("No sites with base name " + baseName + " were found");
        }

        return sites;
    }

    @Override
    public Site getSite(String siteName) throws NoSitesException, SiteNotFoundException {

        Preconditions.checkArgument(!StringUtils.isBlank(siteName), "Site name can't be blank");

        return this.getSites().stream()
                .filter(site -> site.getName().equals(siteName))
                .findFirst()
                .orElseThrow(() -> new SiteNotFoundException("Site " + siteName + " not found in registries"));
    }
}
