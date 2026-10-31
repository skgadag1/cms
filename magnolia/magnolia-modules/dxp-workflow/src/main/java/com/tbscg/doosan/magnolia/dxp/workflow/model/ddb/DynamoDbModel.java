package com.tbscg.doosan.magnolia.dxp.workflow.model.ddb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DynamoDbModel {
    protected HashMap<String, AttributeValue> valueMap;

    public Map<String, AttributeValue> getValueMap() {
        return (Map<String, AttributeValue>) valueMap.clone();
    }

    protected void setValueMap(Map<String, AttributeValue> valueMap) {
        this.valueMap = new HashMap<>(valueMap);
    }

    public Map<String, Object> asMap() {
        return valueMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> fromAttributeValue(entry.getValue())));
    }

    public static AttributeValue toAttributeValue(Object value) {
        if (value == null) {
            return AttributeValue.fromNul(true);
        } else if (value instanceof String) {
            return AttributeValue.fromS((String) value);
        } else if (value instanceof Integer) {
            return AttributeValue.fromN(String.valueOf(value));
        } else if (value instanceof Long) {
            return AttributeValue.fromN(String.valueOf(value));
        } else if (value instanceof Float) {
            return AttributeValue.fromN(String.valueOf(value));
        } else if (value instanceof Double) {
            return AttributeValue.fromN(String.valueOf(value));
        } else if (value instanceof BigDecimal) {
            return AttributeValue.fromN(value.toString());
        } else if (value instanceof Boolean) {
            return AttributeValue.fromBool((Boolean)value);
        } else if (value instanceof Map) {
            return AttributeValue.fromM(((Map<String, Object>) value).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> toAttributeValue(entry.getValue()))));
        } else if (value instanceof List) {
            return AttributeValue.fromL(((List)value).stream().map(DynamoDbModel::toAttributeValue).toList());
        }

        throw new IllegalArgumentException("Invalid type for DynamoDbItem: " + value.getClass());
    }

    public static Object fromAttributeValue(AttributeValue value) {
        if (value == null || (value.nul() != null && value.nul())) {
            return null;
        } else if (value.s() != null) {
            return value.s();
        } else if (value.n() != null) {
            return new BigDecimal(value.n());
        } else if (value.bool() != null) {
            return value.bool();
        } else if (value.hasM()) {
            return value.m().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> fromAttributeValue(entry.getValue())));
        } else if (value.hasL()) {
            return value.l().stream().map(DynamoDbModel::fromAttributeValue).toList();
        }

        throw new IllegalArgumentException("Invalid value for DynamoDbItem: " + value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        DynamoDbModel other = (DynamoDbModel) obj;
        return this.valueMap.equals(other.valueMap);
    }

    @Override
    public int hashCode() {
        return valueMap.hashCode();
    }
}
