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

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

/**
 * TBD.
 */
@Deprecated
public class TooManyVersionsVoter extends AbstractBoolVoter<VersionHistory> {

    private static final Logger logger = LoggerFactory.getLogger(TooManyVersionsVoter.class);

    protected int versionsThreshold = 5;

    @Override
    public boolean boolVote(VersionHistory history) {
        int count = 0;

        // count versions
        try {
            VersionIterator versions = history.getAllVersions();
            while (versions.hasNext()) {
                Version version = versions.nextVersion();

                // don't count the root version
                if(!version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                    ++count;
                }
            }
        } catch (RepositoryException rx) {
            logger.info("Error when determining number of versions: {}", rx.getMessage(), rx);
            return false;
        }

        boolean result = (count > versionsThreshold);

        if (result) {
            logger.debug("Too many versions {}, versions should be pruned", count);
        } else {
            logger.debug("Insufficient versions to prune {}", count);
        }

        return result;
    }

    public int getVersionsThreshold() {
        return versionsThreshold;
    }

    public void setVersionsThreshold(int versionsThreshold) {
        this.versionsThreshold = versionsThreshold;
    }

}
