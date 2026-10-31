package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
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
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_LANG_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PARTITION_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PAYLOAD_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_SORT_KEY_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageIndexingServiceBatchingTest extends BaseIndexingServiceTest {

    private static final String SAMPLE_UUID = "page-uuid-1";
    private static final String TABLE_NAME = "sample-dynamoDb-table";

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
    public void shouldBatchWriteAndRenameDeleteWhenIndexingPage() {
        DynamoDbItem existingItem = new DynamoDbItem()
                .withPrimaryKey(new PrimaryKey(TABLE_PARTITION_KEY_NAME, "site#page#" + SAMPLE_UUID, TABLE_SORT_KEY_NAME, "/na/en/old-path"))
                .withString(TABLE_LANG_KEY_NAME, "en");
        when(dynamoDBService.queryItems(eq(TABLE_NAME), any(QueryRequest.class))).thenReturn(Collections.singletonList(existingItem));

        assertTrue(pageIndexingService.index(INDEX_ACTION, "/site/na/index1", SAMPLE_UUID, Instant.now().toString(), false));

        verify(dynamoDBService).batchWriteDeleteItems(eq(TABLE_NAME), itemsCaptor.capture(), primaryKeysCaptor.capture());
        verify(dynamoDBService, never()).putItem(any(String.class), any(DynamoDbItem.class));
        verify(dynamoDBService, never()).deleteItem(any(String.class), any(PrimaryKey.class));
        verify(productIndexingService).addPageAssociation(any(), eq(false));
        verify(attachmentIndexingService).addPageAssociation(any(), eq(false));

        List<DynamoDbItem> writtenItems = itemsCaptor.getValue();
        List<PrimaryKey> deletedKeys = primaryKeysCaptor.getValue();
        assertEquals(1, writtenItems.size());
        assertEquals(1, deletedKeys.size());
        assertEquals("/na/en/index1", writtenItems.get(0).getString(TABLE_SORT_KEY_NAME));
        assertEquals("/na/en/friendly", writtenItems.get(0).getMap(TABLE_PAYLOAD_KEY_NAME).get("path"));
        assertEquals("/na/en/old-path", deletedKeys.get(0).getValueMap().get(TABLE_SORT_KEY_NAME).s());
    }

    @Test
    public void shouldBatchDeleteWhenDeindexingPage() {
        assertTrue(pageIndexingService.index(DEINDEX_ACTION, "/site/na/index1", SAMPLE_UUID, Instant.now().toString(), false));

        verify(dynamoDBService).batchDeleteItem(eq(TABLE_NAME), primaryKeysCaptor.capture());
        verify(dynamoDBService, never()).deleteItem(any(String.class), any(PrimaryKey.class));
        verify(productIndexingService).addPageAssociation(any(), eq(true));
        verify(attachmentIndexingService).addPageAssociation(any(), eq(true));

        List<PrimaryKey> deletedKeys = primaryKeysCaptor.getValue();
        assertEquals(1, deletedKeys.size());
        assertEquals("/na/en/index1", deletedKeys.get(0).getValueMap().get(TABLE_SORT_KEY_NAME).s());
    }

    private void mockWebsiteContext() throws RepositoryException {
        final MockContext context = new MockContext();
        final MockSession pageSession = new MockSession(WorkflowConstant.WEBSITE_WORKSPACE);
        final MockNode root = (MockNode) pageSession.getRootNode();

        MockNode site = new MockNode("site", NodeTypes.Page.NAME);
        MockNode region = new MockNode("na", NodeTypes.Page.NAME);
        MockNode page = new MockNode("index1", "dxp:website");
        Calendar lastModified = Calendar.getInstance();
        page.setProperty("mgnl:lastModified", lastModified);

        region.addNode(page);
        site.addNode(region);
        root.addNode(site);

        context.addSession(WorkflowConstant.WEBSITE_WORKSPACE, pageSession);
        MgnlContext.setInstance(context);
    }

}
