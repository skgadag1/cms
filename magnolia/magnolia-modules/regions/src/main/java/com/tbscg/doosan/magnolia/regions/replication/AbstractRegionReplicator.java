package com.tbscg.doosan.magnolia.regions.replication;

import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import info.magnolia.context.MgnlContext;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Abstract replicator class which contains the definition of methods that operate on
 * JCR workspaces structure as well as default implementations for some of these operations.
 *
 * @author Alejandro Puig Escobar
 */
abstract class AbstractRegionReplicator {

    protected final String workspace;

    public AbstractRegionReplicator(String workspace) {
        this.workspace = workspace;
    }

    /**
     * Removes site node and its structure in case that region nodes have no children.
     *
     * @param siteNode site node to remove.
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    void removeStructure(Node siteNode) throws RepositoryException {

        if (siteNode.hasNodes()) {

            NodeIterator regionNodesIt = siteNode.getNodes();

            while (regionNodesIt.hasNext()) {

                Node regionNode = regionNodesIt.nextNode();

                if (!regionNode.hasNodes()) {
                    regionNode.remove();
                }
            }

        }

        if (!siteNode.hasNodes()) {
            siteNode.remove();
        }
    }

    /**
     * Moves a node and its children recursively to a destination path in a specific workspace.
     *
     * @param srcPath source node path.
     * @param dstPath destination node path.
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    void moveStructure(String srcPath, String dstPath) throws RepositoryException{
        Session jcrSession = MgnlContext.getJCRSession(workspace);

        jcrSession.move(srcPath, dstPath);

        Node rootSiteNode = jcrSession.getNode(dstPath);
        rootSiteNode.setProperty(JcrConstants.JCR_NAME, rootSiteNode.getName());

        jcrSession.save();
    }

    /**
     * Given a <code>RegionSite</code>, this method replicates its structure.
     *
     * @param regionSite <code>RegionSite</code> whose structure has to be replicated.
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    abstract void createSiteStructure(RegionSite regionSite) throws RepositoryException;

    /**
     * Given a <code>Region</code>, this method replicates its structure.
     *
     * @param regionSite <code>Region</code> whose structure has to be replicated.
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    abstract void createRegionStructure(RegionSite regionSite, Region region) throws RepositoryException;

}
