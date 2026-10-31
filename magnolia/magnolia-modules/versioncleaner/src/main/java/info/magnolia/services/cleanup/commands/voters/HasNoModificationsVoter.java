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
package info.magnolia.services.cleanup.commands.voters;

import info.magnolia.voting.voters.AbstractBoolVoter;
import info.magnolia.jcr.util.NodeTypes.LastModified;

import org.apache.jackrabbit.JcrConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import java.util.Calendar;

/**
 * TBD.
 */
@Deprecated
public class HasNoModificationsVoter extends AbstractBoolVoter<VersionHistory> {

    private static final Logger logger = LoggerFactory.getLogger(HasNoModificationsVoter.class);

    @Override
    public boolean boolVote(VersionHistory history) {
        try {
            VersionIterator versions = history.getAllVersions();
            while (versions.hasNext()) {
                Version version = versions.nextVersion();

                // never consider the root version
                if (version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                    continue;
                }

                Version predecessor = version.getLinearPredecessor();

                if (predecessor == null) {
                    continue;
                }

                Calendar currentLastModified = LastModified.getLastModified(version.getFrozenNode());
                Calendar prevLastModified = LastModified.getLastModified(predecessor.getFrozenNode());

                if (currentLastModified != null && currentLastModified.equals(prevLastModified)) {
                    logger.debug("current version {} should be removed", version);
                    return true;
                }
           }
        } catch (RepositoryException rx) {
            logger.info("Couldn't compare versions for versions without modifications: {}", rx.getMessage(), rx);
            return false;
        }

        logger.debug("no unmodified versions found");
        return false;
    }

}
