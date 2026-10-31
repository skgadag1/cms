package com.tbscg.doosan.magnolia.product.catalog.field.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Table property value of a product used in Product API.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableProperty {
    private List<String> header;
    private List<List<ProductProperty>> rows;
}
