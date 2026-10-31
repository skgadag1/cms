package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.service.EventHandlingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.IndexingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping.DxpCustomPathMappingService;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.publishing.sender.Sender;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Abstract class used for testing DXP publishing commands.
 */
public abstract class DxpPublishingCommandTest {

    @Mock
    protected MockSession mockSession;

    @Mock
    protected Node node;

    @Mock
    protected SystemContext systemContext;

    @Mock
    protected ComponentProvider componentProvider;

    @Mock
    protected Sender sender;

    @Mock
    protected Context context;

    @Mock
    protected IndexingService indexingService;

    @Mock
    protected EventHandlingService eventHandlingService;

    @Mock
    protected DxpCustomPathMappingService dxpCustomPathMappingService;

    @Before
    public void setUp() throws RepositoryException {
        mockContext();
    }

    @After
    public void tearDown() {
        Components.setComponentProvider(null);
        MgnlContext.setInstance(null);
    }

    protected void mockContext() throws RepositoryException {
        when(systemContext.getJCRSession(any())).thenReturn(mockSession);
        when(mockSession.getNodeByIdentifier(anyString())).thenReturn(node);
        final MockComponentProvider componentProvider = new MockComponentProvider();
        componentProvider.setInstance(SystemContext.class, systemContext);
        Components.setComponentProvider(componentProvider);
    }

}
