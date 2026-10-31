/**
 * This file Copyright (c) 2017 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.services.diagnostic.commands;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.SystemContext;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;
import info.magnolia.services.VersionCleaner;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * TBD.
 */
@Slf4j
public class FindBadVersionsCommand extends MgnlCommand {

    public final static String VERSION_HISTORY_PATH = "/jcr:system/jcr:versionStorage";

    protected SystemContext systemContext;
    protected RepositoryManager repositoryManager;
    protected String versionWorkspace;
    protected VersionCleaner versionCleaner;

    @Inject
    public FindBadVersionsCommand(SystemContext systemContext, RepositoryManager repositoryManager, VersionCleaner versionCleaner) {
        super();
        this.systemContext = systemContext;
        this.repositoryManager = repositoryManager;
        this.versionCleaner = versionCleaner;
    }

    @Override
    public boolean execute(Context context) throws RepositoryException {
        Session session = getWorkspaceSession(context);

        if (session == null) {
            log.error("Couldn't get a version workspace session! Can't check version workspace!");
            return false;
        }

        log.debug("starting version analysis");
        Node rootVersion = session.getNode(VERSION_HISTORY_PATH);
        new BadVersionVisitor().visit(rootVersion);
        log.debug("done analyzing version store");

        return true;
    }

    protected Session getWorkspaceSession(Context context) {
        Session session = null;

        // look for version workspace in us
        if (this.versionWorkspace != null) {
            try {
                log.debug("attempting to get session for " + this.versionWorkspace + " from command config...");
                session = this.systemContext.getJCRSession(this.versionWorkspace);
                log.debug("got session from version workspace configured in command (" + this.versionWorkspace + "): " + session);
            } catch (RepositoryException rx) {
                log.info("Couldn't get session for version workspace " + this.versionWorkspace, rx);
            }

            // was configured, return session if found or not
            return session;
        } else {
            log.debug("version workspace is not configured in command, continuing...");
        }

        // look for version workspace in module
        if (versionCleaner.getVersionWorkspace() != null) {
            try {
                log.debug("attempting to get session for " + this.versionCleaner.getVersionWorkspace() + " from module config...");
                session = this.systemContext.getJCRSession(this.versionCleaner.getVersionWorkspace());
                log.debug("got session from version workspace configured in module (" + this.versionCleaner.getVersionWorkspace() + "): " + session);
            } catch (RepositoryException rx) {
                log.info("Couldn't get session for version workspace " + this.versionCleaner.getVersionWorkspace(), rx);
            }

            // was configured, return session if found or not
            return session;
        } else {
            log.debug("version workspace is not configured in module, continuing...");
        }

        // no workspace specified in us or the module config, try for mgnlVersion (VersionManager.VERSION_WORKSPACE)
        try {
            log.debug("attempting to get default version workspace " + VersionManager.VERSION_WORKSPACE);
            session = this.systemContext.getJCRSession(VersionManager.VERSION_WORKSPACE);

            if (session != null) {
                log.debug("got session from default version worspace (" + VersionManager.VERSION_WORKSPACE + "): " + session);
                return session;
            }
        } catch (RepositoryException rx) {
            log.info("Couldn't find default workspace " + VersionManager.VERSION_WORKSPACE + ", maybe running Magnolia 5.4 or later");
        }

        // last option: look for workspace mapping by physical repository name
        try {
            WorkspaceMappingDefinition definition = getWorkspaceMappingDefinition(VersionManager.VERSION_WORKSPACE);

            if (definition == null) {
                log.error("Could not find workspace mapping for " + VersionManager.VERSION_WORKSPACE);
                return session;
            }

            log.debug("attempting to get session for physical version workspace " + definition.getLogicalWorkspaceName());
            session = this.systemContext.getJCRSession(definition.getLogicalWorkspaceName());
            log.debug("got a session from physical repository (" + definition.getLogicalWorkspaceName() + "): " + session);
        } catch (RepositoryException rx) {
            log.info("Couldn't get 5.4 compatible version workspace");
        }

        return session;
    }

    protected WorkspaceMappingDefinition getWorkspaceMappingDefinition(String workspace) {
        for (WorkspaceMappingDefinition definition : this.repositoryManager.getWorkspaceMappings()) {
            if (definition.getPhysicalWorkspaceName().equals(workspace)) {
                return definition;
            }
        }

        return null;
    }

    public String getVersionWorkspace() {
        return versionWorkspace;
    }

    public void setVersionWorkspace(String versionWorkspace) {
        this.versionWorkspace = versionWorkspace;
    }

}
