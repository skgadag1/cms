package com.tbscg.doosan.magnolia.regions.replication;

import com.tbscg.doosan.magnolia.regions.converter.NodeToRegionSiteConverter;
import com.tbscg.doosan.magnolia.regions.converter.RegionToSitesConverter;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.registry.SiteDefinitionRegistrar;
import com.tbscg.doosan.magnolia.regions.service.NoSitesException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.tbscg.doosan.magnolia.regions.service.SiteNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.SiteService;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.site.Site;
import info.magnolia.util.OptionalConsumer;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * {@link RegionReplicator} implementation that replicates events in regions workspace by registering
 * regional sites in site registry.
 *
 * @author Alejandro Puig Escobar
 */
@Singleton
public class RegistryRegionReplicator implements RegionReplicator {

    private final NodeToRegionSiteConverter nodeToRegionSiteConverter;
    private final RegionToSitesConverter regionToSiteConverter;
    private final SiteDefinitionRegistrar siteDefinitionRegistrar;
    private final RegionsService regionsService;
    private final SiteService siteService;

    @Inject
    public RegistryRegionReplicator(NodeToRegionSiteConverter nodeToRegionSiteConverter,
                                    RegionToSitesConverter regionToSiteConverter,
                                    SiteDefinitionRegistrar siteDefinitionRegistrar, SiteService siteService,
                                    RegionsService regionsService) {
        this.nodeToRegionSiteConverter = nodeToRegionSiteConverter;
        this.regionToSiteConverter = regionToSiteConverter;
        this.siteDefinitionRegistrar = siteDefinitionRegistrar;
        this.siteService = siteService;
        this.regionsService = regionsService;
    }

    @Override
    public void addSite(String path) {
        try {
            this.handleAddSite(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleAddSite(String path) throws RepositoryException {

        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);

        Node regionSiteNode = regionsSession.getNode(path);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionSiteNode);

        if (regionSite.hasRegions()) {
            //each region site has one or more Magnolia sites
            Collection<Site> sitesToRegister = this.regionToSiteConverter.toBean(regionSite);
            this.siteDefinitionRegistrar.registerCollection(sitesToRegister);
        }
    }

    @Override
    public void addRegion(String path) {
        try {
            this.handleAddRegion(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleAddRegion(String path) throws RepositoryException {

        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);

        Node regionSiteNode = regionsSession.getNode(path);

        String regionSiteName = this.regionsService.getRegionSiteNameFromPath(path);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionSiteNode);

        //each region site has one or more Magnolia sites
        Optional<Site> siteToRegister = this.regionToSiteConverter.toBean(regionSite).stream()
                .filter(site -> site.getName().equals(regionSiteName))
                .findFirst();

        OptionalConsumer.of(siteToRegister).ifPresent(this.siteDefinitionRegistrar::register);

    }

    @Override
    public void renameRegion(String srcPath, String dstPath) {
        try {
            this.handleRenameRegion(srcPath, dstPath);

        } catch (Exception e) {
            throw new ReplicationException(srcPath + " to " + dstPath, e);
        }
    }

    private void handleRenameRegion(String srcPath, String dstPath) throws RepositoryException, NoSitesException,
            SiteNotFoundException {

        String toUnregisterSiteName = this.regionsService.getRegionSiteNameFromPath(srcPath);
        Site siteToUnregister = this.siteService.getSite(toUnregisterSiteName);

        this.siteDefinitionRegistrar.unregister(siteToUnregister);

        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);

        Node regionSiteNode = regionsSession.getNode(dstPath);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionSiteNode);

        String toRegisterSiteName = this.regionsService.getRegionSiteNameFromPath(dstPath);

        //each region site has one or more Magnolia sites
        Optional<Site> siteToRegister = this.regionToSiteConverter.toBean(regionSite)
                .stream()
                .filter(site -> site.getName().equals(toRegisterSiteName))
                .findFirst();

        OptionalConsumer.of(siteToRegister).ifPresent(this.siteDefinitionRegistrar::register);
    }

    @Override
    public void updateRegion(String path) {
        try {
            this.handleUpdateRegion(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleUpdateRegion(String path) throws RepositoryException {

        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);

        String rootSiteName = this.regionsService.getRootSiteNameFromPath(path);
        Node siteNode = regionsSession.getNode(NodeUtil.PATH_SEPARATOR + rootSiteName);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(siteNode);

        if (regionSite != null) {

            List<Site> sites = this.regionToSiteConverter.toBean(regionSite);

            String regionSiteName = this.regionsService.getRegionSiteNameFromPath(path);

            Optional<Site> siteOpt = sites.stream().filter(site -> site.getName().equals(regionSiteName))
                    .findFirst();

            OptionalConsumer.of(siteOpt).ifPresent(this.siteDefinitionRegistrar::register)
                    .ifNotPresent(() -> this.updateSite(path));

        }
    }

    @Override
    public void removeRegion(String path) {
        try {
            this.handleRemoveRegion(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleRemoveRegion(String path) throws NoSitesException, SiteNotFoundException {

        String regionSiteName = this.regionsService.getRegionSiteNameFromPath(path);
        Site siteToUnregister = this.siteService.getSite(regionSiteName);

        this.siteDefinitionRegistrar.unregister(siteToUnregister);
    }

    @Override
    public void renameSite(String srcPath, String dstPath) {
        try {
            this.handleRenameSite(srcPath, dstPath);

        } catch (Exception e) {
            throw new ReplicationException(srcPath + " to " + dstPath, e);
        }
    }

    private void handleRenameSite(String srcPath, String dstPath) throws RepositoryException, NoSitesException,
            SiteNotFoundException {

        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);

        Node regionSiteNode = regionsSession.getNode(dstPath);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionSiteNode);

        if (regionSite.hasRegions()) {
            Collection<Site> sitesToRegister = this.regionToSiteConverter.toBean(regionSite);

            String baseSiteName = this.regionsService.getRootSiteNameFromPath(srcPath);
            Collection<Site> sitesToUnregister = this.siteService.getSites(baseSiteName);

            this.siteDefinitionRegistrar.unregisterCollection(sitesToUnregister);
            this.siteDefinitionRegistrar.registerCollection(sitesToRegister);
        }
    }

    @Override
    public void removeSite(String path) {
        try {
            this.handleRemoveSite(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleRemoveSite(String path) throws NoSitesException, SiteNotFoundException {

        String rootSiteName = this.regionsService.getRootSiteNameFromPath(path);
        Collection<Site> sitesToUnregister = this.siteService.getSites(rootSiteName);

        this.siteDefinitionRegistrar.unregisterCollection(sitesToUnregister);
    }

    @Override
    public void updateSite(String path) {
        try {
            this.handleUpdateSite(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleUpdateSite(String path) throws RepositoryException {

        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);

        String rootSiteName = this.regionsService.getRootSiteNameFromPath(path);
        Node siteNode = regionsSession.getNode(NodeUtil.PATH_SEPARATOR + rootSiteName);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(siteNode);

        if (regionSite.hasRegions()) {
            List<Site> sites = this.regionToSiteConverter.toBean(regionSite);
            this.siteDefinitionRegistrar.registerCollection(sites);
        }

    }
}
