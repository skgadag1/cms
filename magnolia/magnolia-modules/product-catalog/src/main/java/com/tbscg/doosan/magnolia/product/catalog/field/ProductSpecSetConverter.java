package com.tbscg.doosan.magnolia.product.catalog.field;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSpec;
import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

    public class ProductSpecSetConverter implements Converter<Set<ProductSpec>, Object> {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Result<Object> convertToModel(Set<ProductSpec> fieldValue, ValueContext context) {
            try {
                List<Map<String, Object>> list = fieldValue.stream().map(spec -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("label", spec.getLabel());
                    map.put("value", spec.getValue());
                    map.put("unit", spec.getUnit());
                    return map;
                }).collect(Collectors.toList());
                return Result.ok(objectMapper.writeValueAsString(list));
            } catch (JsonProcessingException e) {
                return Result.error("JSON serialization error: " + e.getMessage());
            }
        }

        @Override
        public Set<ProductSpec> convertToPresentation(Object storedValue, ValueContext context) {
            try {
                if (storedValue == null) {
                    return Collections.emptySet();
                }

                List<Map<String, Object>> list;

                if (storedValue instanceof String jsonString) {
                    list = objectMapper.readValue(jsonString, new TypeReference<>() {
                    });
                } else if (storedValue instanceof List<?>) {
                    list = ((List<?>) storedValue).stream()
                            .filter(it -> it instanceof Map)
                            .map(it -> (Map<String, Object>) it)
                            .collect(Collectors.toList());
                } else {
                    return Collections.emptySet();
                }

                return list.stream()
                        .map(map -> new ProductSpec(
                                (String) map.getOrDefault("label", StringUtils.EMPTY),
                                (String) map.getOrDefault("value", StringUtils.EMPTY),
                                (String) map.getOrDefault("unit",  StringUtils.EMPTY)
                        ))
                        .collect(Collectors.toSet());

            } catch (JsonProcessingException | ClassCastException e) {
                throw new RuntimeException("Error converting stored value to ProductSpec set: " + e.getMessage(), e);
            }
        }
    }