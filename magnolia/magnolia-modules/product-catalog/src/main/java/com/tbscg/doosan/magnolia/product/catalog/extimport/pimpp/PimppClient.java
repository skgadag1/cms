package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

/**
 * Client for PIM++ service.
 */
public interface PimppClient {

    /**
     * Gets full PIM++ catalog (categories & sub-categories).
     *
     * @return PimppCatalog instance representing catalog structure for PIM++
     * @throws PimppClientException
     */
    PimppCatalog getCatalog() throws PimppClientException;
}
