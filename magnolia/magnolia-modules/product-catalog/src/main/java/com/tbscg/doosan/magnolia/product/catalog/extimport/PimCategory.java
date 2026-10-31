package com.tbscg.doosan.magnolia.product.catalog.extimport;

import java.util.List;

/**
 * Represents category/sub-category in hierarchy structure.
 */
public interface PimCategory {

    /**
     * @return name of the category
     */
    String getName();

    /**
     * @return id code of category
     */
    String getCode();

    /**
     * @return description of category
     */
    String getDescription();

    /**
     * @return list of subcategories
     */
    List<? extends PimCategory> getSubCategories();

    /**
     * @return list of products
     */
    List<? extends PimModel> getProducts();

    /**
     * @return list of product groups
     */
    List<? extends PimGroup> getProductGroups();

}
