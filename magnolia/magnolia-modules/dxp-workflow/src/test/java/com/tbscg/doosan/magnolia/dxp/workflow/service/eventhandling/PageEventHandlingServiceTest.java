package com.tbscg.doosan.magnolia.dxp.workflow.service.eventhandling;

import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.core.action.ui.DxpActionPublishedEvent;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageEventHandlingServiceTest {

    @Mock
    private IndexingServiceConfiguration indexingServiceConfig;

    @Mock
    private RegionsService regionsService;

    @Mock
    private DynamoDBService dynamoDBService;

    @Mock
    private EventBus eventBus;

    @Captor
    private ArgumentCaptor<DxpActionPublishedEvent> eventCaptor;

    private PageEventHandlingService pageEventHandlingService;

    @Before
    public void setUp() throws RepositoryException {
        Components.setComponentProvider(new MockComponentProvider());
        when(indexingServiceConfig.getIndexableSites()).thenReturn(List.of("site", WorkflowConstant.DEALERNET_SITE));
        mockWebsiteContext();
        pageEventHandlingService = new PageEventHandlingService(indexingServiceConfig, regionsService, dynamoDBService, eventBus);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    public void shouldPublishPageEventWithoutResolvingRegionLanguages() {
        assertTrue(pageEventHandlingService.handleEvent(
                ActionType.PUBLISH,
                "/site/na/index1",
                "page-uuid-1",
                false,
                123L,
                WorkflowConstant.WEBSITE_WORKSPACE));

        verify(eventBus).fireEvent(eventCaptor.capture());
        verifyNoInteractions(regionsService);
        assertEquals(2, eventCaptor.getValue().getCustomLinks().size());
        assertEquals("/na/fr/index-custom", eventCaptor.getValue().getCustomLinks().get(0));
        assertEquals("/na/en/index-custom", eventCaptor.getValue().getCustomLinks().get(1));
    }

    @Test
    public void shouldPublishPageEventWithSiteFromPagePath() {
        assertTrue(pageEventHandlingService.handleEvent(
                ActionType.PUBLISH,
                "/dealernet/na/index1",
                "page-uuid-1",
                false,
                123L,
                WorkflowConstant.WEBSITE_WORKSPACE));

        verify(eventBus).fireEvent(eventCaptor.capture());
        assertEquals(WorkflowConstant.DEALERNET_SITE, eventCaptor.getValue().getSite());
    }

    private void mockWebsiteContext() throws RepositoryException {
        final MockContext context = new MockContext();
        final MockSession session = new MockSession(WorkflowConstant.WEBSITE_WORKSPACE);
        final MockNode root = (MockNode) session.getRootNode();

        final MockNode site = new MockNode("site", NodeTypes.Page.NAME);
        final MockNode region = new MockNode("na", NodeTypes.Page.NAME);
        final MockNode page = new MockNode("index1", NodeTypes.Page.NAME);

        addCustomLink(page, "fr", "/na/fr/index-custom");
        addCustomLink(page, "en", "/na/en/index-custom");

        region.addNode(page);
        site.addNode(region);
        root.addNode(site);
        root.addNode(createSite());

        context.addSession(WorkflowConstant.WEBSITE_WORKSPACE, session);
        MgnlContext.setInstance(context);
    }

    private MockNode createSite() {
        final MockNode site = new MockNode(WorkflowConstant.DEALERNET_SITE, NodeTypes.Page.NAME);
        final MockNode region = new MockNode("na", NodeTypes.Page.NAME);
        final MockNode page = new MockNode("index1", NodeTypes.Page.NAME);

        region.addNode(page);
        site.addNode(region);

        return site;
    }

    @SuppressWarnings("RedundantThrows")
    private void addCustomLink(final MockNode page, final String language, final String customLink) throws RepositoryException {
        MockNode customNamesForLang = page.hasNode(WorkflowConstant.CUSTOM_NAME_FOR_LANG)
                ? (MockNode) page.getNode(WorkflowConstant.CUSTOM_NAME_FOR_LANG)
                : new MockNode(WorkflowConstant.CUSTOM_NAME_FOR_LANG, "mgnl:contentNode");
        if (!page.hasNode(WorkflowConstant.CUSTOM_NAME_FOR_LANG)) {
            page.addNode(customNamesForLang);
        }
        MockNode languageNode = new MockNode(language, "mgnl:contentNode");
        languageNode.setProperty(WorkflowConstant.CUSTOM_LINK_PROPERTY, customLink);
        customNamesForLang.addNode(languageNode);
    }
}
