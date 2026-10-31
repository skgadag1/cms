package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductIndexingServiceTest extends BaseIndexingServiceTest {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Mock
    private IndexingServiceConfiguration indexingConfigMock;

    @Mock
    private DynamoDBService dynamoDBServiceMock;

    @Mock
    private RegionsService regionsService;

    @Mock
    private AttachmentIndexingService attachmentIndexingService;

    private ProductIndexingService indexingService;


    @Captor
    private ArgumentCaptor<List<DynamoDbItem>> itemsCaptor;

    @Captor
    private ArgumentCaptor<List<PrimaryKey>> primaryKeysCaptor;

    @Captor
    private ArgumentCaptor<UpdateItemRequest> updateItemCaptor;

    @Before
    public void setUp() throws RepositoryException {
        when(indexingConfigMock.getIndexerTableName()).thenReturn("sample-dynamoDb-table");
        mockContext();
        indexingService = new ProductIndexingService(indexingConfigMock, regionsService, dynamoDBServiceMock, attachmentIndexingService);
    }

    private void mockContext() throws RepositoryException {
        final MockContext ctx = new MockContext();
        final MockSession productSession = mockProductSession();
        final MockSession pageSession = mockPageSession();
        ctx.addSession(ProductCatalogConstants.PRODUCTS_WORKSPACE, productSession);
        ctx.addSession(WorkflowConstant.WEBSITE_WORKSPACE, pageSession);
        MgnlContext.setInstance(ctx);
    }

    private MockSession mockProductSession() {
        final MockSession mockSession = new MockSession(ProductCatalogConstants.PRODUCTS_WORKSPACE);
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode bobcat = new MockNode("bobcat", "dxp:region");
        MockNode region = new MockNode("na", "dxp:region");
        region.addNode((MockNode) generateNodes(10));
        region.addNode((MockNode) createCategoryWithProductsGroup());
        bobcat.addNode(region);
        parent.addNode(bobcat);
        return mockSession;
    }

    private MockSession mockPageSession() throws RepositoryException {
        final MockSession mockSession = new MockSession(WorkflowConstant.WEBSITE_WORKSPACE);
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode bobcat = new MockNode("bobcat", "dxp:region");
        MockNode region = new MockNode("na", "dxp:region");
        List<MockNode> page = generatePageNodesWithProduct();
        page.forEach(region::addNode);
        bobcat.addNode(region);
        parent.addNode(bobcat);
        return mockSession;
    }

    @Test
    public void shouldIndex_successfullyIndex() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/test-category", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(anyString(), itemsCaptor.capture());
        assertEquals(10, itemsCaptor.getValue().size());
        assertTrue(itemsCaptor.getValue().stream().anyMatch(item -> "na".equals(item.getString("region"))));
    }

    @Test
    public void shouldIndex_notExecuteBatchWriteItem() throws IndexingException {
        indexingService.index(INDEX_ACTION, "/bobcat/na-test", Instant.now().toString(), true);
        verify(dynamoDBServiceMock, times(0)).batchWriteItem(anyString(), anyList());
    }

    @Test
    public void shouldReindex_successfullyReindex() throws IndexingException {
        assertTrue(indexingService.index(DEINDEX_ACTION, "/bobcat/na/test-category", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteDeleteItems(anyString(), anyList(), primaryKeysCaptor.capture());
        assertEquals(10, primaryKeysCaptor.getValue().size());
    }

    @Test
    public void shouldIndex_successfullyIndexProductsGroup() throws IndexingException {
        String publishTime = Instant.now().toString();
        ObjectMapper objectMapper = new ObjectMapper();
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/test-group-category", publishTime, true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(anyString(), itemsCaptor.capture());
        assertEquals(2, itemsCaptor.getValue().size());
        DynamoDbItem item = itemsCaptor.getValue().get(0);
        assertEquals(publishTime, item.asMap().get("dateModified"));
        Object payload = item.asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified", "category", "subCategory")));
        assertEquals("/bobcat/na/test-group-category/test-subcategory/testGroup/product#1", payloadMap.get("path"));

        item = itemsCaptor.getValue().get(1);
        payload = item.asMap().get("payload");
        payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified", "category")));
        assertEquals("/bobcat/na/test-group-category/testGroup/product#2", payloadMap.get("path"));
    }

    @Test
    public void shouldAddPageAssociation_updateProductWithUrls() throws RepositoryException {
        // given
        when(regionsService.getLanguagesForSiteAndRegion(anyString(), anyString())).thenReturn(List.of("en"));
        indexingService = new ProductIndexingService(indexingConfigMock, regionsService, dynamoDBServiceMock, attachmentIndexingService);
        mockExistingIndexedProducts(indexedProductItem("1", "/bobcat/na/test-category/product#1"));
        Node page = mockPageSession().getNode("/bobcat/na/page1");

        // when
        indexingService.addPageAssociation(page, false);

        // then
        verify(dynamoDBServiceMock, times(1)).updateItem(anyString(), updateItemCaptor.capture());
        Map<String, AttributeValue> key = updateItemCaptor.getValue().key();
        validateUrls(Collections.singletonMap("en", "/na/en/page11"));
        assertEquals(1, updateItemCaptor.getValue().attributeUpdates().size());
        assertTrue(key.containsKey("pk"));
        assertEquals("bobcat#product#1", key.get("pk").s());
        assertTrue(key.containsKey("sk"));
        assertEquals("/bobcat/na/test-category/product#1", key.get("sk").s());
        assertTrue(page.hasProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL));
        assertEquals("/bobcat/na/test-category/product#1", page.getProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL).getString());
    }

    @Test
    public void shouldAddPageAssociation_updateProductsWhenPreviousTheSame() throws RepositoryException {
        // given
        when(regionsService.getLanguagesForSiteAndRegion(anyString(), anyString())).thenReturn(List.of("en"));
        mockExistingIndexedProducts(indexedProductItem("1", "/bobcat/na/test-category/product#1"));
        Node page = mockPageSession().getNode("/bobcat/na/page1");
        page.setProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL, "/bobcat/na/test-category/product#1");

        // when
        indexingService.addPageAssociation(page, false);

        // then
        verify(dynamoDBServiceMock, times(1)).updateItem(anyString(), updateItemCaptor.capture());
        validateUrls(Collections.singletonMap("en", "/na/en/page11"));
        assertTrue(page.hasProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL));
        assertEquals("/bobcat/na/test-category/product#1", page.getProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL).getString());
    }

    @Test
    public void shouldAddPageAssociation_updateTwoProductsWhenPreviousDifferent() throws RepositoryException {
        // given
        when(regionsService.getLanguagesForSiteAndRegion(anyString(), anyString())).thenReturn(List.of("en"));
        mockExistingIndexedProducts(
                indexedProductItem("5", "/bobcat/na/test-category/product#5"),
                indexedProductItem("1", "/bobcat/na/test-category/product#1"));
        Node page = mockPageSession().getNode("/bobcat/na/page1");
        page.setProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL, "/bobcat/na/test-category/product#5");

        // when
        indexingService.addPageAssociation(page, false);

        // then
        verify(dynamoDBServiceMock, times(2)).updateItem(anyString(), updateItemCaptor.capture());
        validateUrls(Collections.singletonMap("en", "/na/en/page11"));
        assertTrue(page.hasProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL));
        assertEquals("/bobcat/na/test-category/product#1", page.getProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL).getString());
    }

    @Test
    public void shouldAddPageAssociation_updateProductWithEmptyUrlsWhenPageUnpublished() throws RepositoryException {
        // given
        mockExistingIndexedProducts(indexedProductItem("2", "/bobcat/na/test-category/test-subcategory/product#2"));
        Node page = mockPageSession().getNode("/bobcat/na/page2");

        // when
        indexingService.addPageAssociation(page, false);

        // then
        verify(dynamoDBServiceMock, times(1)).updateItem(anyString(), updateItemCaptor.capture());
        validateUrls(Collections.emptyMap());
    }

    @Test
    public void shouldAddPageAssociation_notUpdateProductWhenIsUnpublished() throws RepositoryException {
        // given
        when(regionsService.getLanguagesForSiteAndRegion(anyString(), anyString())).thenReturn(List.of("en"));
        Node page = mockPageSession().getNode("/bobcat/na/page3");

        // when
        indexingService.addPageAssociation(page, false);

        // then
        verify(dynamoDBServiceMock, times(0)).updateItem(anyString(), updateItemCaptor.capture());
    }

    @Test
    public void shouldAddPageAssociation_notUpdateProductWhenError() throws RepositoryException {
        // given
        when(regionsService.getLanguagesForSiteAndRegion(anyString(), anyString())).thenThrow(new RepositoryException());
        Node page = mockPageSession().getNode("/bobcat/na/page1");
        page.setProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL, "/bobcat/na/test-category/product#5");

        // when
        indexingService.addPageAssociation(page, false);

        // then
        verify(dynamoDBServiceMock, times(0)).updateItem(anyString(), updateItemCaptor.capture());
        assertNotEquals("/bobcat/na/test-category/product#5", page.getProperty(ProductCatalogConstants.PageProperties.CURRENT_MODEL).getString());
    }

    private List<MockNode> generatePageNodesWithProduct() throws RepositoryException {
        MockNode page1 = new MockNode("page1", NodeTypes.Page.NAME);
        page1.setProperty(ProductCatalogConstants.PageProperties.PRODUCT, "1");
        page1.setProperty("mgnl:activationStatus", true);
        page1.setProperty("mgnl:template", "bobcat:pages/machine-details");
        page1.addNode(this.generateCustomNameForLang("/na/en/page11"));

        MockNode page2 = new MockNode("page2", NodeTypes.Page.NAME);
        page2.setProperty(ProductCatalogConstants.PageProperties.PRODUCT, "2");
        page2.setProperty("mgnl:activationStatus", false);
        page2.setProperty("mgnl:template", "bobcat:pages/machine-details");
        page2.addNode(this.generateCustomNameForLang("/na/en/page12"));


        MockNode page3 = new MockNode("page3", NodeTypes.Page.NAME);
        page3.setProperty(ProductCatalogConstants.PageProperties.PRODUCT, "3");
        page3.setProperty("mgnl:activationStatus", true);
        page3.setProperty("mgnl:template", "bobcat:pages/machine-details");
        page3.addNode(this.generateCustomNameForLang("/na/en/page13"));

        return Arrays.asList(page1, page2, page3);
    }

    private MockNode generateCustomNameForLang(String customLink) throws RepositoryException {
        MockNode customNameForLang = new MockNode("customNameForLang", "mgnl:contentNode");
        MockNode customNameForEn = new MockNode("en", "mgnl:contentNode");
        customNameForEn.setProperty("customLink", customLink);
        customNameForLang.addNode(customNameForEn);
        return customNameForLang;
    }

    private Node generateNodes(int nodeCount) {
        MockNode category = new MockNode("test-category", ProductCatalogConstants.NodeTypes.Category.NAME);
        MockNode subCategory = new MockNode("test-subcategory", ProductCatalogConstants.NodeTypes.SubCategory.NAME);
        IntStream.range(0, nodeCount)
                .forEach(i -> {
                    MockNode productNode = new MockNode("product#" + i, ProductCatalogConstants.NodeTypes.Product.NAME);
                    try {
            productNode.setIdentifier(String.valueOf(i));
                        productNode.setProperty("jcr:uuid", "test-product-uuid");
                        productNode.setProperty("mgnl:lastModified", Calendar.getInstance());
                        productNode.setProperty("mgnl:activationStatus", i != 3);
                        productNode.setProperty(ProductCatalogConstants.Properties.LIFECYCLE, ProductCatalogConstants.Lifecycle.CURRENT);
                    } catch (RepositoryException e) {
                        // do nothing
                    }
                    if (i % 2 == 0) {
                        subCategory.addNode(productNode);
                    } else {
                        category.addNode(productNode);
                    }
                });
        category.addNode(subCategory);
        return category;
    }

    private void mockExistingIndexedProducts(final DynamoDbItem... items) {
        OngoingStubbing<List<DynamoDbItem>> stubbing = when(dynamoDBServiceMock.queryItems(anyString(), any(QueryRequest.class)));
        for (DynamoDbItem item : items) {
            stubbing = stubbing.thenReturn(Collections.singletonList(item));
        }
    }

    private DynamoDbItem indexedProductItem(final String uuid, final String path) {
        return new DynamoDbItem()
                .withPrimaryKey(new PrimaryKey("pk", "bobcat#product#" + uuid, "sk", path));
    }

    private Node createCategoryWithProductsGroup() {
        MockNode category = new MockNode("test-group-category", ProductCatalogConstants.NodeTypes.Category.NAME);
        MockNode subCategory = new MockNode("test-subcategory", ProductCatalogConstants.NodeTypes.SubCategory.NAME);
        MockNode group1 = new MockNode("testGroup", ProductCatalogConstants.NodeTypes.ProductGroup.NAME);
        MockNode group2 = new MockNode("testGroup", ProductCatalogConstants.NodeTypes.ProductGroup.NAME);
        subCategory.addNode(group1);
        category.addNode(subCategory);
        MockNode productNode1 = new MockNode("product#1", ProductCatalogConstants.NodeTypes.Product.NAME);
        try {
            productNode1.setProperty("jcr:uuid", "test-product-uuid");
            productNode1.setProperty("mgnl:lastModified", Calendar.getInstance());
            productNode1.setProperty(ProductCatalogConstants.Properties.LIFECYCLE, ProductCatalogConstants.Lifecycle.CURRENT);
        } catch (RepositoryException e) {
            // do nothing
        }
        group1.addNode(productNode1);
        category.addNode(group2);
        MockNode productNode2 = new MockNode("product#2", ProductCatalogConstants.NodeTypes.Product.NAME);
        try {
            productNode2.setProperty("jcr:uuid", "test-product-uuid");
            productNode2.setProperty("mgnl:lastModified", Calendar.getInstance());
            productNode2.setProperty(ProductCatalogConstants.Properties.LIFECYCLE, ProductCatalogConstants.Lifecycle.CURRENT);
        } catch (RepositoryException e) {
            // do nothing
        }
        group2.addNode(productNode2);

        return category;
    }
    private void validateUrls(Map<String, String> expectedUrls) {
        Optional<AttributeValueUpdate> urls = updateItemCaptor.getValue().attributeUpdates().entrySet().stream().filter(u -> "urls".equals(u.getKey())).map(Map.Entry::getValue).findFirst();
        assertTrue(urls.isPresent());
        assertEquals(urls.get().value().m(), expectedUrls.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> AttributeValue.fromS(entry.getValue()))));
    }
}
