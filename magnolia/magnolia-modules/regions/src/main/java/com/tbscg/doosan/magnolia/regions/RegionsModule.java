package com.tbscg.doosan.magnolia.regions;

import com.tbscg.doosan.magnolia.regions.commands.BackupRegionsApiCommandConfig;
import com.tbscg.doosan.magnolia.regions.registry.SiteDefinitionRegistrar;
import com.tbscg.doosan.magnolia.regions.replication.RegionReplicator;
import com.tbscg.doosan.magnolia.regions.replication.ReplicatorConfiguration;
import com.tbscg.doosan.magnolia.regions.replication.ReplicatorParameters;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.ModulesStartedEvent;
import info.magnolia.module.site.Site;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.observation.WorkspaceEventListenerRegistration;
import info.magnolia.repository.RepositoryConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regions module class.
 *
 * @author Alejandro Puig Escobar
 */

@Singleton
@Slf4j
@Getter
@Setter
public class RegionsModule implements ModuleLifecycle {

    public static final String MODULE_NAME = "regions";

    private static final String MULTISITE_MODULE_PATH = "/modules/multisite/config/sites/";

    private final EventBus eventBus;
    private final SiteDefinitionRegistrar siteDefinitionRegistrar;
    private final RegionsService regionsService;
    private final ComponentProvider componentProvider;

    private List<ReplicatorConfiguration> replicatorConfig;
    private List<RegionReplicator> replicators;
    private Set<String> workspaces;
    private BackupRegionsApiCommandConfig backupRegionsApiCommandConfig;

    private WorkspaceEventListenerRegistration.Handle observationHandle;

    @Inject
    public RegionsModule(@Named(SystemEventBus.NAME) EventBus eventBus, SiteDefinitionRegistrar siteDefinitionRegistrar,
                         RegionsService regionsService, ComponentProvider componentProvider) {
        this.eventBus = eventBus;
        this.siteDefinitionRegistrar = siteDefinitionRegistrar;
        this.regionsService = regionsService;
        this.componentProvider = componentProvider;
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        this.eventBus.addHandler(ModulesStartedEvent.class, modulesStartedEvent -> this.registerRegionSites());
        if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {
            this.registerSiteConfigChangeObserver();
        }
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} stopping.", MODULE_NAME);
        if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_SHUTDOWN) {
            unregisterSiteConfigChangeObserver();
        }
    }

    private void registerSiteConfigChangeObserver() {
        try {
            this.observationHandle = this.getSitesConfigObservationHandle();
        } catch (RepositoryException e) {
            log.error("Unable to register observer for path [{}]", MULTISITE_MODULE_PATH, e);
        }
    }

    private WorkspaceEventListenerRegistration.Handle getSitesConfigObservationHandle() throws RepositoryException {
        return WorkspaceEventListenerRegistration
                .observe(RepositoryConstants.CONFIG, MULTISITE_MODULE_PATH, events -> this.registerRegionSites())
                //sufficient delay to wait until multisite module is restarted
                .withDelay(4000L, 10000L)
                .withSubNodes(true)
                .register();
    }

    private void unregisterSiteConfigChangeObserver() {
        try {
            this.observationHandle.unregister();
        } catch (RepositoryException e) {
            log.warn("Unable to unregister observation for path [{}]", MULTISITE_MODULE_PATH, e);
        }
    }

    private void registerRegionSites() {
        log.info("Registering region site definitions.");

        try {
            List<Site> sites = this.regionsService.getSites();

            if (!sites.isEmpty()) {
                sites.forEach(this.siteDefinitionRegistrar::register);
            }
        } catch (Exception e) {
            log.warn("Unable to find region sites");
        }
    }

    public List<RegionReplicator> getReplicators() {
        return replicators;
    }

    public Set<String> getWorkspaces() {
        return workspaces;
    }

    public void setReplicatorConfig(List<ReplicatorConfiguration> replicatorConfig) {
        this.replicatorConfig = replicatorConfig;
        this.updateRegionReplicators();
        this.updateWorkspaces();
    }

    private void updateRegionReplicators() {
        this.replicators = this.replicatorConfig.stream()
                .map(config -> {
                    final ReplicatorParameters params = ReplicatorParameters.builder()
                            .workspace(config.getWorkspace())
                            .site(config.getSite())
                            .build();
                    return this.componentProvider.newInstance(config.getReplicatorClass(), params);
                }).collect(Collectors.toList());
    }

    private void updateWorkspaces() {
        this.workspaces = this.replicatorConfig.stream()
                .filter(config -> config.getWorkspace() != null)
                .map(ReplicatorConfiguration::getWorkspace)
                .collect(Collectors.toSet());
    }
}
