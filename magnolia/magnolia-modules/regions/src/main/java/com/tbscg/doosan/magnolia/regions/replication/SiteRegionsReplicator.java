package com.tbscg.doosan.magnolia.regions.replication;

import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import static com.tbscg.doosan.magnolia.regions.util.RegionsConstants.REGIONS_WORKSPACE;
import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * {@link SiteRegionsReplicator} replicates {@link Region}(s) from a given {@link RegionSite} to target workspace.
 */
@Slf4j
public class SiteRegionsReplicator extends AbstractRegionReplicator implements RegionReplicator {

    private final String site;

    @Inject
    public SiteRegionsReplicator(final ReplicatorParameters replicatorParameters) {
        super(replicatorParameters.getWorkspace());
        this.site = replicatorParameters.getSite();
    }

    @Override
    void createSiteStructure(RegionSite regionSite) {
        //no implementation required
    }

    @Override
    void createRegionStructure(RegionSite regionSite, Region region) {
        //no implementation required
    }

    @Override
    public void addSite(String path) throws ReplicationException {
        if (isSiteNotInReplicationScope(path)) {
            return;
        }

        try {
            final Session session = MgnlContext.getJCRSession(workspace);
            if (!session.itemExists(path)) {
                final Node rootNode = session.getRootNode();
                rootNode.addNode(this.site, NodeTypes.Folder.NAME);
                session.save();
            }
        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    @Override
    public void addRegion(final String path) {
        if (isSiteNotInReplicationScope(path)) {
            return;
        }
        final String regionName = getRegionNameFromPath(path);

        try {
            final Session session = MgnlContext.getJCRSession(workspace);
            final Node siteNode = session.getRootNode().getNode(this.site);
            if (!siteNode.hasNode(regionName)) {
                siteNode.addNode(regionName, NodeTypes.Folder.NAME);
                session.save();
            }
        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    @Override
    public void renameSite(String srcPath, String dstPath) {
        //no implementation required
    }

    @Override
    public void renameRegion(String srcPath, String dstPath) {
        if (isSiteNotInReplicationScope(srcPath)) {
            return;
        }

        try {
            final Session session = MgnlContext.getJCRSession(workspace);
            if (!session.itemExists(srcPath)) {
                return;
            }

            final Node regionNode = session.getNode(srcPath);
            if (!regionNode.hasNodes()) {
                session.move(srcPath, dstPath);
                session.save();
            }
        } catch (Exception e) {
            throw new ReplicationException(srcPath + " to " + dstPath, e);
        }
    }

    @Override
    public void removeSite(String path) {
        if (isSiteNotInReplicationScope(path)) {
            return;
        }

        try {
            final Session session = MgnlContext.getJCRSession(workspace);
            if (!session.itemExists(path)) {
                return;
            }

            final Node siteNode = session.getNode(path);
            if (!siteNode.hasNodes()) {
                siteNode.remove();
                session.save();
            }
        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    @Override
    public void removeRegion(final String path) {
        if (isSiteNotInReplicationScope(path)) {
            return;
        }

        try {
            final Session session = MgnlContext.getJCRSession(workspace);
            if (!session.itemExists(path)) {
                return;
            }

            final Node regionNode = session.getNode(path);
            if (!regionNode.hasNodes()) {
                regionNode.remove();
                session.save();
            }
        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    @Override
    public void updateSite(String path) {
        if (isSiteNotInReplicationScope(path)) {
            return;
        }

        try {
            final Session targetSession = MgnlContext.getJCRSession(workspace);
            final Session regionsSession = MgnlContext.getJCRSession(REGIONS_WORKSPACE);
            final NodeIterator regionNodes = regionsSession.getNode(path).getNodes();
            while (regionNodes.hasNext()) {
                final Node regionNode = regionNodes.nextNode();
                if (regionNode.isNodeType(RegionsConstants.NodeTypes.Region.NAME) &&
                        !targetSession.itemExists(regionNode.getPath())) {
                    final Node siteNode = targetSession.getRootNode().getNode(this.site);
                    siteNode.addNode(regionNode.getName(), NodeTypes.Folder.NAME);
                }
            }
            targetSession.save();
        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    @Override
    public void updateRegion(String path) {
        //no implementation required
    }

    private boolean isSiteNotInReplicationScope(String path) {
        return StringUtils.isBlank(this.site) || !path.startsWith(PATH_SEPARATOR + this.site);
    }

    private String getRegionNameFromPath(final String path) {
        final String[] pathTokens = StringUtils.split(path, PATH_SEPARATOR);
        if (pathTokens.length < 2) {
            throw new ReplicationException(String.format("Replication failed. Path '%s' is invalid.", path));
        }
        return pathTokens[1];
    }

}
