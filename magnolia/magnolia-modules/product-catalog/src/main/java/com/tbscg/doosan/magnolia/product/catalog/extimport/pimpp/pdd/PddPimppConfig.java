package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.pdd;

import lombok.Data;

/**
 * Class to hold PIM++ configuration
 */
@Data
public class PddPimppConfig {

    private String endpoint;
    private String tokenEndpoint;
    private String tokenUser;
    private String tokenPassword;
    private String enabled;

    /**
     * Creates copy of config.
     *
     * @return copy of self.
     */
    public PddPimppConfig copy() {
        final PddPimppConfig pddPimppConfig = new PddPimppConfig();
        pddPimppConfig.setEndpoint(this.getEndpoint());
        pddPimppConfig.setTokenEndpoint(this.getTokenEndpoint());
        pddPimppConfig.setTokenUser(this.getTokenUser());
        pddPimppConfig.setTokenPassword(this.getTokenPassword());
        pddPimppConfig.setEnabled(this.getEnabled());

        return pddPimppConfig;
    }

    public Boolean isEnabled() {
        return this.getEnabled().equalsIgnoreCase("true");
    }
}
