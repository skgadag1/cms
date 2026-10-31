package com.tbscg.doosan.magnolia.product.catalog.field.product;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static java.util.Collections.emptyList;

/**
 * Custom deserializer for @{@link PropertyValue} of a product used in Product API.
 */
public class PropertyValueDeserializer extends StdDeserializer<PropertyValue> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PropertyValueDeserializer() {
        this(null);
    }

    public PropertyValueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PropertyValue deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        if (START_ARRAY.equals(jsonParser.getCurrentToken())) {
            final PropertyArrayNode propertyArrayNode = handleArray(objectMapper.readTree(jsonParser));
            return new PropertyValue(propertyArrayNode.getProperties(), propertyArrayNode.getType());
        } else if (START_OBJECT.equals(jsonParser.getCurrentToken())) {
            return new PropertyValue(objectMapper.readValue(jsonParser, TableProperty.class));
        }
        return new PropertyValue(jsonParser.getValueAsString());
    }

    private PropertyArrayNode handleArray(final JsonNode treeNode) throws JsonProcessingException {
        if (!treeNode.isEmpty()) {
            if (treeNode.get(0).get("item") != null) {
                return new PropertyArrayNode(Arrays.asList(objectMapper.treeToValue(treeNode, ListProperty[].class)), ListProperty.class);
            }
            if (!treeNode.get(0).isArray()) {
                return new PropertyArrayNode(Arrays.asList(objectMapper.treeToValue(treeNode, ProductProperty[].class)), ProductProperty.class);
            } else {
                final List<List<?>> properties = new ArrayList<>();
                for (final JsonNode item : treeNode) {
                    properties.add(handleArray(item).getProperties());
                }
                return new PropertyArrayNode(properties, ProductProperty.class);
            }
        }
        return new PropertyArrayNode(emptyList(), ListProperty.class);
    }

    @Getter
    @AllArgsConstructor
    private static class PropertyArrayNode {
        private final List<?> properties;
        private final Class<?> type;
    }
}
