package com.tbscg.doosan.magnolia.product.catalog.field.product;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "label")
public class ProductSpec {
    private String label;
    private String value;
    private String unit;
}