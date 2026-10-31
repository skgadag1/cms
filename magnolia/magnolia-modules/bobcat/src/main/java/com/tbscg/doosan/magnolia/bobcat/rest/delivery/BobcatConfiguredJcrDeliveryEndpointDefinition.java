package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import info.magnolia.rest.delivery.jcr.v2.ConfiguredJcrDeliveryEndpointDefinition;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Overrides default implementation for {@link JcrEndpointDefinition} to use
 * custom implementation {@link BobcatJcrDeliveryEndpoint}.
 * Optionally supports excluding specific node names from the delivery endpoint response.
 */
public class BobcatConfiguredJcrDeliveryEndpointDefinition extends ConfiguredJcrDeliveryEndpointDefinition {

    private List<String> excludeNodeNames = new ArrayList<>();

    @Inject
    public BobcatConfiguredJcrDeliveryEndpointDefinition() {
        setImplementationClass(BobcatJcrDeliveryEndpoint.class);
    }

    /**
     * Gets the list of node names to exclude from the response.
     *
     * @return list of node names to exclude
     */
    public List<String> getExcludeNodeNames() {
        return excludeNodeNames;
    }

    /**
     * Sets the list of node names to exclude from the response.
     *
     * @param excludeNodeNames list of node names to exclude
     */
    public void setExcludeNodeNames(List<String> excludeNodeNames) {
        this.excludeNodeNames = excludeNodeNames;
    }
}
