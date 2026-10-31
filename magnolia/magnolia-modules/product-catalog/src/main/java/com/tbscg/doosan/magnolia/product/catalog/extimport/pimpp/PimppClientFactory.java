package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

/**
 * Interface for {@link PimppClient} factory
 */
public interface PimppClientFactory {

    /**
     * @return new instance of {@link PimppClient}
     */
    PimppClient createPimppClient();
}
