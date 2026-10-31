package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_DATE_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_URLS_KEY_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DynamoDBServiceTest {

    @Mock
    private DynamoDbClient amazonDynamoDB;

    @Mock
    private PutItemResponse putItemOutcome;

    @Mock
    private DeleteItemResponse deleteItemOutcome;

    private DynamoDBService dynamoDBService;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        when(amazonDynamoDB.putItem(any(PutItemRequest.class))).thenReturn(putItemOutcome);
        when(amazonDynamoDB.deleteItem(any(DeleteItemRequest.class))).thenReturn(deleteItemOutcome);

        final DynamoDBClientFactoryImpl dynamoDBClientFactory = new DynamoDBClientFactoryImpl() {
            @Override
            protected DynamoDbClient createAmazonDynamoDB() {
                return amazonDynamoDB;
            }
        };

        dynamoDBService = new DynamoDBService(dynamoDBClientFactory);
    }

    @Test
    public void shouldPutItem_successfullyPutItem() {
        final DynamoDbItem sampleItem = new DynamoDbItem()
                .withPrimaryKey("sample_partition_key", "sampleKeyValue")
                .withString("sample_attribute", "sampleAttributeValue");

        assertEquals(putItemOutcome, dynamoDBService.putItem("sampleTable", sampleItem));
        verify(amazonDynamoDB).putItem(eq(PutItemRequest.builder().item(sampleItem.getValueMap()).tableName("sampleTable").build()));
    }

    @Test
    public void shouldDeleteItem_successfullyDeleteItem() {
        final PrimaryKey samplePrimaryKey = new PrimaryKey("sample_partition_key", "sampleKeyValue");

        assertEquals(deleteItemOutcome, dynamoDBService.deleteItem("sampleTable", samplePrimaryKey));
        verify(amazonDynamoDB).deleteItem(eq(DeleteItemRequest.builder().key(samplePrimaryKey.getValueMap()).tableName("sampleTable").build()));
    }

    @Test
    public void shouldBatchWriteItem_successfullyBatchWriteItem() {
        final DynamoDbItem item = new DynamoDbItem()
                .withPrimaryKey("pk", "bobcat#product#test_item_uuid")
                .withString("region", "na")
                .withString("payload", "payload")
                .withString("modifiedDate", "2021-07-21");
        final BatchWriteItemResponse outcome = generateBatchWriteItemOutcome("test_table", item, false).get(0);
        when(amazonDynamoDB.batchWriteItem(any(BatchWriteItemRequest.class))).thenReturn(outcome);
        dynamoDBService.batchWriteItem("test_table", Collections.singletonList(item));

        verify(amazonDynamoDB, times(1)).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @Test
    public void shouldBatchWriteItem_successfullyBatchWriteItemWithRetry() {
        final DynamoDbItem item = new DynamoDbItem()
                .withPrimaryKey("pk", "bobcat#product#test_item_uuid")
                .withString("region", "na")
                .withString("payload", "payload")
                .withString("modifiedDate", "2021-07-21");
        final List<BatchWriteItemResponse> outcomes = generateBatchWriteItemOutcome("test_table", item, true);
        when(amazonDynamoDB.batchWriteItem(any(BatchWriteItemRequest.class))).thenReturn(outcomes.get(0), outcomes.get(1));

        dynamoDBService.batchWriteItem("test_table", Collections.singletonList(item));

        verify(amazonDynamoDB, times(2)).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @Test
    public void shouldBatchDeleteItem_successfullyBatchDeleteItem() {
        final PrimaryKey primaryKey = new PrimaryKey("pk", "bobcat#product#test_item_uuid");
        final BatchWriteItemResponse outcome = BatchWriteItemResponse.builder().unprocessedItems(new HashMap<>()).build();
        when(amazonDynamoDB.batchWriteItem(any(BatchWriteItemRequest.class))).thenReturn(outcome);

        dynamoDBService.batchDeleteItem("test_table", Collections.singletonList(primaryKey));

        verify(amazonDynamoDB, times(1)).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @Test
    public void shouldUpdateItem_successfullyUpdateItem() {
        // given
        final Map<String, AttributeValueUpdate> attributeUpdates = Map.of(
                TABLE_URLS_KEY_NAME, AttributeValueUpdate.builder().action(AttributeAction.PUT).value(AttributeValue.fromS("/url")).build(),
                TABLE_DATE_KEY_NAME, AttributeValueUpdate.builder().action(AttributeAction.PUT).value(AttributeValue.fromN("0")).build()
        );
        final PrimaryKey primaryKey = new PrimaryKey("pk", "bobcat#product#test_item_uuid", "sk", "/bobcat/cn/Brushcat-Std-Flow/72-Brushcat-Std-Flow");
        final UpdateItemRequest updateItemSpec = UpdateItemRequest.builder()
                .key(primaryKey.getValueMap())
                .attributeUpdates(attributeUpdates)
                .build();

        final UpdateItemRequest expectedSpec = updateItemSpec.toBuilder().tableName("sampleTable").build();
        // when
        dynamoDBService.updateItem("sampleTable", updateItemSpec);

        // then
        verify(amazonDynamoDB).updateItem(eq(expectedSpec));
        verify(amazonDynamoDB, times(1)).updateItem(expectedSpec);
    }

    private List<BatchWriteItemResponse> generateBatchWriteItemOutcome(final String tableName, final DynamoDbItem item, final boolean withUnprocessed) {
        final List<BatchWriteItemResponse> outcomes = new ArrayList<>();

        if (withUnprocessed) {
            final PutRequest putRequest = PutRequest.builder().item(item.getValueMap())
                    .build();
            final WriteRequest writeRequest = WriteRequest.builder().putRequest(putRequest)
                    .build();
            final BatchWriteItemResponse unprocessedOutcome = BatchWriteItemResponse.builder().unprocessedItems(Map.of(tableName, Collections.singletonList(writeRequest))).build();
            outcomes.add(unprocessedOutcome);
        }
        final BatchWriteItemResponse processedOutcome = BatchWriteItemResponse.builder().build();
        outcomes.add(processedOutcome);
        return outcomes;
    }
}
