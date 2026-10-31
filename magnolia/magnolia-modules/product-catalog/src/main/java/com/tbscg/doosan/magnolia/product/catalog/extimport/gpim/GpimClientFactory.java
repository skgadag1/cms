package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

/**
 * {@link GpimClient} factory interface
 */
public interface GpimClientFactory {

    /**
     * @return new instance of {@link GpimClient}
     */
    GpimClient createGpimClient();
}
