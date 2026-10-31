package com.tbscg.doosan.magnolia.dxp.workflow.service.configuration;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.PathMappingServiceConfiguration.PATH_MAPPING_CONFIG_PATH;
import static org.hamcrest.MatcherAssert.assertThat;

public class PathMappingServiceConfigurationTest {

    private static final String SITE_1 = "site1";
    private static final String SITE_2 = "site2";
    private static final String SITE_3 = "site3";

    private PathMappingServiceConfiguration pathMappingServiceConfiguration;

    @Before
    public void setUp() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());
        pathMappingServiceConfiguration = new PathMappingServiceConfiguration();
        mockContext();
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    public void getMappableSites() throws Exception {
        // when
        List<String> mappableSites = pathMappingServiceConfiguration.getMappableSites();

        // then
        assertThat(mappableSites, Matchers.equalTo(List.of(SITE_1, SITE_2, SITE_3)));
    }

    private void mockContext() throws IOException, RepositoryException {
        final MockContext ctx = new MockContext();
        ctx.addSession(WorkflowConstant.CONFIG_WORKSPACE, mockConfigSession());
        MgnlContext.setInstance(ctx);
    }

    private MockSession mockConfigSession() throws IOException, RepositoryException {
        final String[] configJcr = {
                PATH_MAPPING_CONFIG_PATH + ".mappableSites=" + SITE_1 + ", " + SITE_2 + ",," + SITE_3
        };
        return SessionTestUtil.createSession(WorkflowConstant.CONFIG_WORKSPACE, configJcr);
    }


}
