package com.tbscg.doosan.magnolia.dxp.core.setup.task;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.TaskExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil.getI18nProperties;
import static info.magnolia.jcr.util.PropertyUtil.renameProperty;

/**
 * I18n-aware task used for migrating a node-specific property to another.
 *
 * @author apuigescobar
 */
@Slf4j
public class I18nPropertyMigrationTask extends PropertyMigrationTask {

    private final String suffixSeparator;
    private final Pattern i18nPropertyPattern;

    public I18nPropertyMigrationTask(String taskName, String taskDescription,
                                     PropertyMigrationData propertyMigrationData, String suffixSeparator) {
        super(taskName, taskDescription, propertyMigrationData);
        this.suffixSeparator = suffixSeparator;
        this.i18nPropertyPattern = Pattern.compile(propertyMigrationData.getOldPropertyName() + suffixSeparator
                + "[a-z][a-z]");
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
        Session jcrSession = MgnlContext.getJCRSession(this.getPropertyMigrationData().getWorkspace());

        Node rootNode = jcrSession.getRootNode();
        Collection<Node> nodeCollection = NodeUtil.getNodes(rootNode, this.getPropertyMigrationData().getNodeType());

        for (Node node: nodeCollection) {
            this.migrateProperty(node);
        }

        jcrSession.save();
    }

    /**
     * Migrates i18n property, given old property it retrieves its i18n property values using
     * old property name and regexp pattern to create a new property using new i18n property name.
     *
     * @param node node whose i18n properties will be migrated
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    private void migrateProperty(Node node) throws RepositoryException {
        Collection<Property> i18nProperties = getI18nProperties(node,
                this.getPropertyMigrationData().getOldPropertyName(), this.i18nPropertyPattern);

        for (Property i18nProperty : i18nProperties) {
            this.handleI18nPropertyMigration(i18nProperty);
        }
    }

    /**
     * Migrates single i18n property.
     *
     * @param i18nProperty i18n property to migrate
     */
    private void handleI18nPropertyMigration(Property i18nProperty) {
        try {
            String[] tokenizedI18nPropertyName = StringUtils.split(i18nProperty.getName(), suffixSeparator);

            if (tokenizedI18nPropertyName != null && tokenizedI18nPropertyName.length > 1) {
                String languageSuffix = tokenizedI18nPropertyName[tokenizedI18nPropertyName.length - 1];
                String i18nNewPropertyName = this.getPropertyMigrationData().getNewPropertyName() + this.suffixSeparator
                        + languageSuffix;
                renameProperty(i18nProperty, i18nNewPropertyName);
            } else {
                renameProperty(i18nProperty, this.getPropertyMigrationData().getNewPropertyName());
            }
        } catch (Exception ex) {
            log.warn("Unable to migrate property {}", PropertyUtil.getPath(i18nProperty));
        }
    }

}
