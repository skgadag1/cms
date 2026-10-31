package com.tbscg.doosan.magnolia.dxp.workflow.model.ddb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class PrimaryKey extends DynamoDbModel {
    private PrimaryKey() {}

    public PrimaryKey(String partitionKey, Object partitionValue) {
        valueMap = new HashMap<>(Map.of(partitionKey, toAttributeValue(partitionValue)));
    }

    public PrimaryKey(String partitionKey, Object partitionValue, String sortKey, Object sortValue) {
        valueMap = new HashMap<>(Map.of(
                partitionKey, toAttributeValue(partitionValue),
                sortKey, toAttributeValue(sortValue)
        ));
    }

    public static PrimaryKey fromValueMap(Map<String, AttributeValue> valueMap) {
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setValueMap(valueMap);
        return primaryKey;
    }
}
