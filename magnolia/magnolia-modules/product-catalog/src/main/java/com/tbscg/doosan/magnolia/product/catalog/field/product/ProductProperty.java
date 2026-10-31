package com.tbscg.doosan.magnolia.product.catalog.field.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Property of a product used in Product API.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductProperty {
    private String id;
    private ProductPropertyType type;
    private String label;
    @JsonDeserialize(using = PropertyValueDeserializer.class)
    private PropertyValue value;
    private String unit;
}
