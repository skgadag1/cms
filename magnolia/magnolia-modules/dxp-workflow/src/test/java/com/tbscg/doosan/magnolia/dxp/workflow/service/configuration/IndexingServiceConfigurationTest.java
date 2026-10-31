package com.tbscg.doosan.magnolia.dxp.workflow.service.configuration;

import com.tbscg.doosan.magnolia.dxp.workflow.DxpWorkflow;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEXER_SERVICE_CONFIG_PATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IndexingServiceConfigurationTest {

    @Mock
    private DxpWorkflow module;

    @InjectMocks
    private IndexingServiceConfiguration indexingServiceConfiguration;

    @Before
    public void setUp() throws IOException, RepositoryException {
        Components.setComponentProvider(new MockComponentProvider());
        mockContext();
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    public void shouldGetIndexerTableName_returnValidTableName() {
        when(module.getCmsIndexerTableName()).thenReturn("sample-dynamodb-table");
        assertEquals("sample-dynamodb-table", indexingServiceConfiguration.getIndexerTableName());
    }

    @Test
    public void shouldGetIndexableSites_returnValidSites() throws RepositoryException {
        assertEquals(List.of("sample-site", "somesite", "test_site"), indexingServiceConfiguration.getIndexableSites());
    }

    @Test
    public void shouldGetSubpages_returnEmptySubpages() throws RepositoryException {
        assertEquals(0, indexingServiceConfiguration.getSubpages("/sample-site/region").size());
    }

    private void mockContext() throws IOException, RepositoryException {
        final MockContext ctx = new MockContext();
        final MockSession configSession = mockConfigSession();
        ctx.addSession(WorkflowConstant.CONFIG_WORKSPACE, configSession);
        final Session websiteSession = mockWebsiteSession();
        ctx.addSession(WorkflowConstant.WEBSITE_WORKSPACE, websiteSession);
        MgnlContext.setInstance(ctx);
    }

    private MockSession mockConfigSession() throws IOException, RepositoryException {
        final String[] configJcr = {
                INDEXER_SERVICE_CONFIG_PATH + ".indexableSites=sample-site, somesite,,test_site,",
                INDEXER_SERVICE_CONFIG_PATH + ".indexerTable=sample-dynamodb-table"
        };
        return SessionTestUtil.createSession(WorkflowConstant.CONFIG_WORKSPACE, configJcr);
    }

    private MockSession mockWebsiteSession() throws IOException, RepositoryException {
        final String[] websiteJcr = {"/sample-site/region.@type=" + NodeTypes.Page.NAME};
        return SessionTestUtil.createSession(WorkflowConstant.WEBSITE_WORKSPACE, websiteJcr);
    }

}
