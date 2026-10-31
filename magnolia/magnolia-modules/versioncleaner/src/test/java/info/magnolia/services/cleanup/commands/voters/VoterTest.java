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

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Calendar;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TBD.
 */
public class VoterTest {

    protected VersionHistory mockVersionHistory() {
        return null;
    }

    protected Version mockVersion(final VersionSkeleton skeleton) throws RepositoryException {
        Version version = mock(Version.class);

        when(version.getFrozenNode()).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return skeleton.frozenNode;
            }
        });

        when(version.getName()).thenAnswer(new Answer() {
           public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
               return skeleton.name;
           }
        });

        when(version.getContainingHistory()).thenAnswer(new Answer() {
           public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
               return skeleton.containingHistory;
           }
        });

        when(version.getCreated()).thenAnswer(new Answer() {
           public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
               return skeleton.created;
           }
        });

        when(version.getLinearPredecessor()).thenAnswer(new Answer() {
           public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
               return skeleton.linearPredecessor;
           }
        });

        when(version.getLinearSuccessor()).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return skeleton.linearSuccessor;
            }
        });

        return version;
    }

    protected Node mockNode(final NodeSkeleton skeleton) throws RepositoryException {
        Node node = mock(Node.class);

        when(node.hasProperty(anyString())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                String key = (String) invocation.getArguments()[0];
                return skeleton.properties.containsKey(key);
            }
        });
        when(node.getProperty(anyString())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String key = (String) invocation.getArguments()[0];

                final Object value = skeleton.properties.get(key);
                Property property = mock(Property.class);
                when (property.getDate()).thenAnswer(new Answer() {
                   public Object answer(InvocationOnMock invocation) {
                       return value;
                   }
                });

                return property;
            }
        });
        return node;
    }

    protected VersionHistory mockVersionHistory(final VersionHistorySkeleton skeleton) throws RepositoryException {
        VersionHistory history = mock(VersionHistory.class);

        when(history.getAllVersions()).thenAnswer(new Answer() {
           public Object answer(InvocationOnMock invocation) {
               VersionIterator versionIterator = mock(VersionIterator.class);
               final Iterator listIterator = skeleton.versions.iterator();

               when(versionIterator.nextVersion()).thenAnswer(new Answer() {
                   public Object answer(InvocationOnMock invocation) {
                       return listIterator.next();
                   }
               });
               when(versionIterator.hasNext()).thenAnswer(new Answer() {
                   public Object answer(InvocationOnMock invocation) {
                       return listIterator.hasNext();
                   }
               });

               return versionIterator;
           }
        });

        return history;
    }

    protected class VersionSkeleton {
        protected String name;
        protected VersionHistory containingHistory;
        protected Calendar created;
        protected Node frozenNode;
        protected Version linearPredecessor;
        protected Version linearSuccessor;
    }

    protected class VersionHistorySkeleton {
        protected List<Version> versions;
    }

    protected class NodeSkeleton {
        protected Map<String,Object> properties;
    }
}
