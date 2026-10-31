package com.tbscg.doosan.magnolia.product.catalog.extimport;

import java.util.List;

/**
 * Represents model information from PIM
 */
public interface PimGroup {

    /**
     * @return name of the group
     */
    String getGroupName();

    /**
     * @return list of products
     */
    List<? extends PimModel> getProducts();
}
