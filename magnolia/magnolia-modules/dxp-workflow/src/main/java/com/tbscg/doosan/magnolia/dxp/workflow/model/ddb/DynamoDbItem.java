package com.tbscg.doosan.magnolia.dxp.workflow.model.ddb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DynamoDbItem extends DynamoDbModel {

    public DynamoDbItem() {
        this.valueMap = new HashMap<>();
    }

    public DynamoDbItem withPrimaryKey(PrimaryKey primaryKey) {
        valueMap.putAll(primaryKey.getValueMap());
        return this;
    }

    public DynamoDbItem withPrimaryKey(String partitionKey, Object partitionValue) {
        valueMap.put(partitionKey, toAttributeValue(partitionValue));
        return this;
    }

    public DynamoDbItem withString(String key, String value) {
        if (value != null) {
            valueMap.put(key, AttributeValue.fromS(value));
        }
        return this;
    }

    public DynamoDbItem withNumber(String key, Integer value) {
        if (value != null) {
            valueMap.put(key, AttributeValue.fromN(String.valueOf(value)));
        }
        return this;
    }

    public DynamoDbItem withNumber(String key, Long value) {
        if (value != null) {
            valueMap.put(key, AttributeValue.fromN(String.valueOf(value)));
        }
        return this;
    }

    public DynamoDbItem withNumber(String key, Float value) {
        if (value != null) {
            valueMap.put(key, AttributeValue.fromN(String.valueOf(value)));
        }
        return this;
    }

    public DynamoDbItem withBoolean(String key, Boolean value) {
        if (value != null) {
            valueMap.put(key, AttributeValue.fromBool(value));
        }
        return this;
    }

    public DynamoDbItem withMap(String key, Map<String, Object> value) {
        if (value != null) {
            valueMap.put(key, toAttributeValue(value));
        }
        return this;
    }

    public Object get(String key) {
        return fromAttributeValue(valueMap.get(key));
    }

    public String getString(String key) {
        return valueMap.get(key).s();
    }

    public BigDecimal getNumber(String key) {
        return new BigDecimal(valueMap.get(key).n());
    }

    public Map<String, Object> getMap(String key) {
        return (Map<String, Object>) fromAttributeValue(valueMap.get(key));
    }

    public Boolean getBoolean(String key) {
        return valueMap.get(key).bool();
    }

    public static DynamoDbItem fromValueMap(Map<String, AttributeValue> valueMap) {
        final DynamoDbItem dynamoDbItem = new DynamoDbItem();
        dynamoDbItem.setValueMap(valueMap);
        return dynamoDbItem;
    }
}
