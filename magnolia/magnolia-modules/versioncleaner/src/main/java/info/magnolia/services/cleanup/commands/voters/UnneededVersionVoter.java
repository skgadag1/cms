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

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.JcrConstants;

/**
 * TBD.
 */
@Slf4j
public class UnneededVersionVoter extends AbstractBoolVoter<Version> {

    protected int versionsThreshold = 5;

    @Override
    public boolean boolVote(Version version) {
        log.debug("UnneededVersionVoter called for " + version);

        int newerVersions = 0;
        try {
            // never remove the root version
            if (version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                log.debug("Must not remove root version {}", version);
                return false;
            }

            VersionHistory history = version.getContainingHistory();
            VersionIterator versions = history.getAllVersions();
            while(versions.hasNext()){
                Version v = versions.nextVersion();

                if (v.getCreated().after(version.getCreated())) {
                    ++newerVersions;
                }
            }
        } catch (RepositoryException rx) {
            return false;
        }

        boolean result = newerVersions >= versionsThreshold;

        if (result) {
            log.debug("Version {} should be reaped, has newer versions = {} threshold = {}", version, newerVersions, versionsThreshold);
        } else {
            log.debug("Version {} should be retained, has newer versions = {} threshold = {}", version, newerVersions, versionsThreshold);
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
