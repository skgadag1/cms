package com.tbscg.doosan.magnolia.regions.replication;

/**
 * Defines an API for replicating actions executed on regions workspace to other workspaces or registries.
 *
 * @author Alejandro Puig Escobar
 */
public interface RegionReplicator {

    /**
     * Adds region site on a workspace or registry.
     *
     * @param path node path on regions workspace.
     * @throws ReplicationException in case that region site can't be added.
     */
    void addSite(String path) throws ReplicationException;

    /**
     * Adds region on a workspace or registry.
     *
     * @param path node path on regions workspace.
     * @throws ReplicationException in case that region can't be added.
     */
    void addRegion(String path) throws ReplicationException;

    /**
     * Moves region site on a workspace or registry.
     *
     * @param srcPath source path that should no longer exist.
     * @param dstPath destination path to create or operate with.
     * @throws ReplicationException in case that region site can't be renamed.
     */
    void renameSite(String srcPath, String dstPath) throws ReplicationException;

    /**
     * Moves region on a workspace or registry.
     *
     * @param srcPath source path that should no longer exist.
     * @param dstPath destination path to create or operate with.
     * @throws ReplicationException in case that region can't be replicated.
     */
    void renameRegion(String srcPath, String dstPath) throws ReplicationException;

    /**
     * Removes region site from a workspace or registry.
     *
     * @param path node path that should be removed.
     */
    void removeSite(String path) throws ReplicationException;

    /**
     * Removes region from a workspace or registry.
     *
     * @param path node path that should be removed.
     * @throws ReplicationException in case that region can't be removed.
     */
    void removeRegion(String path) throws ReplicationException;

    /**
     * Updates region site nodes or definitions on workspaces or registries.
     *
     * @param path node path to update from.
     * @throws ReplicationException in case that region site can't be updated.
     */
    void updateSite(String path) throws ReplicationException;

    /**
     * Updates region nodes or definitions on workspaces or registries.
     *
     * @param path node path to update from.
     * @throws ReplicationException in case that region can't be updated.
     */
    void updateRegion(String path) throws ReplicationException;

}
