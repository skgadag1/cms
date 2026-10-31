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

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import java.util.GregorianCalendar;

/**
 * TBD.
 */
@Deprecated
public class HasOldVersionsVoter extends AbstractBoolVoter<VersionHistory> {

    protected long maxAge = 90L * 24L * 60L * 60L * 1000L;      // default max age: more than 90 days

    @Override
    public boolean boolVote(VersionHistory history) {
        GregorianCalendar limit = new GregorianCalendar();
        limit.setTimeInMillis(limit.getTimeInMillis() - maxAge);
        try {
            VersionIterator versions = history.getAllVersions();
            while (versions.hasNext()) {
                Version version = versions.nextVersion();

                // skip the root version
                if(version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                    continue;
                }

                if (version.getCreated().before(limit)) {
                    return true;
                }
             }
        } catch (RepositoryException rx) {
            return false;
        }

        return false;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
