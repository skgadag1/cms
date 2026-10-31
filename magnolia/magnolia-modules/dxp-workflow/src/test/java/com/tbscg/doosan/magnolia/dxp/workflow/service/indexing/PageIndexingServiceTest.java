package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.framework.message.MessagesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PAYLOAD_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_SORT_KEY_NAME;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageIndexingServiceTest extends BaseIndexingServiceTest {

    private static final String TABLE_NAME = "sample-dynamoDb-table";
    private static final String PAGE_UUID = "page-uuid";
    private static final String CHILD_UUID = "child-uuid";
    private static final String PUBLISH_TIME = "2026-04-27T12:00:00Z";

    @Mock
    private IndexingServiceConfiguration indexingServiceConfig;

    @Mock
    private DynamoDBService dynamoDBService;

    @Mock
    private RegionsService regionsService;

    @Mock
    private ProductIndexingService productIndexingService;

    @Mock
    private AttachmentIndexingService attachmentIndexingService;

    @Mock
    private MessagesManager messagesManager;

    @Captor
    private ArgumentCaptor<List<DynamoDbItem>> itemsCaptor;

    @Captor
    private ArgumentCaptor<List<PrimaryKey>> primaryKeysCaptor;

    private PageIndexingService pageIndexingService;

    @Before
    public void setUp() throws RepositoryException {
        when(indexingServiceConfig.getIndexerTableName()).thenReturn(TABLE_NAME);
        when(indexingServiceConfig.getIndexableSites()).thenReturn(Collections.singletonList("site"));
        when(regionsService.getRegionsForSite("site")).thenReturn(Collections.singletonList("na"));
        when(regionsService.getLanguagesForSiteAndRegion("site", "na")).thenReturn(Collections.singletonList("en"));
        when(dynamoDBService.queryItems(eq(TABLE_NAME), any(QueryRequest.class))).thenReturn(Collections.emptyList());
        mockWebsiteContext();

        pageIndexingService = spy(new PageIndexingService(
                indexingServiceConfig,
                regionsService,
                dynamoDBService,
                productIndexingService,
                attachmentIndexingService,
                messagesManager));
        doReturn("/na/en/friendly").when(pageIndexingService).getFriendlyUrlForLang(any(PageNode.class), eq("en"));
    }

    @Test
    public void shouldBatchWriteSinglePageAndUpdateAssociations() {
        assertTrue(pageIndexingService.index(INDEX_ACTION, "/site/na/index1", PAGE_UUID, PUBLISH_TIME, false));

        verify(dynamoDBService).batchWriteItem(eq(TABLE_NAME), itemsCaptor.capture());
        verify(productIndexingService).addPageAssociation(any(), eq(false));
        verify(attachmentIndexingService).addPageAssociation(any(), eq(false));

        final List<DynamoDbItem> writtenItems = itemsCaptor.getValue();
        assertEquals(1, writtenItems.size());
        assertEquals("/na/en/index1", writtenItems.get(0).getString(TABLE_SORT_KEY_NAME));
        assertEquals("/na/en/friendly", writtenItems.get(0).getMap(TABLE_PAYLOAD_KEY_NAME).get("path"));
    }

    @Test
    public void shouldBatchWriteRecursivePageWorkset() {
        assertTrue(pageIndexingService.index(INDEX_ACTION, "/site/na/index1", PAGE_UUID, PUBLISH_TIME, true));

        verify(dynamoDBService).batchWriteItem(eq(TABLE_NAME), itemsCaptor.capture());

        final List<String> indexedPaths = itemsCaptor.getValue().stream()
                .map(item -> item.getString(TABLE_SORT_KEY_NAME))
                .sorted()
                .toList();
        assertEquals(List.of("/na/en/index1", "/na/en/index1/child"), indexedPaths);
    }

    @Test
    public void shouldBatchDeleteRecursivePageWorksetAndUpdateAssociations() {
        assertTrue(pageIndexingService.index(DEINDEX_ACTION, "/site/na/index1", PAGE_UUID, PUBLISH_TIME, true));

        verify(dynamoDBService).batchDeleteItem(eq(TABLE_NAME), primaryKeysCaptor.capture());
        verify(productIndexingService, times(2)).addPageAssociation(any(), eq(true));
        verify(attachmentIndexingService, times(2)).addPageAssociation(any(), eq(true));

        assertEquals(2, primaryKeysCaptor.getValue().size());
    }

    @Test
    public void shouldSkipPageWhenRegionIsNotConfigured() {
        assertTrue(pageIndexingService.index(INDEX_ACTION, "/site/eu/index1", PAGE_UUID, PUBLISH_TIME, false));

        verify(dynamoDBService, never()).batchWriteItem(eq(TABLE_NAME), any());
        verify(dynamoDBService, never()).batchDeleteItem(eq(TABLE_NAME), any());
        verify(productIndexingService, never()).addPageAssociation(any(), any());
        verify(attachmentIndexingService, never()).addPageAssociation(any(), any());
    }

    @Test(expected = IndexingException.class)
    public void shouldThrowWhenPagePathIsBlank() {
        pageIndexingService.index(INDEX_ACTION, EMPTY, PAGE_UUID, PUBLISH_TIME, false);
    }

    @Test(expected = IndexingException.class)
    public void shouldThrowWhenPageUuidIsBlank() {
        pageIndexingService.index(INDEX_ACTION, "/site/na/index1", EMPTY, PUBLISH_TIME, false);
    }

    @Test(expected = IndexingException.class)
    public void shouldThrowWhenRegionsCannotBeLoaded() throws RepositoryException {
        when(regionsService.getRegionsForSite("site")).thenThrow(new RepositoryException("Path not found"));

        pageIndexingService.index(INDEX_ACTION, "/site/na/index1", PAGE_UUID, PUBLISH_TIME, false);
    }

    private void mockWebsiteContext() throws RepositoryException {
        final MockContext context = new MockContext();
        final MockSession pageSession = new MockSession(WorkflowConstant.WEBSITE_WORKSPACE);
        final MockNode root = (MockNode) pageSession.getRootNode();

        final MockNode site = new MockNode("site", NodeTypes.Page.NAME);
        final MockNode region = new MockNode("na", NodeTypes.Page.NAME);
        final MockNode page = createPage("index1", PAGE_UUID);
        page.addNode(createPage("child", CHILD_UUID));
        region.addNode(page);
        site.addNode(region);
        root.addNode(site);

        context.addSession(WorkflowConstant.WEBSITE_WORKSPACE, pageSession);
        MgnlContext.setInstance(context);
    }

    private MockNode createPage(final String name, final String uuid) throws RepositoryException {
        final MockNode page = new MockNode(name, NodeTypes.Page.NAME);
        page.setIdentifier(uuid);
        page.setProperty(NodeTypes.LastModified.LAST_MODIFIED, Calendar.getInstance());
        return page;
    }
}
