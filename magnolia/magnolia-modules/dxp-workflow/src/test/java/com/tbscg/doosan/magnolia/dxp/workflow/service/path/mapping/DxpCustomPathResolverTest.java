package com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PathMapping;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PathMappingEntry;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.CustomPathMappingException;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

public class DxpCustomPathResolverTest {

    private static final String LANG1 = "en";
    private static final String LANG2 = "fr";
    private static final String domain = "bobcat";
    private static final String TEST_PATH = "/bobcat/eu/page";
    private static final String TEST_PATH1 = "/bobcat/eu/page1";
    private static final String TEST_PATH2 = "/bobcat/na/page2";
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
        ctx.addSession(WorkflowConstant.WEBSITE_WORKSPACE, mockSession);
        MgnlContext.setInstance(ctx);
    }

    private MockSession mockPageSession() throws RepositoryException {
        final MockSession mockSession = new MockSession("website");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode bobcat = new MockNode("bobcat", NodeTypes.Page.NAME);
        MockNode eu = new MockNode("eu", NodeTypes.Page.NAME);
        MockNode na = new MockNode("na", NodeTypes.Page.NAME);
        MockNode page = new MockNode("page", NodeTypes.Page.NAME);
        MockNode page1 = new MockNode("page1", NodeTypes.Page.NAME);
        MockNode page2 = new MockNode("page2", NodeTypes.Page.NAME);
        MockNode pageA = new MockNode("pageA", NodeTypes.Page.NAME);
        MockNode pageAA = new MockNode("pageAA", NodeTypes.Page.NAME);
        MockNode pageAB = new MockNode("pageAB", NodeTypes.Page.NAME);

        MockNode customNameForLang = generateCustomNameForLangNodeEu("acen", "acfr");
        page.addNode(customNameForLang);
        page.setIdentifier("1");
        page.setProperty("removeFromUrl", false);
        page.setProperty("completelyHidden", true);

        MockNode customNameForLangA = generateCustomNameForLangNodeEu("acAen", "acAfr");
        pageA.addNode(customNameForLangA);
        pageA.setIdentifier("1a");
        pageA.setProperty("removeFromUrl", false);

        MockNode customNameForLangAA = generateCustomNameForLangNodeEu("acAAen", "acAAfr");
        pageAA.addNode(customNameForLangAA);
        pageAA.setIdentifier("1aa");
        pageAA.setProperty("removeFromUrl", false);

        MockNode customNameForLangAB = generateCustomNameForLangNodeEu("acABen", "acABfr");
        pageAB.addNode(customNameForLangAB);
        pageAB.setIdentifier("1ab");
        pageAB.setProperty("removeFromUrl", false);

        MockNode customNameForLang1 = generateCustomNameForLangNodeEu("eu", "eu");
        eu.addNode(customNameForLang1);
        eu.setProperty("removeFromUrl", false);

        MockNode customNameForLang2 = new MockNode("customNameForLang", "mgnl:contentNode");
        MockNode customNameForEn2 = new MockNode("en", "mgnl:contentNode");
        customNameForEn2.setProperty("customLink", "/na/en");
        customNameForLang2.setProperty("region", "na");
        customNameForLang2.addNode(customNameForEn2);
        na.addNode(customNameForLang2);

        eu.addNode(page);
        eu.addNode(page1);
        page.addNode(pageA);
        pageA.addNode(pageAA);
        pageA.addNode(pageAB);
        na.addNode(page2);
        bobcat.addNode(eu);
        bobcat.addNode(na);
        parent.addNode(bobcat);
        return mockSession;
    }

    private MockNode generateCustomNameForLangNodeEu(String en, String fr) throws RepositoryException {
        MockNode customNameForLang = new MockNode("customNameForLang", "mgnl:contentNode");
        MockNode customNameForEn = new MockNode("en", "mgnl:contentNode");
        MockNode customNameForFr = new MockNode("fr", "mgnl:contentNode");
        customNameForEn.setProperty("customName", en);
        customNameForFr.setProperty("customName", fr);
        customNameForLang.setProperty("region", "eu");
        customNameForLang.addNode(customNameForEn);
        customNameForLang.addNode(customNameForFr);
        return customNameForLang;
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    public void resolveCustomPaths_shouldThrowOnNullInput() {
        assertThrows(CustomPathMappingException.class, () -> defaultDxpCustomPathResolver().resolveCustomPaths(null));
    }

    @Test
    public void resolveCustomPaths_shouldThrowWhenNoCustomNameForLang() {
        assertThrows(CustomPathMappingException.class, () -> defaultDxpCustomPathResolver().resolveCustomPaths(null));
    }

    @Test
    public void resolveCustomPaths_shouldThrowWhenNoCustomNameForLangInParent() throws RepositoryException {
        //given
        final Node node = mockSession.getNode(TEST_PATH2);

        assertThrows(CustomPathMappingException.class, () -> defaultDxpCustomPathResolver().resolveCustomPaths(node));
    }

    @Test
    public void resolveCustomPaths_shouldResolveDomainNameFromRootPPC() throws RepositoryException {
        //given
        final Node node = mockSession.getNode(TEST_PATH);

        // when
        final PathMapping pathMapping = defaultDxpCustomPathResolver().resolveCustomPaths(node);

        // then
        assertThat(pathMapping, notNullValue());
        assertThat(pathMapping.getDomain(), equalTo(domain));
    }


    @Test
    public void resolveCustomPaths_shouldHandle_singlePathWith2Mappings() throws RepositoryException {
        // given
        final Node node = mockSession.getNode(TEST_PATH);

        // when
        final PathMapping pathMapping = defaultDxpCustomPathResolver().resolveCustomPaths(node);

        // then
        assertThat(pathMapping, notNullValue());
        {
            final Map<String, PathMappingEntry> lang2MappingMap = pathMapping.getLang2MappingMap();
            assertThat(lang2MappingMap, aMapWithSize(2));
            {
                final PathMappingEntry mappingEntry = lang2MappingMap.get(LANG1);
                assertThat(mappingEntry.getUuid(), equalTo("1"));
                assertThat(mappingEntry.getJcrPath(), equalTo("/eu/page"));
                assertThat(mappingEntry.getCustomPath(), equalTo("/eu/acen"));
            }
            {
                final PathMappingEntry mappingEntry = lang2MappingMap.get(LANG2);
                assertThat(mappingEntry.getUuid(), equalTo("1"));
                assertThat(mappingEntry.getJcrPath(), equalTo("/eu/page"));
                assertThat(mappingEntry.getCustomPath(), equalTo("/eu/acfr"));
            }
        }
    }


    @Test
    public void resolveCustomPaths_shouldHandle_currentNodeRemovedFromURL() throws RepositoryException {
        // given
        final Node node = mockSession.getNode(TEST_PATH);
        node.setProperty("removeFromUrl", true);

        // when
        final PathMapping pathMapping = defaultDxpCustomPathResolver().resolveCustomPaths(node);

        // then
        assertThat(pathMapping, notNullValue());

        final PathMappingEntry rootEntry = pathMapping.getLang2MappingMap().get(LANG1);
        assertThat(rootEntry.getUuid(), equalTo("1"));
        assertThat(rootEntry.getCustomPath(), equalTo("/eu/acen"));
    }

    @Test
    public void resolveCustomPaths_shouldHandle_pathWithCompletelyHiddenNode() throws RepositoryException {
        // given
        final Node node = mockSession.getNode(TEST_PATH);
        node.getNode("pageA").setProperty("completelyHidden", true);

        // when
        final PathMapping pathMapping = defaultDxpCustomPathResolver().resolveCustomPaths(node);

        // then
        assertThat(pathMapping, notNullValue());

        final PathMappingEntry rootEntry = pathMapping.getLang2MappingMap().get(LANG1);
        assertThat(rootEntry.getUuid(), equalTo("1"));
    }

    private static DxpCustomPathResolver defaultDxpCustomPathResolver() {
        return new DxpCustomPathResolver();
    }

}
