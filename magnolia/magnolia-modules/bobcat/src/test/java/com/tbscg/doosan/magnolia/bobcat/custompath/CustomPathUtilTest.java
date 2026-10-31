package com.tbscg.doosan.magnolia.bobcat.custompath;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.junit.Assert.assertEquals;

public class CustomPathUtilTest {

    public static final String WEBSITE = "website";

    private final Session mockSession = mockPageSession();

    public CustomPathUtilTest() throws RepositoryException {
    }

    @Before
    public void setUp() throws RepositoryException {
        mockContext();
    }

    private void mockContext() {
        final MockContext ctx = new MockContext();
        ctx.addSession(WEBSITE, mockSession);
        MgnlContext.setInstance(ctx);
    }

    private MockSession mockPageSession() throws RepositoryException {
        final MockSession mockSession = new MockSession("website");
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode site = new MockNode("bobcat", "mgnl:page");
        MockNode region = new MockNode("eu", "mgnl:page");

        MockNode mockNode = new MockNode("page", "mgnl:page");
        MockNode customNameForLang = new MockNode("customNameForLang", "mgnl:contentNode");
        MockNode customNameForEn = new MockNode("en", "mgnl:contentNode");
        MockNode customNameForFr = new MockNode("fr", "mgnl:contentNode");
        customNameForEn.setProperty("customLink", "/eu/en/acen");
        customNameForFr.setProperty("customLink", "/eu/fr/acfr");
        customNameForLang.addNode(customNameForEn);
        customNameForLang.addNode(customNameForFr);

        mockNode.addNode(customNameForLang);
        region.addNode(mockNode);
        site.addNode(region);
        parent.addNode(site);
        return mockSession;
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void toCustomLink_shouldGetCustomLinkFromNode() throws RepositoryException {
        //given
        Node node = mockSession.getNode("/bobcat/eu/page");

        // when
        String linkEn = CustomPathUtil.toCustomLink(true, node, "en");
        String linkFr = CustomPathUtil.toCustomLink(true, node, "fr");

        // then
        assertEquals(linkEn, "/eu/en/acen");
        assertEquals(linkFr, "/eu/fr/acfr");
    }

    @Test
    public void toCustomLink_shouldGetCustomLinkFromPath() throws RepositoryException {
        //given
        Node node = mockSession.getNode("/bobcat/eu/page");

        // when
        String linkEn = CustomPathUtil.toCustomLink(false, node, "en");
        String linkFr = CustomPathUtil.toCustomLink(false, node, "fr");

        // then
        assertEquals(linkEn, "/eu/en/page");
        assertEquals(linkFr, "/eu/fr/page");
    }

    @Test
    public void toCustomLink_shouldReturnDefaultResolvedPathWhenNoLang() throws RepositoryException {
        //given
        Node node = mockSession.getNode("/bobcat/eu/page");

        // when
        String linkDe = CustomPathUtil.toCustomLink(true, node, "de");

        // then
        assertEquals(linkDe, "/eu/de/page");
    }

    @Test
    public void splitToRootPathAndNodeName_shouldSplit() {
        //given
        String path = "/bobcat/eu/page";

        // when
        RootPathAndNodeName rootPathAndNodeName = CustomPathUtil.splitToRootPathAndNodeName(path);

        // then
        assertEquals(rootPathAndNodeName.getNodeName(), "page");
        assertEquals(rootPathAndNodeName.getRootPath(), "/bobcat/eu");
    }
}
