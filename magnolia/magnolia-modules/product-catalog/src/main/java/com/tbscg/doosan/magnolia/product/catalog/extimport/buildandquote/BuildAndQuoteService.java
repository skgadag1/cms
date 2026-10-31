package com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote;

import java.util.Optional;

public interface BuildAndQuoteService {
    Optional<BuildAndQuoteCategory> getCategoryByName(String categoryName);
}
