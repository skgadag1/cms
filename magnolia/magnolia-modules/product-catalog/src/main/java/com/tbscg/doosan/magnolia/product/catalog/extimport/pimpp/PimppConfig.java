package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import lombok.Data;

/**
 * Class to hold PIM++ configuration
 */
@Data
public class PimppConfig {

    private String endpoint;

    /**
     * Creates copy of config.
     *
     * @return copy of self.
     */
    public PimppConfig copy() {
        final PimppConfig pimppConfig = new PimppConfig();
        pimppConfig.setEndpoint(this.getEndpoint());

        return pimppConfig;
    }
}
