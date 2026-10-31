package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import org.hamcrest.Matchers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.tbscg.doosan.test.LocalDynamoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class DynamoDBServiceIntegrationTest {

    private final LocalDynamoService localDynamoService = new LocalDynamoService();
    private DynamoDbClient dynamoDbClient;
    private static final String TEST_TABLE = "TestTable";

    private DynamoDBService dynamoDBService;

    @Before
    public void before() throws Exception {

        localDynamoService.start();
        this.dynamoDbClient = localDynamoService.getAmazonDynamoDB();

        //create test tables
        final CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(TEST_TABLE)
                .keySchema(KeySchemaElement.builder().attributeName("iyear").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("title").keyType(KeyType.RANGE).build())
                .attributeDefinitions(AttributeDefinition.builder().attributeName("iyear").attributeType(ScalarAttributeType.N).build(),
                        AttributeDefinition.builder().attributeName("title").attributeType(ScalarAttributeType.S).build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
                .build();

        final DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build();

        dynamoDbClient.createTable(createTableRequest);
        waiter.waitUntilTableExists(r -> r.tableName(TEST_TABLE));

        //init dynamodb service
        dynamoDBService = new DynamoDBService(localDynamoService);


        dynamoDbClient.putItem(r -> r.tableName(TEST_TABLE).item(new DynamoDbItem()
                        .withNumber("iyear", 1949)
                        .withString("title", "Lord of the Rings")
                        .withString("language", "en")
                        .getValueMap()
                ).build());
    }


    @Test
    public void testPutGetItem() {


        dynamoDBService.putItem(TEST_TABLE, new DynamoDbItem()
                .withNumber("iyear", 1984)
                .withString("title", "Year 1984")
                .withString("language", "en"));


        QueryResponse result = dynamoDbClient.query(r -> r.tableName(TEST_TABLE)
                        .keyConditionExpression("iyear = :iyear and title = :title")
                        .expressionAttributeValues(Map.of(":iyear", AttributeValue.fromN(String.valueOf(1984)), ":title", AttributeValue.fromS("Year 1984")))
                        .consistentRead(true)
                        .build()
                );


        DynamoDbItem retrievedItem = DynamoDbItem.fromValueMap(result.items().iterator().next());
        assertEquals(BigDecimal.valueOf(1984), retrievedItem.getNumber("iyear"));
        assertEquals("Year 1984", retrievedItem.getString("title"));


    }

    @Test
    public void testQueryItems() {
        dynamoDBService.batchWriteItem(TEST_TABLE, Arrays.asList(
                new DynamoDbItem()
                        .withNumber("iyear", 1910)
                        .withString("title", "Book of things, vol1")
                        .withString("language", "en"),
                new DynamoDbItem()
                        .withNumber("iyear", 1910)
                        .withString("title", "Book of things, vol2")
                        .withString("language", "en"),
                new DynamoDbItem()
                        .withNumber("iyear", 1910)
                        .withString("title", "Book of things, vol3")
                        .withString("language", "en"),
                new DynamoDbItem()
                        .withNumber("iyear", 1910)
                        .withString("title", "Conan the Librarian")
                        .withString("language", "en")

        ));

        List<DynamoDbItem> result = dynamoDBService.queryItems(TEST_TABLE, QueryRequest.builder()
                .keyConditionExpression("iyear = :iyear and begins_with (title,:title)")
                .expressionAttributeValues(Map.of(":iyear", AttributeValue.fromN(String.valueOf(1910)), ":title", AttributeValue.fromS("Book of things,")))
                .consistentRead(true).build());

        assertEquals(3, result.size());
        assertThat(result, Matchers.hasItem(new DynamoDbItem()
                .withNumber("iyear", 1910)
                .withString("title", "Book of things, vol3")
                .withString("language", "en")));

    }

    @Test
    public void testUpdateItem() {
        dynamoDBService.batchWriteItem(TEST_TABLE, Arrays.asList(
                new DynamoDbItem()
                        .withNumber("iyear", 1910)
                        .withString("title", "Book of things, vol1")
                        .withString("language", "en")
        ));

        final Map<String, AttributeValueUpdate> languageUpdate = Map.of("language", AttributeValueUpdate.builder()
                .action(AttributeAction.PUT)
                .value(AttributeValue.fromS("pl"))
            .build());
        final PrimaryKey primaryKey = new PrimaryKey("iyear", 1910, "title", "Book of things, vol1");
        final UpdateItemRequest updateItemSpec = UpdateItemRequest.builder()
                .key(primaryKey.getValueMap())
                .attributeUpdates(languageUpdate)
                .build();

        dynamoDBService.updateItem(TEST_TABLE, updateItemSpec);

        List<DynamoDbItem> result = dynamoDBService.queryItems(TEST_TABLE, QueryRequest.builder()
                .keyConditionExpression("iyear = :iyear and begins_with (title,:title)")
                .expressionAttributeValues(Map.of(":iyear", AttributeValue.fromN(String.valueOf(1910)), ":title", AttributeValue.fromS("Book of things,")))
                .consistentRead(true)
                .build());

        assertEquals(1, result.size());
        assertThat(result, Matchers.hasItem(new DynamoDbItem()
                .withNumber("iyear", 1910)
                .withString("title", "Book of things, vol1")
                .withString("language", "pl")));

    }

    @After
    public void after() throws Exception {
        localDynamoService.stop();
    }
}
