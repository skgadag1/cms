package com.tbscg.doosan.magnolia.product.catalog.extimport;

import java.util.Collection;

public interface PimCatalog {

    Collection<? extends PimCategory> getCategories();
}
