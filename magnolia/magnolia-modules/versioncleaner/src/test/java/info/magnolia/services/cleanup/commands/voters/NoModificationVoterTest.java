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

import org.apache.jackrabbit.JcrConstants;

import org.junit.Test;

import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TBD.
 */
public class NoModificationVoterTest extends VoterTest {

    @Test
    public void testRootVersion() throws RepositoryException {
        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = JcrConstants.JCR_ROOTVERSION;
        skeleton.created = new GregorianCalendar();
        Version version = mockVersion(skeleton);

        NoModificationVoter voter = new NoModificationVoter();

        // version is a root node, should return false
        assertFalse("version should eligible", voter.boolVote(version));
    }

    @Test
    public void testNoModification() throws RepositoryException {
        GregorianCalendar now = new GregorianCalendar();

        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = "TargetVersion";

        NodeSkeleton targetNode = new NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, now);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, now);

        skeleton.frozenNode = mockNode(targetNode);

        VersionSkeleton pskeleton = new VersionSkeleton();
        pskeleton.name = "PredecessorVersion";

        NodeSkeleton predecessorNode = new NodeSkeleton();
        predecessorNode.properties = new HashMap<String,Object>();
        predecessorNode.properties.put(NodeTypes.Created.CREATED, now);
        predecessorNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, now);

        pskeleton.frozenNode = mockNode(predecessorNode);

        VersionSkeleton sskeleton = new VersionSkeleton();
        sskeleton.name = "SuccessorVersion";

        NodeSkeleton successorNode = new NodeSkeleton();
        successorNode.properties = new HashMap<String,Object>();
        successorNode.properties.put(NodeTypes.Created.CREATED, now);
        successorNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, now);

        sskeleton.frozenNode = mockNode(successorNode);

        skeleton.linearPredecessor = mockVersion(pskeleton);
        skeleton.linearSuccessor = mockVersion(sskeleton);

        Version target = mockVersion(skeleton);

        NoModificationVoter voter = new NoModificationVoter();

        assertTrue("modification date unchanged", voter.boolVote(target));
    }

    @Test
    public void testModification() throws RepositoryException {
        GregorianCalendar now = new GregorianCalendar();

        VersionSkeleton skeleton = new VersionSkeleton();
        skeleton.name = "TargetVersion";

        NodeSkeleton targetNode = new NodeSkeleton();
        targetNode.properties = new HashMap<String,Object>();
        targetNode.properties.put(NodeTypes.Created.CREATED, now);
        targetNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, now);

        skeleton.frozenNode = mockNode(targetNode);

        VersionSkeleton pskeleton = new VersionSkeleton();
        pskeleton.name = "PredecessorVersion";

        GregorianCalendar old = new GregorianCalendar();
        old.setTimeInMillis(now.getTimeInMillis() - 1);
        NodeSkeleton predecessorNode = new NodeSkeleton();
        predecessorNode.properties = new HashMap<String,Object>();
        predecessorNode.properties.put(NodeTypes.Created.CREATED, old);
        predecessorNode.properties.put(NodeTypes.LastModified.LAST_MODIFIED, old);

        pskeleton.frozenNode = mockNode(predecessorNode);

        skeleton.linearPredecessor = mockVersion(pskeleton);

        Version target = mockVersion(skeleton);

        NoModificationVoter voter = new NoModificationVoter();

        assertFalse("modification date older", voter.boolVote(target));
    }

}
