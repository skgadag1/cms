/**
 * This file Copyright (c) 2021 Magnolia International
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

import info.magnolia.voting.voters.VoterSet;
import info.magnolia.cms.core.version.VersionManager;

import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Calendar;

/**
 * TBD.
 */
@Slf4j
public class NodeVersionVisitor implements ItemVisitor {

    protected final VersionManager versionManager;
    protected boolean noop;
    protected boolean recursive;
    protected VoterSet<Version> pruneVersion;
    protected List<Node> queue;

    public NodeVersionVisitor(final VersionManager versionManager, boolean noop, boolean recursive, VoterSet<Version> pruneVersion) {
        this.versionManager = versionManager;
        this.noop = noop;
        this.recursive = recursive;
        this.pruneVersion = pruneVersion;

        if (recursive) {
            queue = new ArrayList<>();
        }
    }

    @Override
    public void visit(Node node) throws RepositoryException {
        log.debug("Visiting node {} {}", node, node.getPath());

        if (versionManager == null) {
            log.info("Version manager not set, cannot clean up versions");
            return;
        }

        if (pruneVersion == null) {
            log.info("No pruning conditions specified, cannot clean up versions");
            return;
        }

        if (recursive) {
            // push children onto the head of the visit queue
            NodeIterator children = node.getNodes();
            while (children.hasNext()) {
                queue.add(0, children.nextNode());
            }
        }

        VersionHistory history = versionManager.getVersionHistory(node);

        if (history != null) {
            TreeMap<Calendar, Version> sortedByDate = new TreeMap<>();
            VersionIterator versions = history.getAllVersions();
            while (versions.hasNext()) {
                Version version = versions.nextVersion();
                // don't add the root version.
                if (!version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                    sortedByDate.put(version.getCreated(), version);
                }
            }

            for (Map.Entry<Calendar, Version> versionEntry : sortedByDate.descendingMap().entrySet()) {
                Version version = versionEntry.getValue();
                if (shouldPruneVersion(version)) {
                    if (!noop) {
                        log.debug("reaping version " + version);
                        history.removeVersion(version.getName());
                    } else {
                        log.debug("Test run: version " + version + " should be reaped");
                    }
                } else {
                    log.debug("Version " + version + " will be retained");
                }
            }
        } else {
            log.debug("No version history for {} {}, nothing to prune", node, node.getPath());
        }

        if (recursive && !queue.isEmpty()) {
            // get next node and continue our journey
            Node next = queue.remove(0);
            next.accept(this);
        }
    }

    @Override
    public void visit(Property property) {
        log.error("NodeVersionVisitor can't handle properties!");
    }

    protected boolean shouldPruneVersion(Version version) {
        log.debug("Determining if " + version + " should be pruned");
        return (pruneVersion.vote(version) > 0);
    }
}
