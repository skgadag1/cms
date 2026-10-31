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

import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

/**
 * TBD.
 */
@Slf4j
public class EligibleVersionVoter extends AbstractBoolVoter<Version> {

    @Override
    public boolean boolVote(Version version) {
        if (version == null) return false;

        try {
            // can't remove the root version
            if (version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
                log.debug("Must not remove root version: {}", version);
                return false;
            }

            // can't remove the leaf version
            if (version.getLinearSuccessor() == null) {
                log.debug("Must not remove leaf version: {}", version);
                return false;
            }
        } catch (RepositoryException rx) {
            log.info("Couldn't check if version is eligible for removal: {}", rx.getMessage(), rx);
            return false;
        }

        return true;
    }
}
