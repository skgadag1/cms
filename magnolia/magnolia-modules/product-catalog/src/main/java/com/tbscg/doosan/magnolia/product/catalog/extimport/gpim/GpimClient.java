package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

/**
 * Client for GPIM service
 */
public interface GpimClient {

    /**
     * Shallow fetch category info.
     *
     * @param categoryName name of category
     * @param locale       locale clode from REGION app
     * @return {@link GpimCategory} object representing category info.
     * @throws GpimClientException
     */
    GpimCategory getCategory(String categoryName, String locale) throws GpimClientException;

    /**
     * Shallow fetch category info.
     *
     * @param categoryId id of category
     * @return {@link GpimProduct} object representing related product info.
     * @throws GpimClientException
     */
    GpimProduct getProduct(String categoryId) throws GpimClientException;

}
