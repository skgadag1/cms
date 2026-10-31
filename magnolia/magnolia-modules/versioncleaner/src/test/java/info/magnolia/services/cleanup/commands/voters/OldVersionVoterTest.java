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

import org.apache.jackrabbit.JcrConstants;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TBD.
 */
public class OldVersionVoterTest extends VoterTest {

    @Test
    public void testSimpleAge() throws RepositoryException {
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = "foobar";
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        OldVersionVoter voter = new OldVersionVoter();

        assertFalse("version should not be too old", voter.boolVote(version));

        GregorianCalendar old = new GregorianCalendar();
        old.setTimeInMillis(old.getTimeInMillis() - voter.getMaxAge() - 10000);
        skeleton.created = old;

        assertTrue("version should be old enough", voter.boolVote(version));
    }

    @Test
    public void testRootVersion() throws RepositoryException {
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = JcrConstants.JCR_ROOTVERSION;
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        OldVersionVoter voter = new OldVersionVoter();

        // version not old enough and also a root node, should return false
        assertFalse("version should not be too old", voter.boolVote(version));

        GregorianCalendar old = new GregorianCalendar();
        int maxAge = (int) voter.getMaxAge();
        old.add(Calendar.MILLISECOND, -(maxAge + 10000));
        skeleton.created = old;

        // version is old enough and also a root node, should return false
        assertFalse("version should be old enough", voter.boolVote(version));
    }

}
