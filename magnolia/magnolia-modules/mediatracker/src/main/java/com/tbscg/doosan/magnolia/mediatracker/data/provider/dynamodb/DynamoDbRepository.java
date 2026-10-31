package com.tbscg.doosan.magnolia.mediatracker.data.provider.dynamodb;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.document.DocumentTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * Class aggregating CRUD operations on DynamoDB.
 */
@Slf4j
public class DynamoDbRepository {

    private final Map<String, DocumentTableSchema> schemaMap = new HashMap<>();

    public void addSchema(String tableName, DocumentTableSchema tableSchema) {
        schemaMap.put(tableName, tableSchema);
    }

    /**
     * Adds new or replaces already existing domain.
     *
     * @param item representing entity to update
     * @param tableName name of table to update
     * @param client dynamoDb client to use
     * @return true if successful or false otherwise
     */
    public boolean createNewOrReplace(EnhancedDocument item, String tableName, DynamoDbEnhancedClient client) {
        try {
            DynamoDbTable<EnhancedDocument> table = client.table(tableName, schemaMap.get(tableName));
            table.putItem(item);
            return true;
        } catch (Exception e) {
            log.error("Error while adding new domain to DynamoDB: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Deletes domain with given code.
     *
     * @param primaryKey item identifying entity to delete
     * @param tableName name of table to update
     * @param client dynamoDb client to use
     * @return true if successful or false otherwise
     */
    public boolean delete(Key primaryKey, String tableName, DynamoDbEnhancedClient client) {
        try {
            DynamoDbTable<EnhancedDocument> table = client.table(tableName, schemaMap.get(tableName));
            table.deleteItem(primaryKey);
            return true;
        } catch (Exception e) {
            log.error("Error while removing domain from DynamoDB: {}", e.getMessage());
            return false;
        }
    }

}
