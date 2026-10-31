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

import javax.jcr.Session;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import info.magnolia.voting.voters.VoterSet;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.apache.jackrabbit.JcrConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

/**
 * TBD.
 */
@Slf4j
public class VersionHistoryVisitor implements ItemVisitor {

    protected final static String LAST_NODE_PATH = "lastNodePath";

    int visitLimit;
    int visited;
    Session session;
    VoterSet<Version> pruneVersion;
    boolean noop;
    boolean archive;
    String archiveDir;
    Map<String,String> visitHistory;
    List<Node> visitQueue;

    public VersionHistoryVisitor(Session session,
                                 List<Node> visitQueue,
                                 Map<String,String> visitHistory,
                                 VoterSet<Version> pruneVersion,
                                 int visitLimit,
                                 boolean noop,
                                 boolean archive,
                                 String archiveDir) {
        this.session = session;
        this.pruneVersion = pruneVersion;
        this.visitLimit = (visitLimit < 0) ? Integer.MAX_VALUE : visitLimit;
        this.noop = noop;
        this.archive = archive;
        this.archiveDir = archiveDir;
        this.visitQueue = (visitQueue != null) ? new ArrayList<>(visitQueue) : new ArrayList<>();
        this.visitHistory = visitHistory;
    }

    @Override
    public void visit(Node node) throws RepositoryException {

        if (visited >= visitLimit) {
            log.info("Visited " + visited + " nodes, reached visit limit");
            return;
        }

        if (node.isNodeType(JcrConstants.NT_VERSIONHISTORY)) {
            prune((VersionHistory) node);
        }

        // note our visit
        ++visited;
        visitHistory.put(VersionCleanupCommand.LAST_VISITED_PATH_KEY, node.getPath());

        if (visited >= visitLimit) {
            log.debug("reached visit limit, stopping");
            return;
        }

        // push children onto the head of the visit queue retaining their order
        NodeIterator children = node.getNodes();
        int i = 0;
        while (children.hasNext()) {
            visitQueue.add(i++, children.nextNode());
        }

        if (visitQueue.size() == 0) {
            // all done, remove last visited reference for next run
            visitHistory.remove(VersionCleanupCommand.LAST_VISITED_PATH_KEY);
            log.info("All nodes visited, all done!");
        } else {
            // get next node and continue our journey
            Node next = visitQueue.remove(0);
            next.accept(this);
        }
    }

    @Override
    public void visit(Property property) {
        log.info("Can't use {} to visit properties!", VersionHistoryVisitor.class.getName());
    }

    protected boolean shouldPruneVersion(Version version) {
        log.debug("Determining if " + version + " should be pruned");
        return (pruneVersion.vote(version) > 0);
    }

    protected void prune(VersionHistory history) {
        try {
            // compile a map of all versions
            VersionIterator versions = history.getAllVersions();
            TreeMap<Calendar, Version> sortedByDate = new TreeMap<>();

            while (versions.hasNext()) {
                Version version = versions.nextVersion();
                // don't add the root version.
                if (!version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                    sortedByDate.put(version.getCreated(), version);
                }
            }

            int reaped = 0;
            for (Map.Entry<Calendar, Version> versionEntry : sortedByDate.descendingMap().entrySet()) {
                Version version = versionEntry.getValue();
                if (shouldPruneVersion(version)) {
                    if (!noop) {
                        if (archive) {
                            archiveVersion(version);
                        }

                        log.debug("reaping version " + version);

                        history.removeVersion(version.getName());
                        ++reaped;
                    } else {
                        log.info("Version " + version + " should be reaped");
                    }
                } else {
                    log.debug("Version " + version + " will be retained");
                }
            }

        } catch (RepositoryException rx) {
            log.warn("Could not prune versions of " + history, rx);
        }
    }

    protected void archiveVersion(Version version) throws RepositoryException {
        log.debug("archiving version " + version);
        File dir = new File(archiveDir);
        String path = StringUtils.removeStart(version.getPath(), "/jcr:system/jcr:versionStorage/");
        File file = new File(archiveDir, path + ".xml");
        file.getParentFile().mkdirs();
        FileOutputStream out;

        try {
            out = new FileOutputStream(file);
            session.exportDocumentView(version.getPath(), out , false, false);
            out.close();
        } catch (IOException iox) {
            log.error("Can't write archive file", iox);
        }
    }

}
