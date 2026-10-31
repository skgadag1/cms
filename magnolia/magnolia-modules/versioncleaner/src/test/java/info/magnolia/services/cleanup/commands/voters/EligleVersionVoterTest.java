/**
 * This file Copyright (c) 2019 Magnolia International
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

import org.apache.jackrabbit.JcrConstants;

import org.junit.Test;

import javax.jcr.version.Version;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TBD.
 */
public class EligleVersionVoterTest extends VoterTest {
    @Test
    public void testSmoke() {
        EligibleVersionVoter voter = new EligibleVersionVoter();
        assertNotNull(voter);

        assertFalse("expected false with null version", voter.boolVote(null));
    }

    @Test
    public void testRootVersion() throws Exception {
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = JcrConstants.JCR_ROOTVERSION;
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        EligibleVersionVoter voter = new EligibleVersionVoter();

        assertFalse("expected false with root version", voter.boolVote(version));
    }

    @Test
    public void testLeafVersion() throws Exception {
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = "IAmALeafyTree";
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        EligibleVersionVoter voter = new EligibleVersionVoter();

        assertFalse("expected false with leaf version", voter.boolVote(version));
    }

    @Test
    public void testEligibleVersion() throws Exception {
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = "IAmALeafyTree";
        skeleton.created = new GregorianCalendar();
        Version leafVersion = mockVersion(skeleton);

        skeleton = new VersionSkeleton();
        skeleton.name = "DeleteMe";
        skeleton.linearSuccessor = leafVersion;
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        EligibleVersionVoter voter = new EligibleVersionVoter();

        assertTrue("expected false with eligible version", voter.boolVote(version));
    }
}
