package com.tbscg.doosan.magnolia.pmd.data.provider.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ApiMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static CollectionType collectionType(Class<? extends Collection> collectionClass,
                                                 Class<?> elementClass) {
        return MAPPER.getTypeFactory().constructCollectionType(
                collectionClass,
                elementClass);
    }

    public static JsonNode jsonNode(final String jsonStr) {
        try {
            return MAPPER.readTree(jsonStr);
        } catch (IOException e) {
            log.error("Error processing JSON: " + e.getLocalizedMessage());
            return null;
        }
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (IOException e) {
            log.error("Error processing JSON: " + e.getLocalizedMessage());
        }
        return null;
    }

    public static <T> T map(String json, Class<T> elementClass) {
        try {
            return MAPPER.readValue(json, elementClass);
        } catch (IOException e) {
            log.error("Error processing JSON: " + e.getLocalizedMessage());
        }
        return null;
    }

    public static <T> List<T> list(String json, Class<T> elementClass) {
        if (json != null && json.length() > 0) {
            try {
                return MAPPER.readValue(json, collectionType(List.class, elementClass));
            } catch (IOException e) {
                log.error("Error processing JSON: " + e.getLocalizedMessage());
            }
        }
        return Collections.emptyList();
    }
}
