package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import lombok.Data;

/**
 * Class to hold GPIM configuration
 */
@Data
public class GpimConfig {

    private String endpoint;
    private String secretKey;

    /**
     * Creates copy of config.
     *
     * @return copy of self.
     */
    public GpimConfig copy() {
        final GpimConfig gpimConfig = new GpimConfig();
        gpimConfig.setEndpoint(this.getEndpoint());
        gpimConfig.setSecretKey(this.getSecretKey());

        return gpimConfig;
    }
}
