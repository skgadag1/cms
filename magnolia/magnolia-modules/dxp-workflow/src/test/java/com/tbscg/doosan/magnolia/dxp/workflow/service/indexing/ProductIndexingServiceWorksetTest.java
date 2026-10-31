package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationRequestContext;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationScopes;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowExecutionSettings;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.apache.jackrabbit.JcrConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductIndexingServiceWorksetTest extends BaseIndexingServiceTest {

    private static final String TABLE_NAME = "sample-dynamoDb-table";

    @Mock
    private IndexingServiceConfiguration indexingConfigMock;

    @Mock
    private DynamoDBService dynamoDBServiceMock;

    @Mock
    private RegionsService regionsService;

    @Mock
    private AttachmentIndexingService attachmentIndexingService;

    @Captor
    private ArgumentCaptor<List<DynamoDbItem>> itemsCaptor;

    private ProductIndexingService indexingService;

    @Before
    public void setUp() throws RepositoryException {
        when(indexingConfigMock.getIndexerTableName()).thenReturn(TABLE_NAME);
        mockContext();
        indexingService = new ProductIndexingService(indexingConfigMock, regionsService, dynamoDBServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldIndexActivatedChildProductsForNonRecursiveCategoryPublish() {
        final String path = "/bobcat/na/test-category";

        try (PublicationRequestContext ignored = PublicationRequestContext.open(ProductCatalogConstants.PRODUCTS_WORKSPACE, path, false)) {
            assertTrue(indexingService.index(INDEX_ACTION, path, Instant.now().toString(), false));
        }

        verify(dynamoDBServiceMock, times(1)).batchWriteItem(anyString(), itemsCaptor.capture());
        final List<String> indexedPaths = itemsCaptor.getValue().stream()
                .map(item -> item.getMap("payload").get("path"))
                .map(String::valueOf)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(Stream.of(
                "/bobcat/na/test-category/product-direct",
                "/bobcat/na/test-category/test-subcategory/product-child",
                "/bobcat/na/test-category/test-subcategory/product-historical"
        ).sorted().collect(Collectors.toList()), indexedPaths);
    }

    @Test
    public void shouldApplyLifecycleFilteringToChildProductsDerivedFromWorkset() {
        final String path = "/bobcat/na/test-category/test-subcategory";

        try (PublicationRequestContext ignored = PublicationRequestContext.open(
                ProductCatalogConstants.PRODUCTS_WORKSPACE,
                path,
                false,
                PublicationScopes.forProductLifecycle(ProductCatalogConstants.Lifecycle.CURRENT))) {
            assertTrue(indexingService.index(INDEX_ACTION, path, Instant.now().toString(), false));
        }

        verify(dynamoDBServiceMock, times(1)).batchWriteItem(anyString(), itemsCaptor.capture());
        final List<String> indexedPaths = itemsCaptor.getValue().stream()
                .map(item -> item.getMap("payload").get("path"))
                .map(String::valueOf)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("/bobcat/na/test-category/test-subcategory/product-child"), indexedPaths);
    }

    @Test
    public void shouldChunkRelatedAttachmentQueriesForLargeProductSets() {
        final int batchSize = WorkflowExecutionSettings.RELATED_ATTACHMENT_QUERY_BATCH_SIZE;
        final List<String> productUuids = IntStream.rangeClosed(1, batchSize + 1)
                .mapToObj(index -> "uuid-" + index)
                .toList();

        final List<String> queries = indexingService.buildRelatedAttachmentQueries(productUuids);

        assertEquals(2, queries.size());
        assertTrue(queries.get(0).contains("uuid-" + batchSize));
        assertFalse(queries.get(0).contains("uuid-" + (batchSize + 1)));
        assertTrue(queries.get(1).contains("uuid-" + (batchSize + 1)));
    }

    private void mockContext() throws RepositoryException {
        final MockContext context = new MockContext();
        context.addSession(ProductCatalogConstants.PRODUCTS_WORKSPACE, mockProductSession());
        context.addSession(ProductCatalogConstants.ATTACHMENTS_WORKSPACE, new MockSession(ProductCatalogConstants.ATTACHMENTS_WORKSPACE));
        MgnlContext.setInstance(context);
    }

    private MockSession mockProductSession() throws RepositoryException {
        final MockSession mockSession = new MockSession(ProductCatalogConstants.PRODUCTS_WORKSPACE);
        final MockNode root = (MockNode) mockSession.getRootNode();

        final MockNode site = new MockNode("bobcat", "dxp:region");
        final MockNode region = new MockNode("na", "dxp:region");
        final MockNode category = new MockNode("test-category", ProductCatalogConstants.NodeTypes.Category.NAME);
        final MockNode subCategory = new MockNode("test-subcategory", ProductCatalogConstants.NodeTypes.SubCategory.NAME);

        category.addNode(createProductNode("product-direct", "uuid-direct", true, ProductCatalogConstants.Lifecycle.CURRENT));
        category.addNode(createProductNode("product-inactive", "uuid-inactive", false, ProductCatalogConstants.Lifecycle.CURRENT));
        subCategory.addNode(createProductNode("product-child", "uuid-child", true, ProductCatalogConstants.Lifecycle.CURRENT));
        subCategory.addNode(createProductNode("product-historical", "uuid-historical", true, ProductCatalogConstants.Lifecycle.HISTORICAL));
        category.addNode(subCategory);
        region.addNode(category);
        site.addNode(region);
        root.addNode(site);

        return mockSession;
    }

    private MockNode createProductNode(final String name,
                                       final String uuid,
                                       final boolean activated,
                                       final String lifecycle) throws RepositoryException {
        final MockNode productNode = new MockNode(name, ProductCatalogConstants.NodeTypes.Product.NAME);
        productNode.setProperty(JcrConstants.JCR_UUID, uuid);
        productNode.setProperty(NodeTypes.LastModified.NAME, Calendar.getInstance());
        productNode.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, activated);
        productNode.setProperty(ProductCatalogConstants.Properties.LIFECYCLE, lifecycle);
        return productNode;
    }
}
