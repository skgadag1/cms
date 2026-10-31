package com.tbscg.doosan.magnolia.dxp.core.setup.task;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collection;

/**
 * Task used for migrating a node-specific property to another.
 *
 * @author apuigescobar
 */
@Slf4j
public class PropertyMigrationTask extends AbstractTask {

    private final PropertyMigrationData propertyMigrationData;

    public PropertyMigrationTask(String taskName, String taskDescription, PropertyMigrationData propertyMigrationData) {
        super(taskName, taskDescription);
        this.propertyMigrationData = propertyMigrationData;
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            this.handleMigration();
        } catch (Exception e) {
            throw new TaskExecutionException("Unable to execute task " +  this.getName(), e);
        }
    }

    /**
     * Retrieves a list of nodes from a specific node type in a designated workspace and performs
     * property migration for each of those nodes.
     *
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    private void handleMigration() throws RepositoryException {
        Session jcrSession = MgnlContext.getJCRSession(this.propertyMigrationData.getWorkspace());

        Node rootNode = jcrSession.getRootNode();
        Collection<Node> nodeCollection = NodeUtil.getNodes(rootNode, this.propertyMigrationData.getNodeType());

        for (Node node: nodeCollection) {
            this.migrateProperty(node);
        }

        jcrSession.save();
    }

    /**
     * Migrates a property, given old property and new property names, it retrieves the property value using
     * old property name and creates a new property using new property name with old property value.
     *
     * @param node node whose property will be migrated
     */
    private void migrateProperty(Node node) {
        try {
            Property oldProperty = node.getProperty(this.propertyMigrationData.getOldPropertyName());
            PropertyUtil.renameProperty(oldProperty, this.propertyMigrationData.getNewPropertyName());
        } catch (Exception ex) {
            log.warn("Unable to migrate property for node {}", NodeUtil.getPath(node));
        }
    }

    public PropertyMigrationData getPropertyMigrationData() {
        return propertyMigrationData;
    }
}
