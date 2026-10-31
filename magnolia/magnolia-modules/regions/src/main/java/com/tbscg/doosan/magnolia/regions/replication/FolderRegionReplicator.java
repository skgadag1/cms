package com.tbscg.doosan.magnolia.regions.replication;

import com.tbscg.doosan.magnolia.regions.converter.NodeToRegionSiteConverter;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * {@link FolderRegionReplicator} replicates {@link RegionSite} and {@link Region} as a folder structure
 * for any designated workspace.
 *
 * @author apuigescobar
 */
public class FolderRegionReplicator extends AbstractRegionReplicator implements RegionReplicator {

    private final NodeToRegionSiteConverter nodeToRegionSiteConverter;
    private final RegionsService regionsService;

    @Inject
    public FolderRegionReplicator(NodeToRegionSiteConverter nodeToRegionSiteConverter, RegionsService regionsService,
                                  ReplicatorParameters replicatorParameters) {
        super(replicatorParameters.getWorkspace());
        this.nodeToRegionSiteConverter = nodeToRegionSiteConverter;
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
        Session conceptsSession = MgnlContext.getJCRSession(workspace);

        Node regionsSiteNode = regionsSession.getNode(path);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionsSiteNode);

        this.createSiteStructure(regionSite);

        conceptsSession.save();
    }

    @Override
    void createSiteStructure(RegionSite regionSite) throws RepositoryException {

        Node rootSiteNode = MgnlContext.getJCRSession(workspace).getRootNode();

        String regionSiteName = regionSite.getName();

        Node siteNode;

        if (!rootSiteNode.hasNode(regionSiteName)) {
            siteNode = rootSiteNode.addNode(regionSite.getName(), NodeTypes.Folder.NAME);

        } else {
            siteNode = rootSiteNode.getNode(regionSiteName);
        }

        for (Region region : regionSite.getRegions()) {

            if (!siteNode.hasNode(region.getName())) {
                siteNode.addNode(region.getName(), NodeTypes.Folder.NAME);
            }
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

    private void handleAddRegion(String path) throws RepositoryException, RegionNotFoundException {
        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);
        Session conceptsSession = MgnlContext.getJCRSession(workspace);

        Node regionsSiteNode = regionsSession.getNode(path);

        String regionName = regionsSiteNode.getName();

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionsSiteNode);

        Region regionToAdd = this.regionsService.getRegion(regionSite, regionName);

        this.createRegionStructure(regionSite, regionToAdd);

        conceptsSession.save();
    }

    @Override
    void createRegionStructure(RegionSite regionSite, Region region) throws RepositoryException {

        Session conceptsSession = MgnlContext.getJCRSession(workspace);

        Node rootNode = conceptsSession.getRootNode();
        String regionSitePath = regionSite.getName();

        if (rootNode.hasNode(regionSitePath)) {
            Node siteNode = rootNode.getNode(regionSitePath);

            if (!siteNode.hasNode(region.getName())) {
                siteNode.addNode(region.getName(), NodeTypes.Folder.NAME);
            }
        } else {
            throw new ReplicationException("No region site node found");
        }
    }

    @Override
    public void renameRegion(String srcPath, String dstPath) {
        try {
            this.moveStructure(srcPath, dstPath);

        } catch (Exception e) {
            throw new ReplicationException(srcPath + " to " + dstPath, e);
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

    private void handleRemoveRegion(String path) throws RepositoryException {

        Session conceptsSession = MgnlContext.getJCRSession(workspace);

        Node regionNode = conceptsSession.getNode(path);

        if (!regionNode.hasNodes()) {
            regionNode.remove();
        }

        conceptsSession.save();
    }

    @Override
    public void updateRegion(String path) {
        //do not handle it for now but in the future
    }

    @Override
    public void renameSite(String srcPath, String dstPath) {
        try {
            this.moveStructure(srcPath, dstPath);

        } catch (Exception e) {
            throw new ReplicationException(srcPath + " to " + dstPath, e);
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

    private void handleRemoveSite(String path) throws RepositoryException {

        Session conceptsSession = MgnlContext.getJCRSession(workspace);

        Node siteNode = conceptsSession.getNode(path);

        this.removeStructure(siteNode);

        conceptsSession.save();
    }

    @Override
    public void updateSite(String path) {
        //not necessary for now but in the future
    }
}
