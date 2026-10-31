package com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PathMapping;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.PathMappingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.CustomPathMappingException;
import com.tbscg.doosan.magnolia.regions.i18n.RegionsI18nContentSupport;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;
import info.magnolia.test.mock.jcr.MockSession;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class DxpCustomPathMappingServiceTest {

    private static final String DEFAULT_TEST_PATH = "/bobcat/test/path";
    private static final String NON_BOBCAT_TEST_PATH = "/test";
    private static final String WORKSPACE = "website";
    private static final String PAGE_TYPE = NodeTypes.Page.NAME;

    @Mock
    private PathMappingServiceConfiguration pathMappingServiceConfiguration;

    @Mock
    private DxpCustomPathResolver dxpCustomPathResolver;

    @Mock
    private RegionsService regionsService;

    private Session mockSession;

    @Before
    public void setUp() throws RepositoryException {
        Components.setComponentProvider(new MockComponentProvider());
        mockSession = mockPageSession();
        mockContext();
        MockitoAnnotations.initMocks(this);
    }

    private void mockContext() {
        final MockContext ctx = new MockContext();
        final MockSession regionsSession = mockRegionSession();
        ctx.addSession(WorkflowConstant.WEBSITE_WORKSPACE, mockSession);
        ctx.addSession(WorkflowConstant.REGIONS_WORKSPACE, regionsSession);
        MgnlContext.setInstance(ctx);
    }

    private MockSession mockPageSession() throws RepositoryException {
        final MockSession mockSession = new MockSession("website");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode bobcat = new MockNode("bobcat", NodeTypes.Page.NAME);
        MockNode nonBobcat = new MockNode("test", NodeTypes.Page.NAME);
        MockNode test = new MockNode("test", NodeTypes.Page.NAME);
        MockNode path = new MockNode("path", NodeTypes.Page.NAME);
        path.setProperty("customNodeName_en", "pathEn");
        test.addNode(path);
        bobcat.addNode(test);
        parent.addNode(bobcat);
        parent.addNode(nonBobcat);
        return mockSession;
    }

    private MockSession mockRegionSession() {
        final MockSession mockSession = new MockSession("regions");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode bobcat = new MockNode("bobcat", "dxp:website");
        MockNode nonBobcat = new MockNode("test", "dxp:website");
        MockNode test = new MockNode("eu", "dxp:region");
        MockNode en = new MockNode("en", "dxp:language");
        MockNode fr = new MockNode("fr","dxp:language");
        MockProperty isDefault = new MockProperty("isDefault","true", en);
        en.addProperty(isDefault);
        test.addNode(fr);
        test.addNode(en);
        bobcat.addNode(test);
        parent.addNode(bobcat);
        parent.addNode(nonBobcat);
        return mockSession;
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(dxpCustomPathResolver);
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    @Parameters({
            "true",
            "false"})
    public void updateMappings_defaultFlow(final boolean recursive) throws RepositoryException, RegionNotFoundException {
        // given
        PathMapping pm = mock(PathMapping.class);

        when(dxpCustomPathResolver.resolveCustomPaths(any(Node.class))).thenReturn(pm);

        // when
        defaultDxpCustomPathMappingService().updateMappings(DEFAULT_TEST_PATH, WORKSPACE, recursive);

        // then
        verify(dxpCustomPathResolver, times(1)).resolveCustomPaths(any(Node.class));
                    }

    @Test
    @Parameters({
            "true",
            "false"})
    public void updateMappings_shouldNotRunWhenNoMappableWorkspace(final boolean recursive) throws RepositoryException, RegionNotFoundException {
        // given
        PathMapping pm = mock(PathMapping.class);
        Node node = mock(Node.class);

        when(dxpCustomPathResolver.resolveCustomPaths(node)).thenReturn(pm);

        // when
        defaultDxpCustomPathMappingService().updateMappings(DEFAULT_TEST_PATH, EMPTY, recursive);

        // then
        verify(dxpCustomPathResolver, never()).resolveCustomPaths(same(node));
    }

    @Test
    @Parameters({
            "true",
            "false"})
    public void updateMappings_shouldNotRunWhenNoMappablePath(final boolean recursive) throws RepositoryException, RegionNotFoundException {
        // given
        PathMapping pm = mock(PathMapping.class);
        Node node = mock(Node.class);

        when(dxpCustomPathResolver.resolveCustomPaths(node)).thenReturn(pm);

        // when
        defaultDxpCustomPathMappingService().updateMappings(NON_BOBCAT_TEST_PATH, WORKSPACE, recursive);

        // then
        verify(dxpCustomPathResolver, never()).resolveCustomPaths(same(node));
    }

    @Test
    @Parameters({
            "true",
            "false"})
    public void updateMappings_shouldThrowException_onDxpCustomPathResolverFail(final boolean recursive) throws RepositoryException, RegionNotFoundException {
        // given
        final CustomPathMappingException error = new CustomPathMappingException("error");

        when(dxpCustomPathResolver.resolveCustomPaths(any(Node.class))).thenThrow(error);

        // when
        final CustomPathMappingException actual = assertThrows(CustomPathMappingException.class,
                () -> defaultDxpCustomPathMappingService().updateMappings(DEFAULT_TEST_PATH, WORKSPACE, recursive));

        // then
        assertThat(actual.getMessage(), not(emptyOrNullString()));
        verify(dxpCustomPathResolver, times(1)).resolveCustomPaths(any(Node.class));
    }

    @Test
    public void updatePageWithCustomName_shouldUpdatePageWithCustomName() throws RepositoryException, RegionNotFoundException {
        // given
        Node node = mockSession.getNode(DEFAULT_TEST_PATH);

        // when
        defaultDxpCustomPathMappingService().updatePageWithCustomName(node, DEFAULT_TEST_PATH, PAGE_TYPE, WORKSPACE);

        //then
        Node nodeAfter = mockSession.getNode(DEFAULT_TEST_PATH);
        Node customNameForLang = nodeAfter.getNode("customNameForLang");
        String customLinkEn = customNameForLang.getNode("en").getProperty("customName").getString();
        String customLinkFr = customNameForLang.getNode("fr").getProperty("customName").getString();
        String region = customNameForLang.getProperty("region").getString();
        assertEquals(2, NodeUtil.getDirectNodes(customNameForLang).size());
        assertEquals("pathEn", customLinkEn);
        assertEquals("path", customLinkFr);
        assertEquals("eu", region);
    }

    @Test
    public void updatePageWithCustomName_shouldUpdatePageWithCustomNameForFallbackLang() throws RepositoryException, RegionNotFoundException {
        // given
        Node node = mockSession.getNode(DEFAULT_TEST_PATH);
        doReturn(Arrays.asList("bobcat")).when(pathMappingServiceConfiguration).getMappableSites();
        RegionsI18nContentSupport i18nContentSupport = new RegionsI18nContentSupport();

        Region region = new Region();
        region.setI18n(i18nContentSupport);
        doReturn(region).when(regionsService).getRegionFromPath(DEFAULT_TEST_PATH);
        doReturn(region).when(regionsService).getRegionFromPath(DEFAULT_TEST_PATH);
        DxpCustomPathMappingService service = new DxpCustomPathMappingService(regionsService, dxpCustomPathResolver, pathMappingServiceConfiguration);

        // when
        service.updatePageWithCustomName(node, DEFAULT_TEST_PATH, PAGE_TYPE, WORKSPACE);

        //then
        Node nodeAfter = mockSession.getNode(DEFAULT_TEST_PATH);
        Node customNameForLang = nodeAfter.getNode("customNameForLang");
        String customLinkEn = customNameForLang.getNode("en").getProperty("customName").getString();
        assertEquals(1, NodeUtil.getDirectNodes(customNameForLang).size());
        assertEquals("pathEn", customLinkEn);
    }

    @Test
    public void updatePageWithCustomName_shouldNotUpdatePageWhenNotBobcat() throws RepositoryException, RegionNotFoundException {
        // given
        Node node = mockSession.getNode(NON_BOBCAT_TEST_PATH);

        // when
        defaultDxpCustomPathMappingService().updatePageWithCustomName(node, NON_BOBCAT_TEST_PATH, PAGE_TYPE, WORKSPACE);

        //then
        Node nodeAfter = mockSession.getNode(NON_BOBCAT_TEST_PATH);
        assertFalse(nodeAfter.hasNode("customNameForLang"));
    }

    public void updatePageWithCustomName_shouldNotUpdatePageWhenNotPageType() throws RepositoryException, RegionNotFoundException {
        // given
        Node node = mockSession.getNode(DEFAULT_TEST_PATH);

        // when
        defaultDxpCustomPathMappingService().updatePageWithCustomName(node, DEFAULT_TEST_PATH, EMPTY, WORKSPACE);

        //then
        Node nodeAfter = mockSession.getNode(DEFAULT_TEST_PATH);
        assertFalse(nodeAfter.hasNode("customNameForLang"));
    }

    public void updatePageWithCustomName_shouldNotUpdatePageWhenNotWebsiteWorkspace() throws RepositoryException, RegionNotFoundException {
        // given
        Node node = mockSession.getNode(DEFAULT_TEST_PATH);

        // when
        defaultDxpCustomPathMappingService().updatePageWithCustomName(node, DEFAULT_TEST_PATH, PAGE_TYPE, EMPTY);

        //then
        Node nodeAfter = mockSession.getNode(DEFAULT_TEST_PATH);
        assertFalse(nodeAfter.hasNode("customNameForLang"));
    }

    private DxpCustomPathMappingService defaultDxpCustomPathMappingService() throws RepositoryException, RegionNotFoundException {
        doReturn(Arrays.asList("bobcat")).when(pathMappingServiceConfiguration).getMappableSites();
        RegionsI18nContentSupport i18nContentSupport = new RegionsI18nContentSupport();
        LocaleDefinition localeEn = new LocaleDefinition();
        localeEn.setLanguage("en");
        localeEn.setEnabled(true);
        LocaleDefinition localeFr = new LocaleDefinition();
        localeFr.setLanguage("fr");
        localeFr.setEnabled(true);
        i18nContentSupport.setLocales(Arrays.asList(localeFr, localeEn));
        Region region = new Region();
        region.setI18n(i18nContentSupport);
        region.setName("eu");
        doReturn(region).when(regionsService).getRegionFromPath(DEFAULT_TEST_PATH);
        return new DxpCustomPathMappingService(regionsService, dxpCustomPathResolver, pathMappingServiceConfiguration);
    }

}
