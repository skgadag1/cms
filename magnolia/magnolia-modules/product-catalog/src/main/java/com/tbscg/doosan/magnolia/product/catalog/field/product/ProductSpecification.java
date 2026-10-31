package com.tbscg.doosan.magnolia.product.catalog.field.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Product specification used in Product API.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSpecification {
    private String name;
    private String serial;
    private String status;
    private String description;
    private String modifiedDate;
    private String imageUrl;
    private List<ProductSection> sections;
}
