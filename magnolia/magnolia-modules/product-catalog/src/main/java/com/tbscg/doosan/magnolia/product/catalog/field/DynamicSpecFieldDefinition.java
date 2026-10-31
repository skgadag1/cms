package com.tbscg.doosan.magnolia.product.catalog.field;

import info.magnolia.ui.field.ConfiguredFieldDefinition;
import info.magnolia.ui.field.FieldType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSpec;

/**
 * Definition for DynamicSpecField used for key specification fields on product pages.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@FieldType("dynamicSpecField")
public class DynamicSpecFieldDefinition extends ConfiguredFieldDefinition<Set<ProductSpec>> {

    private Integer limitedOptions;

    public DynamicSpecFieldDefinition() {
        setType(String.class);
        setFactoryClass(DynamicSpecFieldFactory.class);
        setConverterClass(ProductSpecSetConverter.class);
    }
}
