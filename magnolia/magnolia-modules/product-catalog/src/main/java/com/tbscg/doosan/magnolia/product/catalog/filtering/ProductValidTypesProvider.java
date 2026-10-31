package com.tbscg.doosan.magnolia.product.catalog.filtering;

import com.tbscg.doosan.magnolia.filtering.ValidNodeTypesProvider;
import com.tbscg.doosan.magnolia.product.catalog.field.ProductType;

import java.util.Set;

public class ProductValidTypesProvider implements ValidNodeTypesProvider {

    @Override
    public Set<String> getMatchedNodesByText(String searchingText) {
        return ProductType.findMatchedNodesByText(searchingText);
    }

}
