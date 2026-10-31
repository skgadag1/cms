package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimProduct;

import java.util.List;

/**
 * Represents model information from PIM
 */
public interface PimModel {

    /**
     * @return model code / id - unique in given PIM
     */
    String getCode();

    /**
     * @return name of the model
     */
    String getName();

    /**
     * @return year of production as string
     */
    String getYearOfProduction();

    /**
     * @return year of production end as string
     */
    String getYearOfProductionEnd();

    /**
     * @return product lifecycle as string
     */
    String getLifecycle();

    /**
     * @return list of CompatibleCarriers as GPIMProducts
     */
    List<GpimProduct> getCompatibleCarriers();

    /**
     * @return description of a model
     */
    String getDescription();
}
