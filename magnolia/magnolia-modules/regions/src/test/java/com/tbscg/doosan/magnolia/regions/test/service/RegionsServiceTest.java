package com.tbscg.doosan.magnolia.regions.test.service;

import com.tbscg.doosan.magnolia.regions.converter.NodeToRegionSiteConverter;
import com.tbscg.doosan.magnolia.regions.converter.RegionToSitesConverter;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.tbscg.doosan.magnolia.regions.service.impl.RegionsServiceImpl;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.tbscg.doosan.magnolia.regions.util.RegionsConstants.REGIONS_WORKSPACE;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RegionsServiceTest {

    private RegionsService regionsService;

    @Mock
    private NodeToRegionSiteConverter nodeToRegionSiteConverter;

    @Mock
    private RegionToSitesConverter regionToSitesConverter;

    @Before
    public void setUp() throws IOException, RepositoryException {
        regionsService = new RegionsServiceImpl(nodeToRegionSiteConverter, regionToSitesConverter, getMockContext());
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void shouldGetRegionsForSite_returnAllJCRegions() throws RepositoryException {
        assertEquals(Arrays.asList("na", "eu", "kr"), regionsService.getRegionsForSite("site"));
    }

    @Test
    public void shouldGetLanguagesForSiteAndRegion_returnAllJCRLanguagesForOneLanguageRegion() throws RepositoryException {
        assertEquals(Collections.singletonList("en"), regionsService.getLanguagesForSiteAndRegion("site", "na"));
    }

    @Test
    public void shouldGetLanguagesForSiteAndRegion_returnAllJCRLanguagesForTwoLanguagesRegion() throws RepositoryException {
        assertEquals(Arrays.asList("en", "pl"), regionsService.getLanguagesForSiteAndRegion("site", "eu"));
    }

    @Test
    public void shouldGetLanguagesForSiteAndRegion_returnNoJCRLanguagesForZeroLanguagesRegion() throws RepositoryException {
        assertEquals(Collections.emptyList(), regionsService.getLanguagesForSiteAndRegion("site", "kr"));
    }

    private SystemContext getMockContext() throws IOException, RepositoryException {
        final MockSession session = mockSession();
        final MockContext ctx = new MockContext();
        ctx.addSession(REGIONS_WORKSPACE, session);
        return ctx;
    }

    private MockSession mockSession() throws IOException, RepositoryException {
        final String[] regionsJcr = {
                "/site/na.@type=" + RegionsConstants.NodeTypes.Region.NAME,
                "/site/na/en.@type=" + RegionsConstants.NodeTypes.Language.NAME,
                "/site/eu.@type=" + RegionsConstants.NodeTypes.Region.NAME,
                "/site/eu/en.@type=" + RegionsConstants.NodeTypes.Language.NAME,
                "/site/eu/pl.@type=" + RegionsConstants.NodeTypes.Language.NAME,
                "/site/kr.@type=" + RegionsConstants.NodeTypes.Region.NAME};
        return SessionTestUtil.createSession(REGIONS_WORKSPACE, regionsJcr);
    }
}
