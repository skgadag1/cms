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

import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.JcrConstants;

import java.util.Set;
import java.util.HashSet;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

/**
 * TBD.
 */
@Slf4j
public class BadVersionVisitor implements ItemVisitor {
    public BadVersionVisitor() {
        // nothing to do for now?
    }

    @Override
    public void visit(Node node) throws RepositoryException {

        if (node.isNodeType(JcrConstants.NT_VERSIONHISTORY)) {
            VersionHistory history = (VersionHistory) node;
            log.debug("analyzing version history: " + history);

            Version rootVersion = history.getRootVersion();
            if (rootVersion == null) {
                log.warn("No root version for version history ()", history.getIdentifier());
            } else {
                log.debug("found root version from history: {}", rootVersion.getIdentifier());
            }

            Set<Version> rootVersions = new HashSet<Version>();
            VersionIterator versions = history.getAllVersions();
            while (versions.hasNext()) {
                Version version = versions.nextVersion();
                if (version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                    log.debug("found root version: {}", version.getIdentifier());
                    rootVersions.add(version);
                }
            }

            int errors = 0;

            if (rootVersions.size() == 0) {
                // no root versions
                log.warn("Did not find root version by name!");
            } else if (rootVersions.size() > 1) {
                // multiple root versions
                log.warn("Found multiple root versions by name!");
                for (Version version : rootVersions) {
                    log.warn("Found duplication root version: {} {}", version.getIdentifier(), version.getPath());
                    if (rootVersion != null && !version.getIdentifier().equals(rootVersion.getIdentifier())) {
                        log.warn("Duplicate root version {} {} does not match root version {} {}", version, version.getIdentifier(), rootVersion, rootVersion.getIdentifier());
                    }
                }
            } else {
                for (Version version : rootVersions) {
                    if (rootVersion != null) {
                        if (!version.getIdentifier().equals(rootVersion.getIdentifier())) {
                            log.warn("Root version {} {} found by name does not match root version {} {}", version, version.getIdentifier(), rootVersion, rootVersion.getIdentifier());
                        } else {
                            log.debug("Root version {} {} found by name matches root version {} {}", version, version.getIdentifier(), rootVersion, rootVersion.getIdentifier());
                        }
                    }
                }
            }
        } else {
            // not a version, iterate over other nodes
            log.debug("Node {} is not a version history, iterating...", node);
            NodeIterator children = node.getNodes();
            while(children.hasNext()){
                Node child = children.nextNode();
                child.accept(this);
            }
        }

    }

    @Override
    public void visit(Property property) throws RepositoryException {
        // Do nothing, not interested in properties
    }

}
