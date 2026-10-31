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
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TBD.
 */
public class UnneededVersionVoterTest extends VoterTest {

    @Test
    public void testSmoke() {
        UnneededVersionVoter voter = new UnneededVersionVoter();
        assertTrue("default number of versions must be non-zero", voter.getVersionsThreshold() > 0);
    }

    @Test
    public void testRootVersion() throws RepositoryException {
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = JcrConstants.JCR_ROOTVERSION;
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        UnneededVersionVoter voter = new UnneededVersionVoter();

        // version is a root node, should return false
        assertFalse("version should not be unneeded", voter.boolVote(version));
    }

    @Test
    public void testNeededVersion() throws RepositoryException {
        UnneededVersionVoter voter = new UnneededVersionVoter();

        // case 1: more versions than threshold but current version is the newest
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = "ScoobyDoo";
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        GregorianCalendar now = new GregorianCalendar();

        ArrayList<Version> versions = new ArrayList<Version>();

        versions.add(version);

        // mock up version history
        for (int i = 0; i < voter.getVersionsThreshold()*2; ++i) {
            VersionSkeleton vskeleton = new VersionSkeleton();
            vskeleton.name = "Old Version " + i;
            GregorianCalendar created = new GregorianCalendar();
            created.setTimeInMillis(now.getTimeInMillis() - (i*1000));
            vskeleton.created = created;

            Version pversion = mockVersion(vskeleton);

            if (i == 0) {
                skeleton.linearPredecessor = pversion;
            }

            versions.add(pversion);
        }

        VersionHistorySkeleton vhskeleton = new VersionHistorySkeleton();
        vhskeleton.versions = versions;

        skeleton.containingHistory = mockVersionHistory(vhskeleton);

        assertFalse("version should not be unneeded", voter.boolVote(version));

        // case 2: fewer versions than threshold, current version is newest
        vhskeleton.versions = vhskeleton.versions.subList(0, voter.getVersionsThreshold() - 1);

        assertFalse("version should not be unneeded", voter.boolVote(version));

        // case 3: fewer versions than threshold, current version is oldest;
        Calendar oldest = versions.get(versions.size() - 1).getCreated();
        skeleton.created.setTimeInMillis(oldest.getTimeInMillis() - 1000);

        assertFalse("version should not be unneeded", voter.boolVote(version));
    }

    @Test
    public void testUnneededVersion() throws RepositoryException {
        UnneededVersionVoter voter = new UnneededVersionVoter();
        GregorianCalendar now = new GregorianCalendar();

        ArrayList<Version> versions = new ArrayList<Version>();

        // mock up version history
        for (int i = 0; i < voter.getVersionsThreshold()*2; ++i) {
            VersionSkeleton vskeleton = new VersionSkeleton();
            vskeleton.name = "New Version " + i;
            GregorianCalendar created = new GregorianCalendar();
            created.setTimeInMillis(now.getTimeInMillis() - (i*1000));
            vskeleton.created = created;

            Version pversion = mockVersion(vskeleton);

            versions.add(pversion);
        }

        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = "ScoobyDoo";
        skeleton.created = new GregorianCalendar();
        skeleton.created.setTimeInMillis(versions.get(versions.size() - 1).getCreated().getTimeInMillis() - 1000);

        VersionHistorySkeleton vhskeleton = new VersionHistorySkeleton();
        vhskeleton.versions = versions;
        skeleton.containingHistory = mockVersionHistory(vhskeleton);

        Version version = mockVersion(skeleton);
        versions.add(version);

        // case 1: more versions than threshold, current version is oldest
        assertTrue("version should be unneeded", voter.boolVote(version));

        // case 2: exactly versions at threshold, current version is oldest
        vhskeleton.versions = vhskeleton.versions.subList(vhskeleton.versions.size() - (voter.getVersionsThreshold() + 1), vhskeleton.versions.size());
        assertTrue("version be unneeded", voter.boolVote(version));
    }

}
