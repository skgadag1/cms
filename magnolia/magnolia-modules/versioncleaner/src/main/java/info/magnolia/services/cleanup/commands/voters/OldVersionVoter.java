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

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import info.magnolia.voting.voters.AbstractBoolVoter;

import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.JcrConstants;
import java.util.Calendar;

/**
 * TBD.
 */
@Slf4j
public class OldVersionVoter extends AbstractBoolVoter<Version> {

    protected long maxAge = 90L * 24L * 60L * 60L * 1000L;      // default max age: more than 90 days

    @Override
    public boolean boolVote(Version version) {
        log.debug("OldVersionVoter called for {}", version);

        Calendar limit = Calendar.getInstance();
        limit.setTimeInMillis(limit.getTimeInMillis() - maxAge);
        try {
            // never remove the root version
            if (version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                log.debug("Must not remove root version: {}", version);
                return false;
            }

            boolean result = version.getCreated().before(limit);

            if (result) {
                log.debug("Should remove {} created before {}", version, limit);
            } else {
                log.debug("Should not remove {} created after {}", version, limit);
            }

            return result;
        } catch (RepositoryException rx) {
            return false;
        }
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

}
