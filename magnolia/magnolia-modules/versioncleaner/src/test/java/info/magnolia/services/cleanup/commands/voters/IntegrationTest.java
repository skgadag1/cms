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

import info.magnolia.jcr.util.NodeTypes;

import org.junit.Test;

import info.magnolia.voting.voters.VoterSet;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;

/**
 * TBD.
 */
public class IntegrationTest extends VoterTest {

    @Test
    public void testAgeRemoval() throws RepositoryException {
        // set up complex pruning condition
        VoterSet<Version> pruneConditions = new VoterSet<Version>();
        pruneConditions.setOp("OR");

        NoModificationVoter duplicateVersion = new NoModificationVoter();
        pruneConditions.addVoter(duplicateVersion);

        VoterSet<Version> oldVersions = new VoterSet<Version>();
        oldVersions.setOp("AND");

        UnneededVersionVoter extraVersion = new UnneededVersionVoter();
        extraVersion.setVersionsThreshold(5);
        oldVersions.addVoter(extraVersion);

        OldVersionVoter oldVersion = new OldVersionVoter();
        // age threshold: 4.5 days
        oldVersion.setMaxAge((5L * 24L * 60L * 60L * 1000L) - (12L * 60L * 60L * 1000L));
        oldVersions.addVoter(oldVersion);

        pruneConditions.addVoter(oldVersions);

        // set up versions
        GregorianCalendar now = new GregorianCalendar();
        VoterTest.VersionSkeleton skeletons[] = new VoterTest.VersionSkeleton[10];
        List<Version> versions = new ArrayList<Version>();
        for (int i = 0; i < 10; ++i) {
            skeletons[i] = new VoterTest.VersionSkeleton();
            skeletons[i].name = "Version-" + (10 - i);

            VoterTest.NodeSkeleton targetNode = new VoterTest.NodeSkeleton();
            targetNode.properties = new HashMap<String,Object>();
            GregorianCalendar mark = new GregorianCalendar();
            mark.setTimeInMillis(now.getTimeInMillis() - (i * 24L * 60L * 60L * 1000L));
            skeletons[i].created = mark;
            targetNode.properties.put(NodeTypes.Created.CREATED, mark);
            targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark);
            skeletons[i].frozenNode = mockNode(targetNode);

            versions.add(mockVersion(skeletons[i]));
        }

        VoterTest.VersionHistorySkeleton historySkeleton = new VoterTest.VersionHistorySkeleton();
        historySkeleton.versions = versions;
        VersionHistory history = mockVersionHistory(historySkeleton);
        for (int i = 0; i < skeletons.length; ++i) {
            skeletons[i].linearPredecessor = (i < skeletons.length - 1) ? versions.get(i + 1) : null;
            skeletons[i].containingHistory = history;
        }

        for (int i = 0; i < versions.size(); ++i) {
            Version version = versions.get(i);
            if (i < 5) {
                assertTrue("version " + version.getName() + " should be retained", pruneConditions.vote(version) == 0);
            } else {
                assertTrue("version " + version.getName() + " should be deleted", pruneConditions.vote(version) > 0);
            }
        }
    }

    @Test
    public void testDuplicateRemoval() throws RepositoryException {
        // set up complex pruning condition
        VoterSet<Version> pruneConditions = new VoterSet<Version>();
        pruneConditions.setOp("OR");

        NoModificationVoter duplicateVersion = new NoModificationVoter();
        pruneConditions.addVoter(duplicateVersion);

        VoterSet<Version> oldVersions = new VoterSet<Version>();
        oldVersions.setOp("AND");

        UnneededVersionVoter extraVersion = new UnneededVersionVoter();
        extraVersion.setVersionsThreshold(5);
        oldVersions.addVoter(extraVersion);

        OldVersionVoter oldVersion = new OldVersionVoter();
        // age threshold: 30 days
        oldVersion.setMaxAge(30L * 24L * 60L * 60L * 1000L);
        oldVersions.addVoter(oldVersion);

        pruneConditions.addVoter(oldVersions);

        GregorianCalendar mark1 = new GregorianCalendar();
        List<Version> versions = new ArrayList<Version>();
        Version version;
        VoterTest.NodeSkeleton targetNode;
        VoterTest.VersionHistorySkeleton historySkeleton = new VoterTest.VersionHistorySkeleton();
        historySkeleton.versions = versions;

        VoterTest.VersionSkeleton s8 = new VoterTest.VersionSkeleton();
        s8.name = "Version-8";
        s8.created = mark1;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark1);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark1);
        s8.frozenNode = mockNode(targetNode);
        s8.containingHistory = mockVersionHistory(historySkeleton);
        version = mockVersion(s8);
        versions.add(version);

        GregorianCalendar mark2 = new GregorianCalendar();
        mark2.setTimeInMillis(mark1.getTimeInMillis() - 1000);

        VoterTest.VersionSkeleton s7 = new VoterTest.VersionSkeleton();
        s7.name = "Version-7";
        s7.created = mark2;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark2);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark2);
        s7.frozenNode = mockNode(targetNode);
        s7.containingHistory = mockVersionHistory(historySkeleton);
        s7.linearSuccessor = version;
        version = mockVersion(s7);
        versions.add(version);
        s8.linearPredecessor = version;

        VoterTest.VersionSkeleton s6 = new VoterTest.VersionSkeleton();
        s6.name = "Version-6";
        s6.created = mark2;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark2);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark2);
        s6.frozenNode = mockNode(targetNode);
        s6.containingHistory = mockVersionHistory(historySkeleton);
        s6.linearSuccessor = version;
        version = mockVersion(s6);
        versions.add(version);
        s7.linearPredecessor = version;

        VoterTest.VersionSkeleton s5 = new VoterTest.VersionSkeleton();
        s5.name = "Version-5";
        s5.created = mark2;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark2);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark2);
        s5.frozenNode = mockNode(targetNode);
        s5.containingHistory = mockVersionHistory(historySkeleton);
        s5.linearSuccessor = version;
        version = mockVersion(s5);
        versions.add(version);
        s6.linearPredecessor = version;

        GregorianCalendar mark3 = new GregorianCalendar();
        mark3.setTimeInMillis(mark2.getTimeInMillis() - 1000);

        VoterTest.VersionSkeleton s4 = new VoterTest.VersionSkeleton();
        s4.name = "Version-4";
        s4.created = mark3;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark3);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark3);
        s4.frozenNode = mockNode(targetNode);
        s4.containingHistory = mockVersionHistory(historySkeleton);
        s4.linearSuccessor = version;
        version = mockVersion(s4);
        versions.add(version);
        s5.linearPredecessor = version;

        VoterTest.VersionSkeleton s3 = new VoterTest.VersionSkeleton();
        s3.name = "Version-3";
        s3.created = mark3;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark3);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark3);
        s3.frozenNode = mockNode(targetNode);
        s3.containingHistory = mockVersionHistory(historySkeleton);
        s3.linearSuccessor = version;
        version = mockVersion(s3);
        versions.add(version);
        s4.linearPredecessor = version;

        VoterTest.VersionSkeleton s2 = new VoterTest.VersionSkeleton();
        s2.name = "Version-2";
        s2.created = mark3;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark3);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark3);
        s2.frozenNode = mockNode(targetNode);
        s2.containingHistory = mockVersionHistory(historySkeleton);
        s2.linearSuccessor = version;
        version = mockVersion(s2);
        versions.add(version);
        s3.linearPredecessor = version;

        GregorianCalendar mark4 = new GregorianCalendar();
        mark4.setTimeInMillis(mark3.getTimeInMillis() - 1000);

        VoterTest.VersionSkeleton s1 = new VoterTest.VersionSkeleton();
        s1.name = "Version-1";
        s1.created = mark4;
        targetNode = new VoterTest.NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, mark4);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, mark4);
        s1.frozenNode = mockNode(targetNode);
        s1.containingHistory = mockVersionHistory(historySkeleton);
        s1.linearSuccessor = version;
        version = mockVersion(s1);
        versions.add(version);
        s2.linearPredecessor = version;

        Set<String> retained = new HashSet<String>();
        retained.add(s8.name);
        retained.add(s5.name);
        retained.add(s2.name);
        retained.add(s1.name);

        for (Version v : versions) {
            assertTrue(v.getName() + " incorrectly determined", retained.contains(v.getName()) == (pruneConditions.vote(v) == 0));
        }
    }

}
