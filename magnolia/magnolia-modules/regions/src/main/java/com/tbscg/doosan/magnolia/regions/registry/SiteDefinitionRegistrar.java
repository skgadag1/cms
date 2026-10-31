package com.tbscg.doosan.magnolia.regions.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.dxp.core.registry.DefinitionRegistrar;
import com.tbscg.doosan.magnolia.regions.RegionsModule;
import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionProviderBuilder;
import info.magnolia.config.registry.DefinitionRawView;
import info.magnolia.config.source.ConfigurationSourceTypes;
import info.magnolia.config.source.raw.DefinitionRawViewMapWrapper;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.registry.DefinitionTypes;
import info.magnolia.module.site.registry.SiteRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link DefinitionRegistrar} for registering {@link Site} using {@link SiteRegistry}.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
@Singleton
public class SiteDefinitionRegistrar implements DefinitionRegistrar<Site> {

    private final SiteRegistry siteRegistry;

    @Inject
    public SiteDefinitionRegistrar(SiteRegistry siteRegistry) {
        this.siteRegistry = siteRegistry;
    }

    /**
     * Registers site definition on site registry.
     *
     * @param site <code>Site</code> to register.
     */
    @Override
    public void register(Site site) {
        log.info("Registering site {}", site.getName());
        siteRegistry.register(this.getSiteDefinition(site));
    }

    /**
     * Unregisters site definition from site registry.
     *
     * @param site <code>Site</code> to unregister.
     */
    @Override
    public void unregister(Site site) {
        log.info("Unregistering site {}", site.getName());
        this.siteRegistry.unregisterAndRegister(this.getSiteMetadata(site), Collections.emptyList());
    }

    /**
     * Creates a site definition given a site.
     *
     * @param site <code>Site</code> to build definition from.
     * @return site definition as <code>DefinitionProvider<Site></code>.
     */
    private DefinitionProvider<Site> getSiteDefinition(Site site) {
        return DefinitionProviderBuilder.<Site>newBuilder()
                .metadata(siteRegistry.newMetadataBuilder()
                        .type(DefinitionTypes.SITE)
                        .name(site.getName())
                        .module(RegionsModule.MODULE_NAME)
                        .configurationSourceType(ConfigurationSourceTypes.jcr))
                .definition(site)
                .rawView(this.getRawView(site))
                .build();
    }

    /**
     * Creates definition view from site.
     *
     * @param site <code>Site</code> to create definition view from.
     * @return site definition view as <code>DefinitionRawView</code>.
     */
    private DefinitionRawView getRawView(Site site) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map siteMap = objectMapper.convertValue(site, Map.class);

        return new DefinitionRawViewMapWrapper(siteMap);
    }

    /**
     * Gets metadata from site.
     *
     * @param site <code>Site</code> to get metadata from.
     * @return site metadata collection as <code>Collection<DefinitionMetadata></code>.
     */
    private Collection<DefinitionMetadata> getSiteMetadata(Site site) {
        return siteRegistry.getAllMetadata().stream()
                .filter(metadata -> metadata.getReferenceId().equals(site.getName()))
                .collect(Collectors.toList());
    }
}
