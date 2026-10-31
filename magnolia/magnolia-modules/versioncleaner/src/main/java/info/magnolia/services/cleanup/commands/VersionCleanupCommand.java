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
package info.magnolia.services.cleanup.commands;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.repository.definition.WorkspaceMappingDefinition;
import info.magnolia.services.VersionCleaner;
import info.magnolia.voting.voters.VoterSet;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.NodeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;

import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import lombok.extern.slf4j.Slf4j;

import static info.magnolia.init.MagnoliaConfigurationProperties.MAGNOLIA_REPOSITORIES_HOME;

/**
 * TBD.
 */
@Slf4j
public class VersionCleanupCommand extends MgnlCommand {

    public final static String ARCHIVE_DIR_PROPERTY = "magnolia.cleanup.dir";

    public final static String VERSION_HISTORY_PATH = "/jcr:system/jcr:versionStorage";

    public final static String VERSION_WORKSPACE_NAME = VersionCleanupCommand.class.getName() + ".workspaceName";

    protected final static String VISIT_HISTORY_FILE = "versionCleanupHistory.ser";
    public final static String LAST_VISITED_PATH_KEY = "lastVisitedPath";

    protected VersionCleaner versionCleaner;
    protected SystemContext systemContext;
    protected RepositoryManager repositoryManager;

    protected String versionWorkspace;
    protected int visitLimit = 1000;
    protected VoterSet<Version> pruneVersion = new VoterSet<>();
    protected boolean noop = false;
    protected boolean archive = false;
    protected String archiveDir;

    protected Map<String,String> visitHistory;

    @Inject
    public VersionCleanupCommand(VersionCleaner versionCleaner, SystemContext systemContext, RepositoryManager repositoryManager, MagnoliaConfigurationProperties configurationProperties) {
        super();
        this.versionCleaner = versionCleaner;
        this.systemContext = systemContext;
        this.repositoryManager = repositoryManager;

        if (configurationProperties.hasProperty(ARCHIVE_DIR_PROPERTY)) {
            this.archiveDir = configurationProperties.getProperty(ARCHIVE_DIR_PROPERTY);
        } else if (configurationProperties.hasProperty(MAGNOLIA_REPOSITORIES_HOME)) {
            this.archiveDir = configurationProperties.getProperty(MAGNOLIA_REPOSITORIES_HOME);
        } else {
            this.archiveDir = "/tmp";
        }
    }

    @Override
    public boolean execute(Context context) throws RepositoryException {
        Session session = getWorkspaceSession(context);

        if (session == null) {
            log.error("Couldn't get a version workspace session! Can't clean up versions!");
            return false;
        }

        loadHistory();

        // figure out where to start
        Node node;
        List<Node> visitQueue = new ArrayList<>();

        if (visitLimit == Integer.MAX_VALUE || visitLimit < 0) {
            // start at the top
            log.debug("no visit limit, start at root version node");
            node = session.getNode(VERSION_HISTORY_PATH);
        } else if (visitHistory.containsKey(LAST_VISITED_PATH_KEY)) {
            try {
                // start at last visited node
                node = session.getNode(visitHistory.get(LAST_VISITED_PATH_KEY));

                // NodeVisitor does a depth-first walk of the version workspace. We need to restart the walk with
                // the following siblings of the node where we stopped and any following siblings of any ancestors.

                restoreQueue(visitQueue, node);
            } catch (RepositoryException rx) {
                log.warn("Couldn't find last visited node {}, starting at root version node", visitHistory.get(LAST_VISITED_PATH_KEY));
                node = session.getNode(VERSION_HISTORY_PATH);
            }
        } else {
            log.info("Last ditch default, start at root version node");
            node = session.getNode(VERSION_HISTORY_PATH);
        }

        VersionHistoryVisitor visitor = new VersionHistoryVisitor(session, visitQueue, visitHistory, pruneVersion, visitLimit, noop, archive, archiveDir);
        visitor.visit(node);

        // all done
        session.save();

        saveHistory();

        return true;
    }

    protected void restoreQueue(List<Node> queue, Node node) {
        if (node == null) return;

        try {
            // queue up following siblings
            for (Node sibling : NodeUtil.getSiblingsAfter(node)) {
                queue.add(sibling);
            }

            restoreQueue(queue, node.getParent());
        } catch (RepositoryException rx) {
            log.info("Exception when restoring visit queue: {}", rx.getMessage(), rx);
        }
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

    protected void loadHistory() {
        if (visitLimit == Integer.MAX_VALUE || visitLimit < 0) {
            // unlimited run, don't bother loading history
            log.debug("Cleaning all versions, will not use history");
            visitHistory = new HashMap<>();
        } else {
            // set up for limited run
            File histories = new File(getArchiveDir(), VISIT_HISTORY_FILE);

            if (histories.exists()) {
                try {
                    log.debug("attempting to load visit history {}", histories);
                    FileInputStream fis = new FileInputStream(histories);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    visitHistory = (Map) ois.readObject();
                    ois.close();
                    fis.close();
                } catch (IOException iox) {
                    log.warn("Could not read saved version histories: " + iox.getMessage());
                    log.warn("Will not skip previously pruned versions");
                    visitLimit = Integer.MAX_VALUE;
                    visitHistory = new HashMap<>();
                } catch (ClassNotFoundException cnfx) {
                    log.warn("Could not restore saved version histories: " + cnfx.getMessage());
                    log.warn("Will not skip previously pruned versions");
                    visitLimit = Integer.MAX_VALUE;
                    visitHistory = new HashMap<>();
                }
            } else {
                log.info("No version histories found at " + histories);
                visitHistory = new HashMap<>();
            }
        }
    }

    public void saveHistory() {
        if (visitLimit == Integer.MAX_VALUE) {
            visitHistory.clear();
        }

        try {
            File histories = new File(getArchiveDir(), VISIT_HISTORY_FILE);
            histories.createNewFile();

            log.info("Saving cleanup history to {}", histories.getAbsolutePath());

            FileOutputStream fos = new FileOutputStream(histories);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(visitHistory);
            oos.close();
            fos.close();
        } catch (IOException iox) {
            log.warn("Could not save visited histories: " + iox.getMessage());
        }
    }

    public String getVersionWorkspace() {
        return versionWorkspace;
    }

    public void setVersionWorkspace(String versionWorkspace) {
        this.versionWorkspace = versionWorkspace;
    }

    @Deprecated
    public VoterSet<VersionHistory> getPruneHistory() {
        log.error("pruneHistory property is no longer supported");
        return null;
    }

    @Deprecated
    public void setPruneHistory(VoterSet<VersionHistory> pruneHistory) {
        log.error("pruneHistory property is no longer supported");
    }

    public VoterSet<Version> getPruneVersion() {
        return pruneVersion;
    }

    public void setPruneVersion(VoterSet<Version> pruneVersion) {
        this.pruneVersion = pruneVersion;
    }

    public int getVisitLimit() {
        return visitLimit;
    }

    public void setVisitLimit(int visitLimit) {
        this.visitLimit = visitLimit;
    }

    public boolean isNoop() {
        return noop;
    }

    public void setNoop(boolean noop) {
        this.noop = noop;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public String getArchiveDir() {
        return archiveDir;
    }

    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }

}
