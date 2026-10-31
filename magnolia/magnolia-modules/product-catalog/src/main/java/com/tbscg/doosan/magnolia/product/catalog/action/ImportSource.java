package com.tbscg.doosan.magnolia.product.catalog.action;

public enum ImportSource {
    GPIM_PRODUCTS("GPIM_PRODUCTS"),
    GPIM_ATTACHMENTS("GPIM_ATTACHMENTS"),
    PIMPP_PRODUCTS("PIMPP_PRODUCTS"),
    PIMPP_ATTACHMENTS("PIMPP_ATTACHMENTS");

    public final String label;

    ImportSource(String label) {
        this.label = label;
    }
}
