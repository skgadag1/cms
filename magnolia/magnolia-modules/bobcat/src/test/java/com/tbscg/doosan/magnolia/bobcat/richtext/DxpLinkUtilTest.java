package com.tbscg.doosan.magnolia.bobcat.richtext;

import com.tbscg.doosan.magnolia.bobcat.BobcatModuleConfiguration;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.link.Link;
import info.magnolia.link.LinkTransformer;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DxpLinkUtilTest {

    private static final String BOBCAT_PATH = "/bobcat/na/equipment/loaders/compact-track-skid-steer-loaders";
    private static final String CUSTOM_PATH = "/na/en/equipment/loaders/compact-track-skid-steer-loaders";

    private final BobcatModuleConfiguration moduleConfiguration = mock(BobcatModuleConfiguration.class);
    private final ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
    private final LinkTransformer transformer = mock(LinkTransformer.class);

    @Before
    public void setUp() throws RepositoryException {
        MockSession session = new MockSession("website");
        MockNode root = (MockNode) session.getRootNode();
        MockNode bobcat = new MockNode("bobcat", "mgnl:page");
        MockNode na = new MockNode("na", "mgnl:page");
        MockNode equipment = new MockNode("equipment", "mgnl:page");
        MockNode loaders = new MockNode("loaders", "mgnl:page");
        MockNode target = new MockNode("compact-track-skid-steer-loaders", "mgnl:page");
        MockNode customNameForLang = new MockNode("customNameForLang", "mgnl:contentNode");
        MockNode customNameForEn = new MockNode("en", "mgnl:contentNode");
        customNameForEn.setProperty("customLink", CUSTOM_PATH);
        customNameForLang.addNode(customNameForEn);
        target.addNode(customNameForLang);
        loaders.addNode(target);
        equipment.addNode(loaders);
        na.addNode(equipment);
        bobcat.addNode(na);
        root.addNode(bobcat);

        MockContext context = new MockContext();
        context.addSession("website", session);
        MgnlContext.setInstance(context);
        when(moduleConfiguration.isCustomPathResolutionEnabled()).thenReturn(true);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void publicContext_resolvesBobcatLinkToCustomPath() throws Exception {
        when(serverConfiguration.isAdmin()).thenReturn(false);

        String result = linkUtil().convertCustomLinksFromUUIDPattern(link("website", BOBCAT_PATH), transformer, "en");

        assertEquals(CUSTOM_PATH, result);
        verify(transformer, never()).transform(any(Link.class));
    }

    @Test
    public void authorContext_usesLinkTransformerOutputForBobcatLink() throws Exception {
        when(serverConfiguration.isAdmin()).thenReturn(true);
        when(transformer.transform(any(Link.class))).thenReturn("/bobcat_editor/bobcat/na/equipment/loaders/compact-track-skid-steer-loaders");

        String result = linkUtil().convertCustomLinksFromUUIDPattern(link("website", BOBCAT_PATH), transformer, "en");

        assertEquals("/bobcat_editor/bobcat/na/equipment/loaders/compact-track-skid-steer-loaders", result);
        verify(transformer).transform(any(Link.class));
    }

    @Test
    public void nonBobcatLink_continuesToUseLinkTransformer() throws Exception {
        when(serverConfiguration.isAdmin()).thenReturn(false);
        when(transformer.transform(any(Link.class))).thenReturn("/other-site/page");

        String result = linkUtil().convertCustomLinksFromUUIDPattern(link("website", "/other-site/page"), transformer, "en");

        assertEquals("/other-site/page", result);
        verify(transformer).transform(any(Link.class));
    }

    private DxpLinkUtil linkUtil() {
        return new DxpLinkUtil(moduleConfiguration, serverConfiguration);
    }

    private String link(String workspace, String path) {
        return "${link:{uuid:{12345678-1234-1234-1234-123456789012},repository:{" + workspace + "},path:{" + path + "}}}";
    }
}
