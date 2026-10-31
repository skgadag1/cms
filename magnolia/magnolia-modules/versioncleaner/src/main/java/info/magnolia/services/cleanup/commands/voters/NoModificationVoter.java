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

import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;


import java.util.Calendar;

/**
 * TBD.
 */
@Slf4j
public class NoModificationVoter extends AbstractBoolVoter<Version> {

    @Override
    public boolean boolVote(Version version) {
        try {
            log.debug("NoModificationVoter called for " + version);

            // never remove the root version
            if (version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                log.debug("skipping the root version {}", version);
                return false;
            }

            // never remove a leaf
            if (version.getLinearSuccessor() == null) {
                log.debug("skipping leaf version {}", version);
                return false;
            }

            Version predecessor = version.getLinearPredecessor();

            if (predecessor == null) {
                log.debug("skipping orphan version {}", version);
                return false;
            }

            Calendar currentLastMod = LastModified.getLastModified(version.getFrozenNode());
            Calendar previousLastMod = LastModified.getLastModified(predecessor.getFrozenNode());

            if (currentLastMod == null) {
                log.info("Version {} didn't return a last modification date!", version);
                return false;
            }

            if (previousLastMod == null) {
                log.info("Version {} didn't return a last modification date!", predecessor);
                return false;
            }

            boolean result = currentLastMod.equals(previousLastMod);

            if (result) {
                log.debug("Should remove version {}", version);
            } else {
                log.debug("Should not remove version {} current mod = {}", version, ((currentLastMod != null) ? currentLastMod.getTimeInMillis() : "N/A") + " previous mod = " + ((previousLastMod != null) ? previousLastMod.getTimeInMillis() : "N/A"));
            }

            return result;
        } catch (RepositoryException rx) {
            log.info("Error when determining version modification: " + rx.getMessage(), rx);
            return false;
        }
    }

}
