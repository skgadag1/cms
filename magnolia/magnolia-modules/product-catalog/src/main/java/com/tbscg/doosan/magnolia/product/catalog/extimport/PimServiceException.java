package com.tbscg.doosan.magnolia.product.catalog.extimport;

/**
 * Exception of PimService
 */
public class PimServiceException extends Exception {
    public PimServiceException() {
    }

    public PimServiceException(final String message) {
        super(message);
    }

    public PimServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PimServiceException(final Throwable cause) {
        super(cause);
    }
}
