package com.tbscg.doosan.magnolia.sso.service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.document.DocumentTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class DynamoDBService {
    public EnhancedDocument getItemsByStringKey(String field, String tableName, String value) {
        EnhancedDocument results = null;

        try {
            DynamoDbClient client = DynamoDbClient.builder().build();
            DynamoDbEnhancedClient dynamoDB = DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();

            DocumentTableSchema schema = TableSchema.documentSchemaBuilder()
                    .addIndexPartitionKey(TableMetadata.primaryIndexName(), field, AttributeValueType.S)
                    .attributeConverterProviders(AttributeConverterProvider.defaultProvider())
                    .build();
            DynamoDbTable<EnhancedDocument> table = dynamoDB.table(tableName, schema);
            EnhancedDocument item = table.getItem(Key.builder().partitionValue(value).build());

            if (item != null) {
                results = item;
            } else {
                log.warn("No item found with the key {}!", value);
            }
        } catch (Exception e) {
            log.error("Error retrieving data from DynamoDB: {}", e.getMessage());
        }

        return results;
    }
}
