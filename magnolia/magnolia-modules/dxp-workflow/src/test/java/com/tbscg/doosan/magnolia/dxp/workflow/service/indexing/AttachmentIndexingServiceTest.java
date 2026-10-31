package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.MockValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.time.Instant;
import java.util.*;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentIndexingServiceTest extends BaseIndexingServiceTest {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<LinkedHashMap<String, Object>>> LINKED_MAP_LIST_TYPE = new TypeReference<>() {
    };

    @Mock
    private IndexingServiceConfiguration indexingConfigMock;

    @Mock
    private DynamoDBService dynamoDBServiceMock;

    @Mock
    private RegionsService regionsService;

    @InjectMocks
    private AttachmentIndexingService indexingService;

    @Captor
    private ArgumentCaptor<List<DynamoDbItem>> itemsCaptor;

    @Captor
    private ArgumentCaptor<List<PrimaryKey>> primaryKeysCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws RepositoryException {
        mockContext();
        indexingService = new AttachmentIndexingService(indexingConfigMock, regionsService, dynamoDBServiceMock);
    }

    @Test
    public void shouldIndex_successfullyIndexAllNodes() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());
        // 11 original nodes + 6 new nodes + 1 attachment category = 18 total
        assertEquals(18, itemsCaptor.getValue().size());
        assertTrue(itemsCaptor.getValue().stream().allMatch(item -> "na".equals(item.getString("region"))));
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithOneProduct() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/attachment#1", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        assertEquals(5, item.get().asMap().size());
        assertTrue(item.get().asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid1", item.get().asMap().get("pk"));
        assertEquals("/bobcat/na/attachment#1", item.get().asMap().get("sk"));
        assertEquals("na", item.get().asMap().get("region"));

        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified",
                ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS, "attachmentType")));
        assertEquals("test-attachment-uuid1", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/attachment#1", payloadMap.get("path"));
        assertEquals(ProductCatalogConstants.NodeTypes.Attachment.NAME, payloadMap.get("attachmentType"));

        Object products = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        List<LinkedHashMap<String, Object>> productsList = objectMapper.convertValue(products, LINKED_MAP_LIST_TYPE);

        assertEquals(1, productsList.size());
        assertEquals("test-product-uuid1", productsList.get(0).get("uuid"));
        assertEquals("/bobcat/na/test-category/test-subcategory/product#1", productsList.get(0).get("path"));

        validateProductCategory(productsList.get(0));
        validateProductSubCategory(productsList.get(0));
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithMultipleProduct() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/attachment#2", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        assertEquals(5, item.get().asMap().size());
        assertTrue(item.get().asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid2", item.get().asMap().get("pk"));
        assertEquals("/bobcat/na/attachment#2", item.get().asMap().get("sk"));
        assertEquals("na", item.get().asMap().get("region"));

        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified",
                ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS)));
        assertEquals("test-attachment-uuid2", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/attachment#2", payloadMap.get("path"));

        Object products = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        List<LinkedHashMap<String, Object>> productsList = objectMapper.convertValue(products, LINKED_MAP_LIST_TYPE);

        // Both items have type rank 0 (Product) in nodesTypeComparator, so the stable sort keeps
        // them in the order returned by the underlying JCR query: product#2 then product#3.
        assertEquals(2, productsList.size());
        assertEquals("test-product-uuid2", productsList.get(0).get("uuid"));
        assertEquals("/bobcat/na/test-category/test-subcategory/product#2", productsList.get(0).get("path"));
        assertEquals("test-product-uuid3", productsList.get(1).get("uuid"));
        assertEquals("/bobcat/na/test-category/product#3", productsList.get(1).get("path"));

        validateProductCategory(productsList.get(0));
        validateProductCategory(productsList.get(1));
        validateProductSubCategory(productsList.get(0));
        Object subcategory = productsList.get(1).get("subCategory");
        assertNull(subcategory);
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithNoProduct() throws IndexingException {
        String publishTime = Instant.now().toString();
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/attachment#3", publishTime, true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        assertEquals(5, item.get().asMap().size());
        assertTrue(item.get().asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid3", item.get().asMap().get("pk"));
        assertEquals("/bobcat/na/attachment#3", item.get().asMap().get("sk"));
        assertEquals("na", item.get().asMap().get("region"));
        assertEquals(publishTime, item.get().asMap().get("dateModified"));

        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified")));
        assertEquals("test-attachment-uuid3", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/attachment#3", payloadMap.get("path"));

        Object products = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        assertNull(products);
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithNotExistingProduct() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/attachment#4", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        assertEquals(5, item.get().asMap().size());
        assertTrue(item.get().asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid4", item.get().asMap().get("pk"));
        assertEquals("/bobcat/na/attachment#4", item.get().asMap().get("sk"));
        assertEquals("na", item.get().asMap().get("region"));

        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified")));
        assertEquals("test-attachment-uuid4", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/attachment#4", payloadMap.get("path"));

        Object products = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        assertNull(products);
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithinGroupAndGroup() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/testGroup", Instant.now().toString(), true));

        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        List<DynamoDbItem> items = itemsCaptor.getValue();
        validateGroup(items.get(0));
        validateFirstItemOfGroup(items.get(1));
        validateSecondItemOfGroup(items.get(2));
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithCategory() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/test-attachment-category/attachment#7", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        assertEquals(5, item.get().asMap().size());
        assertTrue(item.get().asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid7", item.get().asMap().get("pk"));
        assertEquals("/bobcat/na/test-attachment-category/attachment#7", item.get().asMap().get("sk"));
        assertEquals("na", item.get().asMap().get("region"));

        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified", "category", "attachmentType")));
        assertEquals("test-attachment-uuid7", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/test-attachment-category/attachment#7", payloadMap.get("path"));
        assertEquals(ProductCatalogConstants.NodeTypes.Attachment.NAME, payloadMap.get("attachmentType"));

        // Validate category is present
        validateAttachmentCategory(payloadMap);
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithOneRelatedAttachment() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/attachment#10", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        assertEquals(5, item.get().asMap().size());
        assertTrue(item.get().asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid10", item.get().asMap().get("pk"));
        assertEquals("/bobcat/na/attachment#10", item.get().asMap().get("sk"));
        assertEquals("na", item.get().asMap().get("region"));

        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified",
                ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS)));
        assertEquals("test-attachment-uuid10", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/attachment#10", payloadMap.get("path"));

        Object relatedAttachments = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        List<LinkedHashMap<String, Object>> relatedAttachmentsList = objectMapper.convertValue(relatedAttachments, LINKED_MAP_LIST_TYPE);

        assertEquals(1, relatedAttachmentsList.size());
        assertEquals("test-attachment-uuid1", relatedAttachmentsList.get(0).get("uuid"));
        assertEquals("/bobcat/na/attachment#1", relatedAttachmentsList.get(0).get("path"));
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithMultipleRelatedAttachments() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/attachment#11", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        assertEquals(5, item.get().asMap().size());
        assertTrue(item.get().asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid11", item.get().asMap().get("pk"));
        assertEquals("/bobcat/na/attachment#11", item.get().asMap().get("sk"));
        assertEquals("na", item.get().asMap().get("region"));

        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified",
                ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS)));
        assertEquals("test-attachment-uuid11", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/attachment#11", payloadMap.get("path"));

        Object relatedAttachments = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        List<LinkedHashMap<String, Object>> relatedAttachmentsList = objectMapper.convertValue(relatedAttachments, LINKED_MAP_LIST_TYPE);

        assertEquals(2, relatedAttachmentsList.size());
        assertEquals("test-attachment-uuid2", relatedAttachmentsList.get(0).get("uuid"));
        assertEquals("/bobcat/na/attachment#2", relatedAttachmentsList.get(0).get("path"));
        assertEquals("test-attachment-uuid3", relatedAttachmentsList.get(1).get("uuid"));
        assertEquals("/bobcat/na/attachment#3", relatedAttachmentsList.get(1).get("path"));
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentGroupWithRelatedAttachments() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/testGroupWithAttachments", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        List<DynamoDbItem> items = itemsCaptor.getValue();
        // Should have group + 2 attachments within group
        assertEquals(3, items.size());

        // Validate the group itself has related attachments
        DynamoDbItem groupItem = items.stream()
                .filter(i -> "bobcat#attachment#test-attachment-group-with-attachments-uuid".equals(i.getString("pk")))
                .findFirst()
                .orElse(null);

        assertNotNull(groupItem);
        Object payload = groupItem.asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);

        assertTrue(payloadMap.containsKey(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS));
        Object relatedAttachments = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        List<LinkedHashMap<String, Object>> relatedAttachmentsList = objectMapper.convertValue(relatedAttachments, LINKED_MAP_LIST_TYPE);

        assertEquals(1, relatedAttachmentsList.size());
        assertEquals("test-attachment-uuid1", relatedAttachmentsList.get(0).get("uuid"));
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithProductsAndAttachments() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/attachment#12", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        Optional<DynamoDbItem> item = itemsCaptor.getValue().stream().findFirst();

        assertTrue(item.isPresent());
        Object payload = item.get().asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);

        // Should have both products and attachments
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified",
                ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS,
                ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS)));

        Object products = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        List<LinkedHashMap<String, Object>> productsList = objectMapper.convertValue(products, LINKED_MAP_LIST_TYPE);
        assertEquals(1, productsList.size());

        Object relatedAttachments = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        List<LinkedHashMap<String, Object>> relatedAttachmentsList = objectMapper.convertValue(relatedAttachments, LINKED_MAP_LIST_TYPE);
        assertEquals(1, relatedAttachmentsList.size());
    }

    @Test
    public void shouldIndex_successfullyIndexAttachmentWithCategoryAndGroup() throws IndexingException {
        assertTrue(indexingService.index(INDEX_ACTION, "/bobcat/na/test-attachment-category/testCategoryGroup", Instant.now().toString(), true));
        verify(dynamoDBServiceMock, times(1)).batchWriteItem(any(), itemsCaptor.capture());

        List<DynamoDbItem> items = itemsCaptor.getValue();
        // Should have group + 2 attachments
        assertEquals(3, items.size());

        // Validate the group itself
        DynamoDbItem groupItem = items.get(0);
        assertEquals("bobcat#attachment#test-attachment-group-in-category-uuid", groupItem.asMap().get("pk"));

        // Validate attachment in group has category
        DynamoDbItem attachmentItem = items.get(1);
        Object payload = attachmentItem.asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);

        // Validate category is present for attachment in group
        validateAttachmentCategory(payloadMap);
    }

    private void validateSecondItemOfGroup(DynamoDbItem item) {
        assertEquals(5, item.asMap().size());
        assertTrue(item.asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid6", item.asMap().get("pk"));
        assertEquals("/bobcat/na/testGroup/attachment#6", item.asMap().get("sk"));
        assertEquals("na", item.asMap().get("region"));

        Object payload = item.asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified",
                ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS)));
        assertEquals("test-attachment-uuid6", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/testGroup/attachment#6", payloadMap.get("path"));

        Object products = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        List<LinkedHashMap<String, Object>> productsList = objectMapper.convertValue(products, LINKED_MAP_LIST_TYPE);

        assertEquals(1, productsList.size());
        assertEquals("test-product-uuid1", productsList.get(0).get("uuid"));
        assertEquals("/bobcat/na/test-category/test-subcategory/product#1", productsList.get(0).get("path"));

        validateProductCategory(productsList.get(0));
        validateProductSubCategory(productsList.get(0));
    }

    private void validateGroup (DynamoDbItem item) {
        assertEquals("bobcat#attachment#test-attachment-group-uuid", item.asMap().get("pk"));
        Object payload = item.asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertEquals(ProductCatalogConstants.NodeTypes.AttachmentGroup.NAME, payloadMap.get("attachmentType"));
    }

    private void validateFirstItemOfGroup(DynamoDbItem item) {
        assertEquals(5, item.asMap().size());
        assertTrue(item.asMap().keySet().containsAll(Arrays.asList("region", "pk", "dateModified", "payload")));
        assertEquals("bobcat#attachment#test-attachment-uuid5", item.asMap().get("pk"));
        assertEquals("/bobcat/na/testGroup/attachment#5", item.asMap().get("sk"));
        assertEquals("na", item.asMap().get("region"));

        Object payload = item.asMap().get("payload");
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, MAP_TYPE);
        assertTrue(payloadMap.keySet().containsAll(Arrays.asList("uuid", "path", "lastModified",
                ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS)));
        assertEquals("test-attachment-uuid5", payloadMap.get("uuid"));
        assertEquals("/bobcat/na/testGroup/attachment#5", payloadMap.get("path"));

        Object products = payloadMap.get(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        List<LinkedHashMap<String, Object>> productsList = objectMapper.convertValue(products, LINKED_MAP_LIST_TYPE);

        // Both items have type rank 0 (Product) in nodesTypeComparator, so the stable sort keeps
        // them in the order returned by the underlying JCR query: product#2 then product#3.
        assertEquals(2, productsList.size());
        assertEquals("test-product-uuid2", productsList.get(0).get("uuid"));
        assertEquals("/bobcat/na/test-category/test-subcategory/product#2", productsList.get(0).get("path"));
        assertEquals("test-product-uuid3", productsList.get(1).get("uuid"));
        assertEquals("/bobcat/na/test-category/product#3", productsList.get(1).get("path"));

        validateProductCategory(productsList.get(0));
        validateProductCategory(productsList.get(1));
        validateProductSubCategory(productsList.get(0));
        Object subcategory = productsList.get(1).get("subCategory");
        assertNull(subcategory);
    }

    private void validateProductSubCategory(LinkedHashMap<String, Object> product) {
        Object subcategory = product.get("subCategory");
        Map<String, Object> subcategoryMap = objectMapper.convertValue(subcategory, MAP_TYPE);
        assertEquals("test-subCategory-uuid", subcategoryMap.get("uuid"));
        assertEquals("/bobcat/na/test-category/test-subcategory", subcategoryMap.get("path"));
        assertEquals("test-subcategory", subcategoryMap.get("name"));
    }

    private void validateProductCategory(LinkedHashMap<String, Object> product) {
        Object category = product.get("category");
        Map<String, Object> categoryMap = objectMapper.convertValue(category, MAP_TYPE);
        assertEquals("test-category-uuid", categoryMap.get("uuid"));
        assertEquals("/bobcat/na/test-category", categoryMap.get("path"));
        assertEquals("bobcat/na/test-category", categoryMap.get("name"));
    }

    private void validateAttachmentCategory(Map<String, Object> payloadMap) {
        Object category = payloadMap.get("category");
        assertNotNull(category);
        Map<String, Object> categoryMap = objectMapper.convertValue(category, MAP_TYPE);
        assertEquals("test-attachment-category-uuid", categoryMap.get("uuid"));
        assertEquals("/bobcat/na/test-attachment-category", categoryMap.get("path"));
        assertEquals("test-attachment-category", categoryMap.get("name"));
        assertEquals("CAT-001", categoryMap.get(ProductCatalogConstants.Properties.ID));
    }

    @Test
    public void shouldIndex_notExecuteBatchWriteItem() throws IndexingException {
        indexingService.index(INDEX_ACTION, "/bobcat/na-test", Instant.now().toString(), true);
        verify(dynamoDBServiceMock, times(0)).batchWriteItem(any(), anyList());
    }

    @Test
    public void shouldReindex_successfullyReindex() throws IndexingException {
        assertTrue(indexingService.index(DEINDEX_ACTION, "/bobcat/na", Instant.now().toString(), true));
        List<PrimaryKey> expectedKeys = Arrays.asList(
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-category-uuid", "sk", "/bobcat/na/test-attachment-category"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-group-uuid", "sk", "/bobcat/na/testGroup"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid1", "sk", "/bobcat/na/attachment#1"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid2", "sk", "/bobcat/na/attachment#2"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid3", "sk", "/bobcat/na/attachment#3"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid4", "sk", "/bobcat/na/attachment#4"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid5", "sk", "/bobcat/na/testGroup/attachment#5"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid6", "sk", "/bobcat/na/testGroup/attachment#6"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid7", "sk", "/bobcat/na/test-attachment-category/attachment#7"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-group-in-category-uuid", "sk", "/bobcat/na/test-attachment-category/testCategoryGroup"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid8", "sk", "/bobcat/na/test-attachment-category/testCategoryGroup/attachment#8"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid9", "sk", "/bobcat/na/test-attachment-category/testCategoryGroup/attachment#9"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid10", "sk", "/bobcat/na/attachment#10"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid11", "sk", "/bobcat/na/attachment#11"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid12", "sk", "/bobcat/na/attachment#12"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-group-with-attachments-uuid", "sk", "/bobcat/na/testGroupWithAttachments"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid13", "sk", "/bobcat/na/testGroupWithAttachments/attachment#13"),
                new PrimaryKey("pk", "bobcat#attachment#test-attachment-uuid14", "sk", "/bobcat/na/testGroupWithAttachments/attachment#14")
        );
        verify(dynamoDBServiceMock, times(1)).batchWriteDeleteItems(any(), anyList(), primaryKeysCaptor.capture());

        List<PrimaryKey> keys = primaryKeysCaptor.getValue();
        assertEquals(18, keys.size());
        assertTrue(keys.containsAll(expectedKeys));
    }

    private List<Node> generateNodes() throws RepositoryException {
        MockNode attachment1 = createAttachmentWithOneProduct();
        MockNode attachment2 = createAttachmentWithMultipleProducts();
        MockNode attachment3 = createAttachmentWithNoProducts();
        MockNode attachment4 = createAttachmentWithNotExistingProducts();
        MockNode group = createAttachmentsGroup();
        return new ArrayList<>(List.of(attachment1, attachment2, attachment3, attachment4, group));
    }

    private MockNode createAttachment(int i, MockNode products) throws RepositoryException {
        MockNode region = new MockNode("bobcat/na", "dxp:region");
        MockNode attachmentNode = new MockNode("attachment#" + i, ProductCatalogConstants.NodeTypes.Attachment.NAME);
        attachmentNode.setParent(region);
        setCommonProperties(attachmentNode, "test-attachment-uuid" + i);
        attachmentNode.setIdentifier("attachment#" + i);
        if (products != null) {
            attachmentNode.addNode(products);
        }
        return attachmentNode;
    }

    private MockNode createAttachmentWithOneProduct() throws RepositoryException {
        MockNode products = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product0 = new MockNode("products0");
        product0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#1"));
        products.addNode(product0);
        return createAttachment(1, products);
    }

    private MockNode createAttachmentWithMultipleProducts() throws RepositoryException {
        MockNode products = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product0 = new MockNode("products0");
        product0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#2"));
        MockNode product1 = new MockNode("products1");
        product1.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#3"));
        products.addNode(product0);
        products.addNode(product1);
        return createAttachment(2, products);
    }

    private MockNode createAttachmentWithNoProducts() throws RepositoryException {
        return createAttachment(3, null);
    }

    private MockNode createAttachmentWithNotExistingProducts() throws RepositoryException {
        MockNode products = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product0 = new MockNode("products0");
        product0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#20"));
        products.addNode(product0);
        return createAttachment(4, products);
    }

    private MockNode createAttachmentsGroup() throws RepositoryException {
        MockNode region = new MockNode("bobcat/na", "dxp:region");
        MockNode group = new MockNode("testGroup", ProductCatalogConstants.NodeTypes.AttachmentGroup.NAME);
        group.setParent(region);
        setCommonProperties(group, "test-attachment-group-uuid" );
        MockNode products1 = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product0 = new MockNode("products0");
        product0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#2"));
        MockNode product1 = new MockNode("products1");
        product1.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#3"));
        products1.addNode(product0);
        products1.addNode(product1);
        MockNode attachmentNode1 = createAttachment(5, products1);
        group.addNode(attachmentNode1);

        MockNode products2 = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product02 = new MockNode("products0");
        product02.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#1"));
        products2.addNode(product02);
        MockNode attachmentNode2 = createAttachment(6, products2);
        group.addNode(attachmentNode2);

        return group;
    }

    private MockNode createAttachmentWithCategory() throws RepositoryException {
        MockNode attachmentCategory = new MockNode("bobcat/na/test-attachment-category", ProductCatalogConstants.NodeTypes.AttachmentCategory.NAME);
        setCommonProperties(attachmentCategory, "test-attachment-category-uuid");
        attachmentCategory.setProperty(ProductCatalogConstants.Properties.ID, "CAT-001");

        MockNode attachmentNode = new MockNode("attachment#7", ProductCatalogConstants.NodeTypes.Attachment.NAME);
        attachmentNode.setParent(attachmentCategory);
        setCommonProperties(attachmentNode, "test-attachment-uuid7");

        return attachmentNode;
    }

    private MockNode createAttachmentGroupWithCategory() throws RepositoryException {
        MockNode attachmentCategory = new MockNode("bobcat/na/test-attachment-category", ProductCatalogConstants.NodeTypes.AttachmentCategory.NAME);
        setCommonProperties(attachmentCategory, "test-attachment-category-uuid");
        attachmentCategory.setProperty(ProductCatalogConstants.Properties.ID, "CAT-001");

        MockNode group = new MockNode("testCategoryGroup", ProductCatalogConstants.NodeTypes.AttachmentGroup.NAME);
        group.setParent(attachmentCategory);
        setCommonProperties(group, "test-attachment-group-in-category-uuid");

        MockNode products1 = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product0 = new MockNode("products0");
        product0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#1"));
        products1.addNode(product0);

        MockNode attachmentNode1 = new MockNode("attachment#8", ProductCatalogConstants.NodeTypes.Attachment.NAME);
        attachmentNode1.setParent(group);
        setCommonProperties(attachmentNode1, "test-attachment-uuid8");
        attachmentNode1.addNode(products1);
        group.addNode(attachmentNode1);

        MockNode products2 = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product02 = new MockNode("products0");
        product02.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#2"));
        products2.addNode(product02);

        MockNode attachmentNode2 = new MockNode("attachment#9", ProductCatalogConstants.NodeTypes.Attachment.NAME);
        attachmentNode2.setParent(group);
        setCommonProperties(attachmentNode2, "test-attachment-uuid9");
        attachmentNode2.addNode(products2);
        group.addNode(attachmentNode2);

        return group;
    }

    private MockNode createProductNode(int i) throws RepositoryException {
        MockNode productNode = new MockNode("product#" + i, ProductCatalogConstants.NodeTypes.Product.NAME);
        setCommonProperties(productNode, "test-product-uuid" + i);
        productNode.setIdentifier("product#" + i);
        return productNode;
    }

    private MockNode createAttachmentWithOneRelatedAttachment() throws RepositoryException {
        MockNode relatedAttachments = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        MockNode relatedAttachment0 = new MockNode("relatedAttachments0");
        relatedAttachment0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.ATTACHMENT, new MockValue("attachment#1"));
        relatedAttachments.addNode(relatedAttachment0);
        MockNode attachmentNode = createAttachment(10, null);
        attachmentNode.addNode(relatedAttachments);
        return attachmentNode;
    }

    private MockNode createAttachmentWithMultipleRelatedAttachments() throws RepositoryException {
        MockNode relatedAttachments = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        MockNode relatedAttachment0 = new MockNode("relatedAttachments0");
        relatedAttachment0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.ATTACHMENT, new MockValue("attachment#2"));
        MockNode relatedAttachment1 = new MockNode("relatedAttachments1");
        relatedAttachment1.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.ATTACHMENT, new MockValue("attachment#3"));
        relatedAttachments.addNode(relatedAttachment0);
        relatedAttachments.addNode(relatedAttachment1);
        MockNode attachmentNode = createAttachment(11, null);
        attachmentNode.addNode(relatedAttachments);
        return attachmentNode;
    }

    private MockNode createAttachmentWithProductsAndAttachments() throws RepositoryException {
        MockNode products = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product0 = new MockNode("products0");
        product0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#1"));
        products.addNode(product0);

        MockNode relatedAttachments = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        MockNode relatedAttachment0 = new MockNode("relatedAttachments0");
        relatedAttachment0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.ATTACHMENT, new MockValue("attachment#1"));
        relatedAttachments.addNode(relatedAttachment0);

        MockNode attachmentNode = createAttachment(12, products);
        attachmentNode.addNode(relatedAttachments);
        return attachmentNode;
    }

    private MockNode createAttachmentGroupWithRelatedAttachments() throws RepositoryException {
        MockNode region = new MockNode("bobcat/na", "dxp:region");
        MockNode group = new MockNode("testGroupWithAttachments", ProductCatalogConstants.NodeTypes.AttachmentGroup.NAME);
        group.setParent(region);
        setCommonProperties(group, "test-attachment-group-with-attachments-uuid");
        group.setIdentifier("testGroupWithAttachments");

        // Add related attachments to the group itself
        MockNode relatedAttachments = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.RELATED_ATTACHMENTS);
        MockNode relatedAttachment0 = new MockNode("relatedAttachments0");
        relatedAttachment0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.ATTACHMENT, new MockValue("attachment#1"));
        relatedAttachments.addNode(relatedAttachment0);
        group.addNode(relatedAttachments);

        // Add child attachments
        MockNode products1 = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product0 = new MockNode("products0");
        product0.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#1"));
        products1.addNode(product0);
        MockNode attachmentNode1 = createAttachment(13, products1);
        group.addNode(attachmentNode1);

        MockNode products2 = new MockNode(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCTS);
        MockNode product02 = new MockNode("products0");
        product02.setProperty(ProductCatalogConstants.NodeTypes.Attachment.Properties.PRODUCT, new MockValue("product#2"));
        products2.addNode(product02);
        MockNode attachmentNode2 = createAttachment(14, products2);
        group.addNode(attachmentNode2);

        return group;
    }

    private void setCommonProperties(MockNode category, String uuid) throws RepositoryException {
        category.setProperty("jcr:uuid", uuid);
        category.setProperty("mgnl:lastModified", Calendar.getInstance());
    }

    private void mockContext() throws RepositoryException {
        final MockContext ctx = new MockContext();
        final MockSession productSession = mockProductSession();
        final MockSession attachmentSession = mockAttachmentSession();
        ctx.addSession(ProductCatalogConstants.PRODUCTS_WORKSPACE, productSession);
        ctx.addSession(ProductCatalogConstants.ATTACHMENTS_WORKSPACE, attachmentSession);
        MgnlContext.setInstance(ctx);
    }

    private MockSession mockProductSession() throws RepositoryException {
        final MockSession mockSession = new MockSession(ProductCatalogConstants.PRODUCTS_WORKSPACE);
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode category = new MockNode("bobcat/na/test-category", ProductCatalogConstants.NodeTypes.Category.NAME);
        setCommonProperties(category, "test-category-uuid");
        MockNode subCategory = new MockNode("test-subcategory", ProductCatalogConstants.NodeTypes.SubCategory.NAME);
        setCommonProperties(subCategory, "test-subCategory-uuid");
        category.addNode(subCategory);
        parent.addNode(category);

        MockNode product1 = createProductNode(1);
        MockNode product2 = createProductNode(2);
        MockNode product3 = createProductNode(3);

        category.addNode(product3);
        subCategory.addNode(product1);
        subCategory.addNode(product2);

        return mockSession;
    }

    private MockSession mockAttachmentSession() throws RepositoryException {
        final MockSession mockSession = new MockSession(ProductCatalogConstants.ATTACHMENTS_WORKSPACE);
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode bobcat = new MockNode("bobcat", "dxp:region");
        MockNode region = new MockNode("na", "dxp:region");

        // Add attachment category with attachment and group
        MockNode attachmentCategory = new MockNode("test-attachment-category", ProductCatalogConstants.NodeTypes.AttachmentCategory.NAME);
        setCommonProperties(attachmentCategory, "test-attachment-category-uuid");
        attachmentCategory.setProperty(ProductCatalogConstants.Properties.ID, "CAT-001");

        MockNode attachmentWithCategory = createAttachmentWithCategory();
        attachmentCategory.addNode(attachmentWithCategory);

        MockNode groupWithCategory = createAttachmentGroupWithCategory();
        attachmentCategory.addNode(groupWithCategory);

        region.addNode(attachmentCategory);

        generateNodes().forEach(node -> region.addNode((MockNode) node));

        // Add new test nodes for related attachments
        region.addNode(createAttachmentWithOneRelatedAttachment());
        region.addNode(createAttachmentWithMultipleRelatedAttachments());
        region.addNode(createAttachmentWithProductsAndAttachments());
        region.addNode(createAttachmentGroupWithRelatedAttachments());

        bobcat.addNode(region);
        parent.addNode(bobcat);
        return mockSession;
    }
}
